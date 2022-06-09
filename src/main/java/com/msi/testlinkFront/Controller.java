package com.msi.testlinkFront;

import br.eti.kinoshita.testlinkjavaapi.constants.ExecutionStatus;
import br.eti.kinoshita.testlinkjavaapi.model.*;
import com.msi.ConfigManager;
import com.msi.ExceptionListenerCustom;
import com.msi.testlinkBack.ToolManager;
import com.msi.testlinkBack.api.TestPlanApi;
import com.msi.testlinkFront.services.GetPlanService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.textfield.CustomTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;


//TODO: limit the progress icon in time and add notification on failure
//TODO: update list of the selected tests                                           - DONE
//TODO: refresh list of tests by button
//TODO: fix thread leak in a test plan.
//TODO: open submit window works from second attempt                                - DONE
//TODO: update selected test cases for submit view
//TODO: set dev key in opened popup with ? sign and image of where to search it
//TODO: no rights - terminate progressing
//TODO: read fields to submit window                                                - DONE
//TODO: change warning text and font size
//TODO: add text field for notifications in report dialog
//TODO: change favicon from default
//TODO: adjust size of the list from main window                                    - DONE
//TODO: set dev key field with ?-help symbol                                        - DONE
//TODO: progress for reporting

public class Controller implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(ToolManager.class.getSimpleName());

    ToolManager toolManager;
    ConfigManager configManager;

    public Controller() {
        configManager = ConfigManager.getInstance();
        configManager.getConfigBack();
        String devKey = configManager.getConfig().getDevKey();
        toolManager = ToolManager.getInstance(devKey);
    }

    @FXML
    public VBox main;

    @FXML
    public HBox testPlanProjectListHBox;

    // TODO: place testProjectListBox and testPlanListBox on the same line
    @FXML
    private ComboBox<String> testProjectListBox;

    @FXML
    private ComboBox<String> testPlanListBox;

    private ProgressIndicator pb;

    String expandText = "Expand";
    String collapseText = "Collapse";

    @FXML
    public Button expandListBtn;

    @FXML
    public Button reportBtn;
    private ReportStageDialog reportStageDialog;

    @FXML
    public CustomTextField executionStatusNumsPass;

    @FXML
    public CustomTextField executionStatusNumsFailed;

    @FXML
    public CustomTextField executionStatusNumsNotRun;

    @FXML
    public CustomTextField executionStatusNumsBlocked;

    @FXML
    private StackPane testSuitsTreePane;
    TreeItem<String> testCasesTree;

    private TreeView<String> testPlanView;

    @FXML
    public Button refreshListBtn;

    @FXML
    private TextField devKeyTextField;

    @FXML
    private Button saveDevKey;

    @FXML Button howToSetDevKey;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        testPlanListBox.setDisable(true);
        expandListBtn.setText(expandText);
        expandListBtn.setDisable(true);

        List <CustomTextField> statusFields = List.of(executionStatusNumsPass
                , executionStatusNumsNotRun
                , executionStatusNumsNotRun
                , executionStatusNumsBlocked);
        executionStatusNumsPass.setLeft(getStatusBallImageView(Color.GREEN));
        executionStatusNumsPass.setPromptText("Passed");
        executionStatusNumsNotRun.setLeft(getStatusBallImageView(Color.GRAY));
        executionStatusNumsNotRun.setPromptText("Not run");
        executionStatusNumsFailed.setLeft(getStatusBallImageView(Color.RED));
        executionStatusNumsFailed.setPromptText("Failed");
        executionStatusNumsBlocked.setLeft(getStatusBallImageView(Color.BLUE));
        executionStatusNumsBlocked.setPromptText("Blocked");
        statusFields.forEach(cf -> cf.setEditable(false));

        devKeyTextField.setPromptText("Developer key");
        initSaveDevKeyBtn();
        initHowTo();
        initRefreshButton();
        reportBtn.setDisable(true);
    }

    private void initHowTo() {
        Image qIcon = new Image("sym_question.gif");
        howToSetDevKey.setGraphic(new ImageView(qIcon));
        Image howToImage = new Image("hlp1.png");
        ImageView howToImageView = new ImageView(howToImage);
        BorderPane bp = new BorderPane();
        bp.setCenter(howToImageView);
        Scene hlpScene = new Scene(bp);
        Stage hlpStage = new Stage();
        hlpStage.setTitle("How to add Developer Key from Test Link");
        hlpStage.getIcons().add(qIcon);
        howToSetDevKey.setOnMousePressed(actionEvent -> {
            if (!hlpStage.isShowing()) {
                hlpStage.setScene(hlpScene);
                hlpStage.showAndWait();
            }
        });
        howToSetDevKey.setOnMouseReleased(actionEvent -> {
            hlpStage.close();
        });
    }

    private void initSubmitButton() {
        reportBtn.setOnAction(actionEvent -> {
            Window mainWindow = main.getScene().getWindow();
            reportStageDialog = new ReportStageDialog();
            reportStageDialog.getIcons().add(new Image("tl-logo.png"));
            reportStageDialog.setParentWindow(mainWindow);
            reportStageDialog.setDialogWindow(800, 600);
            List<String> selectedTests = testPlanView.getSelectionModel().getSelectedItems().stream()
                    .map(TreeItem::getValue).collect(Collectors.toList());
            reportStageDialog.setSelectedTests(selectedTests);
            reportStageDialog.showAndWait();
        });
    }

    private void initRefreshButton(){
        refreshListBtn.setOnAction(actionEvent -> {
            renderTestSuiteTree();
        });
    }


    private void initSaveDevKeyBtn(){
        saveDevKey.setOnAction(actionEvent -> {
            logger.info("saving dev key");
            String devKeyText = devKeyTextField.getText();
            if (devKeyText != null && !devKeyText.isEmpty()) {
                configManager.getConfig().setDevKey(devKeyText);
                configManager.writeConfig(configManager.getConfig().getConfigMap());
                toolManager = ToolManager.resetInstance(devKeyText);
            } else {
                Notifications.create()
                        .title("Warning")
                        .position(Pos.CENTER)
                        .text("Set Dev Key from TestLink/User management/Generate a new key")
                        .showWarning();
            }
        });
    }

    @FXML
    protected void onAppearanceSetProject() {

        testProjectListBox.setVisibleRowCount(10);
        TestProject[] testProjects = toolManager.getAllProjects();
        ObservableList<String> testProjectList = FXCollections.observableArrayList();

        Arrays.stream(testProjects).forEach(tpr -> {
            logger.info("adding Test project: " + tpr.toString());
            testProjectList.add(tpr.getName());
        });
        testProjectListBox.setItems(testProjectList);
        testProjectListBox.setOnAction((event) -> {
            testPlanListBox.setVisibleRowCount(10);
            testPlanListBox.setDisable(true);
            resetStatusNumBar();
            String selectedItem = testProjectListBox.getSelectionModel().getSelectedItem();
            if (selectedItem != null) toolManager.chooseProject(selectedItem);
            logger.info("selected test project = " + selectedItem);
            //TODO get test plan
            testPlanListBox.setDisable(false);

            // set exception listener bonded with runTestPlanListener
            ExceptionListenerCustom exceptionListener = new ExceptionListenerCustom((Stage)main.getScene().getWindow());
            Thread exceptionListenerThread = new Thread(exceptionListener);
            exceptionListenerThread.start();
        });
    }

    @FXML
    protected void onAppearanceSetTestPlan() {
        if (toolManager.getTestProjectApi()!= null && toolManager.getTestProjectApi().getTestProject() != null) {
            testPlanListBox.setVisibleRowCount(5);
            TestPlan[] testPlans = toolManager.getTestProjectApi().getTestPlans();
            ObservableList<String> testPlansList = FXCollections.observableArrayList();
            Arrays.stream(testPlans).forEach(tp -> {
                logger.info("adding testPlan: " + tp.toString());
                testPlansList.add(tp.getName());
            });

            testPlanListBox.setItems(testPlansList);
            testPlanListBox.setOnAction((event) -> {

                String selectedItem = testPlanListBox.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    toolManager.getTestProjectApi().chooseTestPlan(selectedItem);
                    logger.info("selected test plan = " + selectedItem);
                    renderTestSuiteTree();
                } else {
                    logger.error("select project before selection test plan");
                }
            });
        } else {
            testPlanListBox.setDisable(true);
        }
    }

    private void renderTestSuiteTree() {
        GetPlanService getPlanService = new GetPlanService();
        getPlanService.setTestPlanApi(toolManager.getTestProjectApi().getTestPlanApi());
        getPlanService.setSecAbort(30);

        getPlanService.setOnRunning(workerStateEvent -> {
            pb = new ProgressIndicator();
            testSuitsTreePane.getChildren().add(pb);
        });

        getPlanService.setOnSucceeded(workerStateEvent -> {
            Object testSuiteListMap = workerStateEvent.getSource().getValue();

            // check that object from resp - (Map<TestSuite, List<TestCase>>
            // move to helpers
            boolean mapSuitable = false;
            if (testSuiteListMap instanceof Map){
                for (Map.Entry<?, ?> entry : ((Map<?, ?>) testSuiteListMap).entrySet()) {
                    Object k = entry.getKey();
                    Object v = entry.getValue();
                    if (k instanceof TestSuite) {
                        if (v instanceof List) {
                            if (new LinkedList((List) v).pollLast() instanceof TestCase) {
                                mapSuitable = true;
                            }
                        }
                        break;
                    }
                }
            }
            if (mapSuitable){
                buildTestSuitsAndCasesTree((LinkedHashMap<TestSuite, List<TestCase>>) testSuiteListMap);
            } else {
                logger.error("resp from getPlanService on succession is not suitable.");
            }
        });

        getPlanService.setOnCancelled(workerStateEvent -> {
            logger.info("CANCELED");
            getPlanService.reset();
        });


        getPlanService.start();

        setUpdateTestResults();

        initSubmitButton();
        expandListBtn.setText(expandText);
        reportBtn.setDisable(false);
    }

    /* Method to continuously update status of the execution. Works only when TestPlan is selected
        When TestPlan drop dwn is open, update would not performed
    * */
    void setUpdateTestResults(){
        Timeline overTenSecUpdate = new Timeline(new KeyFrame(Duration.seconds(5), (ActionEvent event) -> {
            if (toolManager.getTestProjectApi().getTestPlanApi() != null){
                updateTestCaseExecutionStatus();
            }
        }));
        overTenSecUpdate.setCycleCount(Timeline.INDEFINITE);
        overTenSecUpdate.play();
    }

    @FXML
    public List<String> onGetTestCasesSelected() {
        List<String> chosenItems = testPlanView.getSelectionModel().getSelectedItems().stream()
                .map(TreeItem::getValue).collect(Collectors.toList());
        logger.info("Chosen test cases:");
        for (String chosenItem : chosenItems) {
            logger.info(chosenItem);
        }
        return chosenItems;
    }

    // currently turned off. gives the list of all Test Cases items
//    @FXML
//    protected void onGetTestCasesClick() {
//        assert toolManager != null;
//        TestPlanApi testPlanApi = toolManager.getTestProjectApi().getTestPlanApi();
//        if (testPlanApi.getTestPlanName() != null) {
//            toolManager.getTestProjectApi().getTestPlanApi().getTestCasesAndSetExecutionStatusToTestCaseMap(false);
//            List<String> tcs = testPlanApi.getSummariesAndStatus();
//            ObservableList<String> items = FXCollections.observableArrayList(tcs);
//            tcsNames.setItems(items);
//
//            updateTestCaseExecutionStatus();
//            logger.info("onGetTestCasesClick is done");
//        } else {
//            logger.error("choose test plan before requesting test cases");
//        }
//    }

    private void buildTestSuitsAndCasesTree(LinkedHashMap<TestSuite, List<TestCase>> testSuitesPerTestCases) {

        testCasesTree = createTestCasesTree(testSuitesPerTestCases);
        if (testCasesTree != null) {
            testCasesTree.setExpanded(true);
            testPlanView = new TreeView<>(testCasesTree);
            testPlanView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            testSuitsTreePane.getChildren().add(testPlanView);
            expandListBtn.setDisable(testCasesTree.getChildren().size() <= 0);
        } else {
            expandListBtn.setDisable(true);
        }
        // listener for enabling Report button
        testPlanView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue && newValue.isLeaf()) {
                reportBtn.setDisable(false);
            } else if (newValue != null && oldValue == newValue) {
                reportBtn.setDisable(true);
            } else {
                reportBtn.setDisable(true);
            }
        });
    }

    private TreeItem<String> createTestCasesTree(LinkedHashMap<TestSuite, List<TestCase>> mapWithSummaries) {
        TestPlanApi testPlanApi = toolManager.getTestProjectApi().getTestPlanApi();

        if (testPlanApi != null && testPlanApi.getTestPlanName() != null) {
            String testPlanName = testPlanApi.getTestPlanName();
            TreeItem<String> testCasesTree = new TreeItem<>(testPlanName);

            // mapWithSummaries has test cases with suits and summaries and has all test cases
            // any map from test plan has only execution status and id and has only test cases associated with test plan
            mapWithSummaries = testPlanApi.filterMapWithSummaries(mapWithSummaries);

            mapWithSummaries.forEach((suite, testCases) -> {
                TreeItem<String> suiteItem = new TreeItem<>(suite.getName());

                testCases.forEach(tcInitial -> {
                            TreeItem<String> tcItem = new TreeItem<>(tcInitial.getFullExternalId() + ":" + tcInitial.getName());
                            ImageView execStatusBallImageView = new ImageView(new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/status_ball.png"))));


                            execStatusBallImageView.setFitHeight(18);
                            execStatusBallImageView.setFitWidth(18);

                            if (testPlanApi.getTestCasesFailed().stream().anyMatch(tc -> tc.getId().equals(tcInitial.getId()))) {
                                setColorToImageView(execStatusBallImageView, Color.RED);
                            } else if (testPlanApi.getTestCasesNotRun().stream().anyMatch(tc -> tc.getId().equals(tcInitial.getId()))){
                                setColorToImageView(execStatusBallImageView, Color.GRAY);
                            } else if (testPlanApi.getTestCasesPassed().stream().anyMatch(tc -> tc.getId().equals(tcInitial.getId()))){
                                setColorToImageView(execStatusBallImageView, Color.GREEN);
                            } else if (testPlanApi.getTestCasesBlocked().stream().anyMatch(tc -> tc.getId().equals(tcInitial.getId()))) {
                                setColorToImageView(execStatusBallImageView, Color.BLUE);
                            }

                            tcItem.setGraphic(execStatusBallImageView);

                            suiteItem.getChildren().add(tcItem);
                        }
                );
                testCasesTree.getChildren().add(suiteItem);
            });
            return testCasesTree;
        } else {
            //TODO: set combobox red
            logger.error("set test plan before");
        }
        return null;
    }

    private ImageView setColorToImageView(ImageView execStatusBallImageView, Color color) {
        Lighting lighting = new Lighting(new Light.Distant(45, 90, color));
        ColorAdjust bright = new ColorAdjust(0,1,1,1);
        lighting.setContentInput(bright);
        lighting.setSurfaceScale(0.0);
        execStatusBallImageView.setEffect(lighting);
        return execStatusBallImageView;
    }

    private ImageView getStatusBallImageView(Color color){
        ImageView execStatusBallImageView = new ImageView(
                new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/status_ball.png"))));
        execStatusBallImageView.setFitWidth(18);
        execStatusBallImageView.setFitHeight(18);
        return setColorToImageView(execStatusBallImageView, color);
    }

    synchronized private void updateTestCaseExecutionStatus() {
        TestPlanApi testPlanApi = toolManager.getTestProjectApi().getTestPlanApi();
//        testPlanApi.getTestCasesAndSetExecutionStatusToTestCaseMap(true);
        executionStatusNumsPass.setText("Passed: " + testPlanApi.getTestCasesActualPassedNum());
        executionStatusNumsNotRun.setText("Not run: " + testPlanApi.getTestCasesActualNotRunNum());
        executionStatusNumsFailed.setText("Failed: " + testPlanApi.getTestCasesActualFailedNum());
        executionStatusNumsBlocked.setText("Blocked: " + testPlanApi.getTestCasesActualBlockedNum());
    }

    private void resetStatusNumBar(){
        executionStatusNumsPass.setText("");
        executionStatusNumsNotRun.setText("");
        executionStatusNumsFailed.setText("");
        executionStatusNumsBlocked.setText("");
    }

    @FXML
    public void onExpandList() {
        if (this.testCasesTree != null && this.testCasesTree.getChildren().size() > 0){
            if (expandListBtn.getText().equals(expandText)) {
                testCasesTree.getChildren().forEach(stringTreeItem -> stringTreeItem.setExpanded(true));
                expandListBtn.setText(collapseText);
            } else if (expandListBtn.getText().equals(collapseText)) {
                testCasesTree.getChildren().forEach(stringTreeItem -> stringTreeItem.setExpanded(false));
                expandListBtn.setText(expandText);
            }
        }
    }

    @FXML
    public void onReportResults() {
        ExecutionStatus execStatus = ExecutionStatus.PASSED;

        List<String> chosenItems = testPlanView.getSelectionModel().getSelectedItems().stream()
                .map(TreeItem::getValue).collect(Collectors.toList());
        logger.info("Selected test cases:");
        for (String chosenItem : chosenItems) {
            logger.info(chosenItem);
        }
        List <String> externalFullIds = chosenItems.stream()
                .map(tcStr -> tcStr.substring(0, tcStr.indexOf(":"))).collect(Collectors.toList());

        // may be worth to optimize - find issue why all multiple chosen tests appears in list twice
        // current solution .distinct()
        List<Integer> idsSelected = toolManager.getTestProjectApi().getTestPlanApi().getAllTestCasesFromSuiteTree()
                .stream().filter(tc -> externalFullIds.contains(tc.getFullExternalId()))
                .map(TestCase::getId).distinct().collect(Collectors.toList());

        if (idsSelected.size() > 0) {
            toolManager.getTestProjectApi().getTestPlanApi()
                    .reportResult(ExecutionStatus.PASSED, idsSelected.toArray(Integer[]::new));
        }
    }
}