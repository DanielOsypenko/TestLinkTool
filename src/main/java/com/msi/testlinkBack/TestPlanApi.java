package com.msi.testlinkBack;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;

public class TestPlanApi {

    private TestLinkAPI api;
    private TestPlan testPlan;
    private TestProjectApi testProjectApi;
    private int testPlanId;
    private String testPlanName;
    private String projectName;

    public TestPlanApi(TestLinkAPI api, TestProjectApi testProjectApi, String projectName) {
        this.api = api;
        this.testProjectApi = testProjectApi;
        this.projectName = projectName;
    }

    public TestPlanApi setTestPlan(String planName) {
        this.testPlan = api.getTestPlanByName(planName, projectName);
        this.testPlanId = testPlan.getId();
        this.testPlanName = planName;
        return this;
    }

    public TestPlan getTestPlan() {
        return testPlan;
    }


}
