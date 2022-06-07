package com.msi.testlinkFront;

import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import com.msi.ConfigManager;
import com.msi.testlinkBack.ToolManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ReportStageDialog extends Stage {

    Window parentStage;
    List<String> selectedTests;

    HBox mainBox = new HBox();

    HBox listViewBox = new HBox();

    ListView<String> listView = new ListView<>();

    VBox customFieldsBox = new VBox();

    HBox statusAndSubmitBtnBox = new HBox();
    Button passStatusBtn = new Button("Pass");
    Button failStatusBtn = new Button("Fail");
    Button blockStatusBtn = new Button("Block");

    Button submitBtn = new Button("Submit");


    private static final Logger logger = LoggerFactory.getLogger(ToolManager.class.getSimpleName());

    public ReportStageDialog() {
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

        statusAndSubmitBtnBox.getChildren().addAll(passStatusBtn, failStatusBtn, blockStatusBtn);
        submitBtn.setAlignment(Pos.BOTTOM_RIGHT);
        statusAndSubmitBtnBox.getChildren().add(submitBtn);
        statusAndSubmitBtnBox.setAlignment(Pos.TOP_RIGHT);

        VBox customFieldsBox = getCustomFields();
        customFieldsBox.getChildren().addAll(statusAndSubmitBtnBox);
        setStyles();

        mainBox.getChildren().addAll(listViewBox,  customFieldsBox);
        setTitle("Submit results");

        Scene reportDialogScene = new Scene(mainBox, sceneSizeV, sceneSizeV1);

        setScene(reportDialogScene);

    }

    void setStyles(){
        double mainBoxWidth = 640;
        double mainBoxHigh = 480;
        mainBox.getStylesheets().add(
                Objects.requireNonNull(mainBox.getClass().getResource("/styles.css")).toExternalForm());
        mainBox.getStyleClass().add("submitDialog");
        mainBox.setMinSize(mainBoxWidth, mainBoxHigh);
        listViewBox.getStyleClass().add("selectedTestList");
        listViewBox.setMinHeight(mainBox.getHeight());
        listViewBox.setMinWidth(mainBoxWidth/2);

        listView.setMinWidth(mainBoxWidth/2);

        customFieldsBox.setMinHeight(mainBox.getHeight());

        statusAndSubmitBtnBox.getStyleClass().add("statusBtnBox");
        submitBtn.getStyleClass().add("submitBtnFromDialog");
    }


    private VBox getCustomFields() {
        // read configuration
        ConfigManager configManager = new ConfigManager();
        configManager.processConfig();
        Map<String, String> customFields = ConfigManager.Config.customFields;

        // add all custom fields from json. the number and titles of fields are secured in code

        customFields.forEach((k,v)-> {
            HBox oneFieldHBOX = new HBox();
            oneFieldHBOX.getChildren().addAll(new TextField(k), new TextField(v));
            customFieldsBox.getChildren().add(oneFieldHBOX);
        });
        return customFieldsBox;
    }

    public void setParentWindow(Window parentStage) {
        this.parentStage = parentStage;
    }

    public void reportResluts(){
        ToolManager toolManager = ToolManager.getManager();




//        List<Integer> idsSelected = sendReport();
//
//        if (idsSelected.size() > 0) {
//            toolManager.getTestProjectApi().getTestPlanApi()
//                    .reportResult(ExecutionStatus.PASSED, idsSelected.toArray(Integer[]::new));
//        }
    }

    private List<Integer> sendReport() {
        List <String> externalFullIds = selectedTests.stream()
                .map(tcStr -> tcStr.substring(0, tcStr.indexOf(":"))).collect(Collectors.toList());

        // may be worth to optimize - find issue why all multiple chosen tests appears in list twice
        // current solution .distinct()
        List<Integer> idsSelected = ToolManager.getManager().getTestProjectApi().getTestPlanApi().getAllTestCasesFromSuiteTree()
                .stream().filter(tc -> externalFullIds.contains(tc.getFullExternalId()))
                .map(TestCase::getId).distinct().collect(Collectors.toList());
        return idsSelected;
    }
}
