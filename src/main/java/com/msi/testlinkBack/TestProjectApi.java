package com.msi.testlinkBack;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;

public class TestProjectApi {

    private TestLinkAPI api;
    private TestProject project;
    private String projectName;

    public TestProjectApi(TestLinkAPI api, String projectName) {
        this.api = api;
        this.projectName = projectName;
    }

    public TestProjectApi setProject(String projectName) {
        this.projectName = projectName;
        this.project = api.getTestProjectByName(projectName);
        return this;
    }

    public TestProject getProject() {
        return project;
    }

    public String getProjectName() {
        return projectName;
    }
}
