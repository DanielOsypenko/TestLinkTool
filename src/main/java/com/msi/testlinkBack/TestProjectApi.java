package com.msi.testlinkBack;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestProjectApi {

    private final TestLinkAPI api;
    private TestProject project;
    private String projectName;
    private static final Logger logger = LoggerFactory.getLogger(TestLinkAPI.class.getSimpleName());

    public TestProjectApi(String projectName) {
        this.api = ToolManager.getManager().getApi();
        this.setProject(projectName);
    }

    public TestProjectApi setProject(String projectName) {
        this.projectName = projectName;
        this.project = api.getTestProjectByName(projectName);
        logger.info("set project " + projectName);
        return this;
    }

    public TestProject getProject() {
        return project;
    }

    public String getProjectName() {
        return projectName;
    }
}
