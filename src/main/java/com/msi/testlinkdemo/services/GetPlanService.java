package com.msi.testlinkdemo.services;

import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestSuite;
import com.msi.testlinkBack.api.TestPlanApi;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.List;
import java.util.Map;

public class GetPlanService extends Service<Map<TestSuite, List<TestCase>>> {

    TestPlanApi testPlanApi;

    public final void setTestPlanApi(TestPlanApi testPlanApi) {
        this.testPlanApi = testPlanApi;
    }

    @Override
    protected Task<Map<TestSuite, List<TestCase>>> createTask() {
        return new Task<>() {
            @Override
            protected Map<TestSuite, List<TestCase>> call() {
                return testPlanApi.getTestSuitesPerTestCases();
            }
        };
    }
}
