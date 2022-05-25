package com.msi.testlinkdemo;

import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import com.msi.testlinkBack.TestPlanApi;
import com.msi.testlinkBack.ToolManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class HelloController {
    private static final Logger logger = LoggerFactory.getLogger(ToolManager.class.getSimpleName());

    ToolManager toolManager;

    public HelloController() {
        toolManager = ToolManager.getManager();
    }

    // TODO: place testProjectListBox and testPlanListBox on the same line
    @FXML
    private ComboBox<String> testProjectListBox;

    @FXML
    private ComboBox<String> testPlanListBox;

    @FXML
    public TextField executionStatusNums;

    @FXML
    private ListView<String> tcsNames;

    @FXML
    StackPane testSuitsTreePane;

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
            String selectedItem = testProjectListBox.getSelectionModel().getSelectedItem();
            if (selectedItem != null) toolManager.chooseProject(selectedItem);
            logger.info("selected test project = " + selectedItem);
            //TODO get test plan
        });
    }

    @FXML
    protected void onAppearanceSetTestPlan() {
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
            if (selectedItem != null) toolManager.getTestProjectApi().chooseTestPlan(selectedItem);
            logger.info("selected test plan = " + selectedItem);
            updateTestCaseExecutionStatus();
        });
    }


    @FXML
    protected void onGetTestCasesClick() {
        assert toolManager != null;
        TestPlanApi testPlanApi = toolManager.getTestProjectApi().getTestPlanApi();
        if (testPlanApi.getTestPlanName() != null) {
            toolManager.getTestProjectApi().getTestPlanApi().getTestCasesToStatusMap();
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
        Map<String, String[]> testSuitesPerTestCases = toolManager
                .getTestProjectApi()
                .getTestPlanApi()
                .getTestSuitesPerTestCasesCustomStr();
        TreeItem<String> testCasesTree = createTestCasesTree(testSuitesPerTestCases);
        if (testCasesTree != null) {
            TreeView<String> testPlanView = new TreeView<>(createTestCasesTree(testSuitesPerTestCases));
            testSuitsTreePane.getChildren().add(testPlanView);
            updateTestCaseExecutionStatus();
        }

    }


    private TreeItem<String> createTestCasesTree(Map<String, String[]> map) {
        TestPlanApi testPlanApi = toolManager.getTestProjectApi().getTestPlanApi();
        String testPlanName = testPlanApi.getTestPlanName();
        if (testPlanName != null) {
            TreeItem<String> testCasesTree = new TreeItem<>(testPlanName);
            map.forEach((suite, value) -> {
                TreeItem<String> suiteItem = new TreeItem<>(suite);
                Arrays.stream(value).forEach(tc -> suiteItem.getChildren().add(new TreeItem<>(tc))
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

    private void updateTestCaseExecutionStatus() {
        TestPlanApi testPlanApi = toolManager.getTestProjectApi().getTestPlanApi();
        testPlanApi.getTestCasesToStatusMap();
        executionStatusNums.setText("Passed: " + testPlanApi.getTestCasesActualPassedNum() +
                " Not run: " + testPlanApi.getTestCasesActualNotRunNum() +
                " Failed: " + testPlanApi.getTestCasesActualFailedNum() +
                " Blocked: " + testPlanApi.getTestCasesActualBlockedNum());
    }

}