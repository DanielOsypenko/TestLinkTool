package com.msi.testlinkBack.api;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import com.msi.testlinkBack.ToolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TestProjectApi {

    private final TestLinkAPI api;
    private TestProject project;
    private String projectName;
    private TestPlanApi testPlanApi;
    private static final Logger logger = LoggerFactory.getLogger(TestLinkAPI.class.getSimpleName());

    public TestProjectApi(String projectName) {
        this.api = ToolManager.getManager().getApi();
        this.setProject(projectName);
    }

    TestProjectApi setProject(String projectName) {
        this.projectName = projectName;
        this.project = api.getTestProjectByName(projectName);
        logger.info("set project " + projectName);
        return this;
    }

    TestProject getProject() {
        return project;
    }

    String getProjectName() {
        return projectName;
    }

    public TestPlan[] getTestPlans() {
        return api.getProjectTestPlans(project.getId());
    }

    public List<String> getTestPlanNames() {
        return Arrays.stream(getTestPlans()).map(TestPlan::getName).collect(Collectors.toList());
    }

    public TestPlanApi chooseTestPlan(String planName){
        this.testPlanApi = new TestPlanApi();
        return this.testPlanApi.setTestPlan(planName);
    }

    public TestPlanApi getTestPlanApi() {
        return testPlanApi;
    }


}
