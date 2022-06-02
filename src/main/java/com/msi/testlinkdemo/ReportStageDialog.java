package com.msi.testlinkdemo;

import br.eti.kinoshita.testlinkjavaapi.constants.ExecutionStatus;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
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
import java.util.stream.Collectors;

public class ReportStageDialog extends Stage {

    Window parentStage;
    List<String> selectedTests;

    HBox mainBox = new HBox();

    HBox listViewBox = new HBox();
    ListView<String> listView = new ListView<>();

    HBox statusBtnBox = new HBox();
    Button passStatusBtn = new Button("Pass");
    Button failStatusBtn = new Button("Fail");
    Button blockStatusBtn = new Button("Block");

    HBox submitBtnBox = new HBox();
    Button submitBtn = new Button("Submit");


    private static final Logger logger = LoggerFactory.getLogger(ToolManager.class.getSimpleName());

    public ReportStageDialog(List<String> selectedTests) {
        this.selectedTests = selectedTests;
        for (String chosenItem : selectedTests) {
            logger.info(chosenItem);
        }
    }

    protected void setDialogWindow(int sceneSizeV, int sceneSizeV1){
        initModality(Modality.APPLICATION_MODAL);
        initOwner(parentStage);

        ObservableList<String> testsSelectedObservableList = FXCollections.observableArrayList();

        testsSelectedObservableList.addAll(selectedTests);
        listView.setItems(testsSelectedObservableList);
        listViewBox.getChildren().add(listView);

        statusBtnBox.getChildren().addAll(passStatusBtn, failStatusBtn, blockStatusBtn);
        statusBtnBox.setAlignment(Pos.BOTTOM_LEFT);

        submitBtnBox.getChildren().add(submitBtn);
        submitBtnBox.setAlignment(Pos.BASELINE_RIGHT);

        mainBox.getChildren().addAll(listViewBox, statusBtnBox, submitBtnBox);
        setTitle("Submit results");
//        dialogBox.getChildren().add(new TextField("Submit results"));
        Scene reportDialogScene = new Scene(mainBox, sceneSizeV, sceneSizeV1);

        setScene(reportDialogScene);
        show();
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
