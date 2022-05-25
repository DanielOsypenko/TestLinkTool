package com.msi.testlinkBack;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import br.eti.kinoshita.testlinkjavaapi.model.TestSuite;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestSuiteAPI {

    TestLinkAPI api;
    TestProjectApi testProjectApi;
    TestPlanApi testPlanApi;

    public TestSuiteAPI(TestLinkAPI testLinkAPI, TestProjectApi testProjectApi, TestPlanApi testPlan)  {
        api = testLinkAPI;
        this.testProjectApi = testProjectApi;
        this.testPlanApi = testPlan;
    }

    public List<TestCase> getTestCasesPerTestSuite(TestSuite testSuite){
        return Arrays.asList(api.getTestCasesForTestSuite(testSuite.getId(), true, null));
    }

    private Map.Entry<TestSuite, List<TestCase>> getTestSuiteEntry(TestSuite testSuite) {
        List<TestCase> tcs = this.getTestCasesPerTestSuite(testSuite);
        return Map.entry(testSuite, tcs);
    }

    public Map<TestSuite, List<TestCase>> getTestSuitesPerTestCases(){
        TestSuite[] testSuits = this.getTestSuits();
        return Arrays.asList(testSuits).parallelStream()
                .map(this::getTestSuiteEntry).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public TestSuite[] getTestSuits() {
//        testPlanApi.chooseTestPlan(testProject.getName());
        return api.getTestSuitesForTestPlan(testPlanApi.getTestPlan().getId());
    }
}
