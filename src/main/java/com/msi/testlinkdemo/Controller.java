package com.msi.testlinkdemo;

import br.eti.kinoshita.testlinkjavaapi.constants.ExecutionStatus;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import br.eti.kinoshita.testlinkjavaapi.model.TestSuite;
import com.msi.testlinkBack.ToolManager;
import com.msi.testlinkBack.api.TestPlanApi;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.controlsfx.control.textfield.CustomTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;


public class Controller implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(ToolManager.class.getSimpleName());

    ToolManager toolManager;
    public Controller() {
        toolManager = ToolManager.getManager();
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

    @FXML
    public Button getTestSuitesBtn;

    @FXML
    public Button getTestCasesSelectedBtn;

    String expandText = "Expand";
    String collapseText = "Collapse";

    @FXML
    public Button expandListBtn;

    @FXML
    public Button reportBtn;

//    @FXML
//    public TextField executionStatusNums;

    @FXML
    public CustomTextField executionStatusNumsPass;

    @FXML
    public CustomTextField executionStatusNumsFailed;

    @FXML
    public CustomTextField executionStatusNumsNotRun;

    @FXML
    public CustomTextField executionStatusNumsBlocked;

    @FXML
    private ListView<String> tcsNames;

    @FXML
    private StackPane testSuitsTreePane;
    TreeItem<String> testCasesTree;

    private TreeView<String> testPlanView;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        testPlanListBox.setDisable(true);
        getTestSuitesBtn.setDisable(true);
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


        reportBtn.setDisable(true);
    }

    @FXML
    protected void onAppearanceSetProject() {

        testProjectListBox.setVisibleRowCount(5);
        TestProject[] testProjects = toolManager.getAllProjects();
        ObservableList<String> testProjectList = FXCollections.observableArrayList();

        Arrays.stream(testProjects).forEach(tpr -> {
            logger.info("adding Test project: " + tpr.toString());
            testProjectList.add(tpr.getName());
        });
        testProjectListBox.setItems(testProjectList);
        testProjectListBox.setOnAction((event) -> {
            testPlanListBox.setDisable(true);
            refreshStatusNumBar();
            String selectedItem = testProjectListBox.getSelectionModel().getSelectedItem();
            if (selectedItem != null) toolManager.chooseProject(selectedItem);
            logger.info("selected test project = " + selectedItem);
            //TODO get test plan
            testPlanListBox.setDisable(false);
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

                // TODO: remove previous valuefrom the list when new Project was selected

               // testPlanListBox.setDisable(false);
                String selectedItem = testPlanListBox.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    toolManager.getTestProjectApi().chooseTestPlan(selectedItem);
                    logger.info("selected test plan = " + selectedItem);
                    updateTestCaseExecutionStatus();
                    getTestSuitesBtn.setDisable(false);
                } else {
                    getTestSuitesBtn.setDisable(true);
                }
            });
        } else {
            testPlanListBox.setDisable(true);
        }
    }

    @FXML
    public List<String> onGetTestCasesSelected() {
//        testPlanView.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
//            logger.info(">>>>>>>>>>>>>>>>>> 1");
//
//            if(newValue != null && newValue != oldValue){
//                logger.info("Hello World");
//            }

//            if (!oldValue.equals(newValue)) {
//                reportBtn.setDisable(false);
//                logger.info(">>>>>>>>>>>>>>>>>> 2");
//            } else {
//                reportBtn.setDisable(true);
//            }
//        });
        List<String> chosenItems = testPlanView.getSelectionModel().getSelectedItems().stream()
                .map(TreeItem::getValue).collect(Collectors.toList());
        logger.info("Chosen test cases:");
        for (String chosenItem : chosenItems) {
            logger.info(chosenItem);
        }
        return chosenItems;
    }

    // currently turned off. gives the list of all Test Cases items
    @FXML
    protected void onGetTestCasesClick() {
        assert toolManager != null;
        TestPlanApi testPlanApi = toolManager.getTestProjectApi().getTestPlanApi();
        if (testPlanApi.getTestPlanName() != null) {
            toolManager.getTestProjectApi().getTestPlanApi().getTestCasesAndSetExecutionStatusToTestCaseMap(false);
            List<String> tcs = testPlanApi.getSummariesAndStatus();
            ObservableList<String> items = FXCollections.observableArrayList(tcs);
            tcsNames.setItems(items);

            updateTestCaseExecutionStatus();
            logger.info("onGetTestCasesClick is done");
        } else {
            logger.error("choose test plan before requesting test cases");
        }
    }

    @FXML
    protected void onGetTestSuitsAndCasesClick() {
        assert toolManager != null;
        Map<TestSuite, List<TestCase>> testSuitesPerTestCases = toolManager
                .getTestProjectApi()
                .getTestPlanApi()
                .getTestSuitesPerTestCases();

        testCasesTree = createTestCasesTree(testSuitesPerTestCases);
        if (testCasesTree != null) {
            testCasesTree.setExpanded(true);
            testPlanView = new TreeView<>(testCasesTree);
            testPlanView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            testSuitsTreePane.getChildren().add(testPlanView);
            updateTestCaseExecutionStatus();
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

    private TreeItem<String> createTestCasesTree(Map<TestSuite, List<TestCase>> mapWithSummaries) {
        TestPlanApi testPlanApi = toolManager.getTestProjectApi().getTestPlanApi();

        String testPlanName = testPlanApi.getTestPlanName();
        if (testPlanName != null) {
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

    private void updateTestCaseExecutionStatus() {
        TestPlanApi testPlanApi = toolManager.getTestProjectApi().getTestPlanApi();
        testPlanApi.getTestCasesAndSetExecutionStatusToTestCaseMap(true);
//        executionStatusNums.setText("Passed: " + testPlanApi.getTestCasesActualPassedNum() +
//                " Not run: " + testPlanApi.getTestCasesActualNotRunNum() +
//                " Failed: " + testPlanApi.getTestCasesActualFailedNum() +
//                " Blocked: " + testPlanApi.getTestCasesActualBlockedNum());

        executionStatusNumsPass.setText("Passed: " + testPlanApi.getTestCasesActualPassedNum());
        executionStatusNumsNotRun.setText("Not run: " + testPlanApi.getTestCasesActualNotRunNum());
        executionStatusNumsFailed.setText("Failed: " + testPlanApi.getTestCasesActualFailedNum());
        executionStatusNumsBlocked.setText("Blocked: " + testPlanApi.getTestCasesActualBlockedNum());
    }

    private void refreshStatusNumBar(){
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