package com.msi.testlinkBack.api;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import com.msi.testlinkBack.ToolManager;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TestProjectApi {

    private final TestLinkAPI api;
    private TestProject testProject;
    private String projectName;
    private TestPlanApi testPlanApi;
    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(ToolManager.class.getSimpleName());

    public TestProjectApi(String projectName) {
        this.api = ToolManager.getInstance().getApi();
        this.setTestProject(projectName);
    }

    TestProjectApi setTestProject(String projectName) {
        this.projectName = projectName;
        this.testProject = api.getTestProjectByName(projectName);
        logger.info("set project " + projectName);
        return this;
    }

    public TestProject getTestProject() {
        return testProject;
    }

    public String getProjectName() {
        return projectName;
    }

    public TestPlan[] getTestPlans() {
        return api.getProjectTestPlans(testProject.getId());
    }

    public List<String> getTestPlanNames() {
        return Arrays.stream(getTestPlans()).map(TestPlan::getName).collect(Collectors.toList());
    }

    public TestPlanApi chooseTestPlan(String planName) {
        this.testPlanApi = new TestPlanApi();
        this.testPlanApi.setTestPlan(planName);
        Thread testPlanApiListenerThread = new Thread(this.testPlanApi);
        testPlanApiListenerThread.setDaemon(true);
        testPlanApiListenerThread.start();
        return testPlanApi;
    }

    public TestPlanApi getTestPlanApi() {
        return testPlanApi;
    }


}
