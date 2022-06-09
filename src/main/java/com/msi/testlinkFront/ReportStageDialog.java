package com.msi.testlinkFront;

import br.eti.kinoshita.testlinkjavaapi.constants.ExecutionStatus;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import com.msi.ConfigManager;
import com.msi.testlinkBack.ToolManager;
import com.msi.testlinkBack.api.TestPlanApi;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.stream.Collectors;

public class ReportStageDialog extends Stage {

    Window parentStage;
    List<String> selectedTests;

    HBox mainBox = new HBox();

    HBox listViewBox = new HBox();

    ListView<String> listView = new ListView<>();

    VBox customFieldsBox = new VBox();

    HBox statusBtnBox = new HBox();
    RadioButton passStatusBtn = new RadioButton("Pass");
    RadioButton failStatusBtn = new RadioButton("Fail");
    RadioButton blockStatusBtn = new RadioButton("Block");
    ToggleGroup executionStatusRBgroup= new ToggleGroup();

    HBox submitBtnDlgBox = new HBox();
    Button submitBtnDlg = new Button("Submit");

    AnchorPane anchorPaneCustomFieldsAndButtons = new AnchorPane();
    ConfigManager configManager = ConfigManager.getInstance();

    HBox notificationTextAreaBox = new HBox();
    TextArea notificationTextArea = new TextArea();


    ExecutionStatus submitStatus = null;
    double padding = 4;
    private static final Logger logger = LoggerFactory.getLogger(ToolManager.class.getSimpleName());

    public ReportStageDialog() {
        // initialize some elements.
        // interface Initializable does not work in app twice, since it works against one FXML
        submitBtnDlg.setOnAction(actionEvent -> {
                    sendReport();
                }
        );
        // read status from buttons
        passStatusBtn.setToggleGroup(executionStatusRBgroup);
        failStatusBtn.setToggleGroup(executionStatusRBgroup);
        blockStatusBtn.setToggleGroup(executionStatusRBgroup);
    }

    public void setSelectedTests(List<String> selectedTests) {
        this.selectedTests = selectedTests;

        ObservableList<String> testsSelectedObservableList = FXCollections.observableArrayList();

        testsSelectedObservableList.addAll(selectedTests);
        listView.setItems(testsSelectedObservableList);

        logger.info("selected test cases:");
        for (String chosenItem : selectedTests) {
            logger.info(chosenItem);
        }
    }

    protected void setDialogWindow(int sceneSizeV, int sceneSizeV1){
        initModality(Modality.APPLICATION_MODAL);
        initOwner(parentStage);

        listViewBox.getChildren().add(listView);

        submitBtnDlgBox.getChildren().add(submitBtnDlg);
        statusBtnBox.setAlignment(Pos.TOP_RIGHT);
        statusBtnBox.getChildren().addAll(passStatusBtn, failStatusBtn, blockStatusBtn, submitBtnDlgBox);

        getRenderForCustomFields();

        notificationTextAreaBox.getChildren().add(notificationTextArea);
        customFieldsBox.getChildren().add(notificationTextAreaBox);
        anchorPaneCustomFieldsAndButtons.getChildren().addAll(customFieldsBox, statusBtnBox);
        mainBox.getChildren().addAll(
                listViewBox,
                anchorPaneCustomFieldsAndButtons
        );

        setTitle("Report results");

        Scene reportDialogScene = new Scene(mainBox, sceneSizeV, sceneSizeV1);
        setStyles(reportDialogScene, sceneSizeV, sceneSizeV1);
        setScene(reportDialogScene);
    }

    void setStyles(Scene reportDialogScene, double mainBoxWidth, double mainBoxHigh){
        reportDialogScene.getStylesheets().add(
                Objects.requireNonNull(Thread.currentThread().getContextClassLoader()
                        .getResource("styles.css")).toExternalForm());
        mainBox.getStyleClass().add("submitDialog");
        mainBox.setMinSize(mainBoxWidth, mainBoxHigh);

        mainBox.getStyleClass().add("statusBtnBox");
        mainBox.getStyleClass().add("submitBtnDlgBox");

        listView.getStyleClass().add("listView");
        listView.setMinHeight(mainBox.getHeight() - padding*2);
        listView.setMaxWidth(mainBoxWidth/2 - padding*1.5);
        listView.setMinWidth(mainBoxWidth/9*5 - padding*1.5);

        customFieldsBox.setMaxWidth(mainBox.getWidth()/2 - padding*1.5) ;
        customFieldsBox.setPadding(new Insets(0d, 0d, 0d, padding));
        customFieldsBox.setMaxHeight(mainBox.getHeight()/2 - padding*2);

        statusBtnBox.setPadding(new Insets(0d, padding, 0d,padding));
        statusBtnBox.setSpacing(4);
        submitBtnDlgBox.setPadding(new Insets(0d, padding, 0d, 80));

        notificationTextArea.setPromptText("Notification");
        notificationTextAreaBox.setPrefSize(180, mainBoxHigh/2);

        AnchorPane.setTopAnchor(customFieldsBox, 0d);
        AnchorPane.setBottomAnchor(statusBtnBox, 0d);
    }

    private VBox getRenderForCustomFields() {
        // read configuration

        configManager.getConfigBack();
        Map<String, String> customFields = configManager.getConfig().getCustomFields();
        // add all custom fields from json. the number and titles of fields are secured in code
        customFields.forEach((k,v)-> {
            HBox oneFieldHBOX = new HBox();
            oneFieldHBOX.setSpacing(padding);
            oneFieldHBOX.getChildren().addAll(new TextField(k), new TextField(v));
            oneFieldHBOX.setPadding(new Insets(padding, padding, padding,padding));
            customFieldsBox.getChildren().add(oneFieldHBOX);
        });
        return customFieldsBox;
    }

    public void setParentWindow(Window parentStage) {
        this.parentStage = parentStage;
    }


    private List<Integer> sendReport() {
        TestPlanApi testPlanApi = ToolManager.getInstance().getTestProjectApi().getTestPlanApi();

        List <String> externalFullIds = selectedTests.stream()
                .map(tcStr -> tcStr.substring(0, tcStr.indexOf(":"))).collect(Collectors.toList());

        // may be worth to optimize - find issue why all multiple chosen tests appears in list twice
        // current solution .distinct()
        List<Integer> idsSelected = testPlanApi.getAllTestCasesFromSuiteTree()
                .stream().filter(tc -> externalFullIds.contains(tc.getFullExternalId()))
                .map(TestCase::getId).distinct().collect(Collectors.toList());

        // get custom fields
        collectNewCustomFields();
        // save new custom fields
        configManager.saveConfig();

        // get status
        if (passStatusBtn.isSelected()){
            submitStatus = ExecutionStatus.PASSED;
        } else if (failStatusBtn.isSelected()){
            submitStatus = ExecutionStatus.FAILED;
        } else if (blockStatusBtn.isSelected()){
            submitStatus = ExecutionStatus.BLOCKED;
        }
        logger.info("status '" + submitStatus.name() + "' will be reported");

        // report
        testPlanApi.reportResult(submitStatus
                , configManager.getConfig().getCustomFields()
                , notificationTextArea.getText()
                , idsSelected.toArray(Integer[]::new)
        );
        return idsSelected;
    }

    private void collectNewCustomFields() {
        ObservableList<Node> customFieldsFront = customFieldsBox.getChildren();
        LinkedHashMap<String, String> customFieldsBack = configManager.getConfig().getCustomFields();
        Set<String> customFieldsKeys = customFieldsBack.keySet();

        for (Object node: customFieldsFront){
            if (node instanceof HBox){
                ObservableList<Node> textFields = ((HBox) node).getChildren();
                for (int i = 0; i < textFields.size(); i++){
                    Object nodeEmb = textFields.get(i);
                    if (nodeEmb instanceof TextField){
                        String textStr = ((TextField) nodeEmb).getText();
                        if (customFieldsKeys.contains(textStr)){
                            logger.info("new custom field is " + textStr + " : " + ((TextField)textFields.get(i+1)).getText());
                            customFieldsBack.put(textStr, ((TextField)textFields.get(i+1)).getText());
                        }
                    }
                }
            }
        }
        configManager.getConfig().setCustomFields(customFieldsBack);
    }


}
