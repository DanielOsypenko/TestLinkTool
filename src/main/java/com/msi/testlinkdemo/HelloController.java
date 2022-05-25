package com.msi.testlinkdemo;

import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import com.msi.testlinkBack.API;
import com.msi.testlinkBack.TestProjectApi;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class HelloController {
    private static final Logger logger = LoggerFactory.getLogger(API.class.getSimpleName());

    API testLinkTool;
    TestProjectApi testProjectApi;

    public HelloController() throws MalformedURLException {
        try {
            testLinkTool = new API();
        } catch (MalformedURLException e) {
            logger.error(Arrays.toString(Arrays.stream(e.getStackTrace()).toArray()));
        }
        testLinkTool = new API();
    }

    @FXML
    private ComboBox<String> testProjectListBox;

    @FXML
    private ComboBox<String> testPlanListBox;

    @FXML
    public TextField executionStatusNums;

    @FXML
    private ListView<String> tcsNames;

//    @FXML
//    TreeView<String> testPlanView;

    @FXML
    StackPane testSuitsTreePane;

    @FXML
    protected void onAppearanceSetProject() {
        testProjectListBox.setVisibleRowCount(5);
        TestProject[] testProjects = testLinkTool.getAllProjects();
        ObservableList<String> testProjectList = FXCollections.observableArrayList();

        Arrays.stream(testProjects).forEach(tpr -> {
            logger.info("adding Test project: " + tpr.toString());
            testProjectList.add(tpr.getName());
        });
        testProjectListBox.setItems(testProjectList);
        testProjectListBox.setOnAction((event) -> {
            String selectedItem = testProjectListBox.getSelectionModel().getSelectedItem();
            if (selectedItem != null) testLinkTool.chooseProject(selectedItem);
            logger.info("selected test project = " + selectedItem);
            //TODO get test plan
        });

//        TestProject[] testPlans = testLinkTool.getAllProjects();
//        ObservableList<String> testPlansList = FXCollections.observableArrayList();
//        Arrays.stream(testPlans).forEach(tp -> {
//            logger.info("adding testPlan: " + tp.toString());
//            testPlansList.add(tp.getName());
//        });
//        tcPerTestSuite.setItems(testPlansList);
//        tcPerTestSuite.setOnAction((event) -> {
//            tcPerTestSuite.getSelectionModel().getSelectedIndex();
//            String selectedItem = tcPerTestSuite.getSelectionModel().getSelectedItem();
//            if (selectedItem != null) testLinkTool.chooseTestPlan(selectedItem);
//            logger.info("selected test plan="+selectedItem);
//            updateTestCaseExecutionStatus();
//        });

    }

    @FXML
    protected void onAppearanceSetTestPlan() {
        testPlanListBox.setVisibleRowCount(5);
        TestPlan[] testPlans = testLinkTool.getTestPlans();
        ObservableList<String> testPlansList = FXCollections.observableArrayList();
        Arrays.stream(testPlans).forEach(tp -> {
            logger.info("adding testPlan: " + tp.toString());
            testPlansList.add(tp.getName());
        });
        testPlanListBox.setItems(testPlansList);
        testPlanListBox.setOnAction((event) -> {
            String selectedItem = testPlanListBox.getSelectionModel().getSelectedItem();
            if (selectedItem != null) testLinkTool.chooseTestPlan(selectedItem);
            logger.info("selected test plan = " + selectedItem);
            updateTestCaseExecutionStatus();
        });
    }



    @FXML
    protected void onGetTestCasesClick() {
        assert testLinkTool != null;
//        g3INCAR_api.chooseTestPlan("Manual ECN - 1.0.12");
        if (testLinkTool.getTestPlanName() != null) {
            testLinkTool.getTestCasesToStatusMap();
            List<String> tcs = testLinkTool.getSummariesAndStatus();
            ObservableList<String> items = FXCollections.observableArrayList(tcs);
            tcsNames.setItems(items);

            updateTestCaseExecutionStatus();
            logger.info("onGetTestCasesClick is done");
        } else {
            logger.error("choose test plan before requesting test cases");
        }
    }

    @FXML
    protected void onGetTestSuitsAndCasesClick(){
        assert testLinkTool != null;

        Map<String, String[]> testSuitesPerTestCases = testLinkTool.getTestSuitesPerTestCasesCustomStr();
//        logger.info("num of test suits="+testSuitesPerTestCases.entrySet().size());
//        logger.info("Found suites num= " + testSuitesPerTestCases.keySet().size());
//
//        testPlanView = new TableView<>();
//        testPlanView.getItems().addAll(testSuitesPerTestCases.keySet());
//
//        TableColumn<String, String> keyColumn = new TableColumn<>("TestSuite");
//        keyColumn.setCellValueFactory(cd -> new SimpleStringProperty("TestSuite"));
//
//        TableColumn<String, String> valueColumn = new TableColumn<>("TestCase");
//        valueColumn.setCellValueFactory(cd -> new SimpleStringProperty("TestCase"));
        TreeItem<String> testCasesTree = createTestCasesTree(testSuitesPerTestCases);
        if (testCasesTree != null) {
            TreeView<String> testPlanView = new TreeView<>(createTestCasesTree(testSuitesPerTestCases));
            testSuitsTreePane.getChildren().add(testPlanView);
            updateTestCaseExecutionStatus();
        }

    }


    private  TreeItem<String> createTestCasesTree(Map<String, String[]> map){
        String testPlanName = testLinkTool.getTestPlanName();
        if (testPlanName != null) {
            TreeItem<String> testCasesTree = new TreeItem<>(testLinkTool.getTestPlanName());
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

    private void updateTestCaseExecutionStatus(){
        testLinkTool.getTestCasesToStatusMap();
        executionStatusNums.setText("Passed: " + testLinkTool.getTestCasesActualPassedNum() +
                " Not run: " + testLinkTool.getTestCasesActualNotRunNum() +
                " Failed: " + testLinkTool.getTestCasesActualFailedNum() +
                " Blocked: " + testLinkTool.getTestCasesActualBlockedNum());
    }

    /// Attempt to use Table  ///

//    private static TreeItem<MapItem> createTree(Map<String, Object> map) {
//        TreeItem<MapItem> result = new TreeItem<>();
//
//        for (Map.Entry<String, Object> entry : map.entrySet()) {
//            result.getChildren().add(createTree(map, entry));
//        }
//
//        return result;
//    }

//    private static TreeItem<MapItem> createTree(Map<String, Object> map, Map.Entry<String, Object> entry) {
//        MapItem mi = new MapItem(map, entry.getKey());
//        TreeItem<MapItem> result = new TreeItem<>(mi);
//
//        Object value = entry.getValue();
//
//        if (value instanceof Map) {
//            Map<String, Object> vMap = (Map<String, Object>)value;
//
//            // recursive creation of subtrees for map entries
//            for (Map.Entry<String, Object> e : vMap.entrySet()) {
//                result.getChildren().add(createTree(vMap, e));
//            }
//        } else {
//            result.getChildren().add(new TreeItem<>(new MapItem(null, value.toString())));
//        }
//
//        return result;
//    }
//
//    private static class MapItem {
//
//        private final Map<String, Object> map;
//        private final String value;
//
//        public MapItem(Map<String, Object> map, String value) {
//            this.map = map;
//            this.value = value;
//        }
//    }

//    private static class Converter extends StringConverter<MapItem> {
//
//        private final TreeCell<MapItem> cell;
//
//        public Converter(TreeCell<MapItem> cell) {
//            this.cell = cell;
//        }
//
//        @Override
//        public String toString(MapItem object) {
//            return object == null ? null : object.value;
//        }
//
//        @Override
//        public MapItem fromString(String string) {
//            MapItem mi = cell.getItem();
//
//            if (mi != null) {
//                TreeItem<MapItem> item = cell.getTreeItem();
//                if (item.isLeaf()) {
//                    MapItem parentItem = item.getParent().getValue();
//
//                    // modify value in parent map
//                    parentItem.map.put(parentItem.value, string);
//                    mi = new MapItem(mi.map, string);
//                } else if (!mi.map.containsKey(string)) {
//                    // change key of mapping, if there is no mapping for the new key
//                    mi.map.put(string, mi.map.remove(mi.value));
//                    mi = new MapItem(mi.map, string);
//                }
//            }
//
//            return mi;
//        }
//
//    }
}