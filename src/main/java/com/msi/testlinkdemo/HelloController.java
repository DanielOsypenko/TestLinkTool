package com.msi.testlinkdemo;

import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import com.msi.testlinkBack.G3INCAR_API;
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
    private static final Logger logger = LoggerFactory.getLogger(G3INCAR_API.class.getSimpleName());
    G3INCAR_API g3INCAR_api;

    public HelloController() {
        try {
            g3INCAR_api = new G3INCAR_API().chooseProject("G3INCAR");
        } catch (MalformedURLException e) {
            logger.error(Arrays.toString(Arrays.stream(e.getStackTrace()).toArray()));
        }
    }

    @FXML
    private ComboBox<String> testPlanBox;

    @FXML
    private ListView<String> tcsNames;

//    @FXML
//    TreeView<String> testPlanView;
    @FXML
    StackPane root;

    @FXML
    protected void onAppearance() {
        testPlanBox.setVisibleRowCount(5);
        TestPlan[] testPlans = g3INCAR_api.getTestPlans();
        ObservableList<String> testPlansList = FXCollections.observableArrayList();
        Arrays.stream(testPlans).forEach(tp -> {
            logger.info("adding testPlan: " + tp.toString());
            testPlansList.add(tp.getName());
        });
        testPlanBox.setItems(testPlansList);
        testPlanBox.setOnAction((event) -> {
            testPlanBox.getSelectionModel().getSelectedIndex();
            String selectedItem = testPlanBox.getSelectionModel().getSelectedItem();
            if (selectedItem != null) g3INCAR_api.chooseTestPlan(selectedItem);
            logger.info("selected test plan="+selectedItem);
        });
    }



    @FXML
    protected void onGetTestCasesClick() {
        assert g3INCAR_api != null;
        g3INCAR_api.chooseTestPlan("Manual ECN - 1.0.12");
        g3INCAR_api.getTestCases();
        List<String> tcs = g3INCAR_api.getSummariesAndStatus();
        ObservableList<String> items = FXCollections.observableArrayList(tcs);
        tcsNames.setItems(items);
        logger.info("onGetTestCasesClick is done");
    }

    @FXML
    protected void onGetTestSuitsAndCasesClick(){
        assert g3INCAR_api != null;

        Map<String, String[]> testSuitesPerTestCases = g3INCAR_api.getTestSuitesPerTestCasesCustomStr();
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
            root.getChildren().add(testPlanView);

        }

    }


    private  TreeItem<String> createTestCasesTree(Map<String, String[]> map){
        String testPlanName = g3INCAR_api.getTestPlanName();
        if (testPlanName != null) {
            TreeItem<String> testCasesTree = new TreeItem<>(g3INCAR_api.getTestPlanName());
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