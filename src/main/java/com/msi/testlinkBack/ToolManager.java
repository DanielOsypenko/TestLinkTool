package com.msi.testlinkBack;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.constants.ExecutionStatus;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import br.eti.kinoshita.testlinkjavaapi.util.TestLinkAPIException;
import com.msi.testlinkBack.api.TestProjectApi;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

public class ToolManager {

    private final static String SERVER_URL = "put_link_to_the_server";
    private static String devKey;
    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(ToolManager.class.getSimpleName());

    TestLinkAPI api;
    TestProjectApi testProjectApi;
    String projectName;

    ReentrantLock lock = new ReentrantLock();

    public static ToolManager toolManager;

    private ToolManager(String devKey){
        try {
            this.api = new TestLinkAPI(new URL(SERVER_URL), devKey);
        } catch (TestLinkAPIException | MalformedURLException e) {
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
        }
    }

    public static ToolManager getInstance(String devKey){
        if (toolManager == null){
            toolManager = new ToolManager(devKey);
        }
        return toolManager;
    }

    public static ToolManager resetInstance(String devKey){
        ToolManager.devKey = devKey;
        toolManager = new ToolManager(devKey);
        return toolManager;
    }

    public static String getDevKey() {
        return devKey;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public static ToolManager getInstance(){
      return getInstance(devKey);
    }

    public TestProjectApi chooseProject(String projectName) {
        this.projectName = projectName;
        this.testProjectApi = new TestProjectApi(this.projectName);
        return testProjectApi;
    }

    public TestLinkAPI getApi() {
        return api;
    }

    public TestProject[] getAllProjects() {
        return api.getProjects();
    }

    synchronized public TestProjectApi getTestProjectApi() {
        return testProjectApi;
    }

    public static void main(String[] args) {

        TestProjectApi testProjectApi = ToolManager.getInstance().chooseProject("set_the_project");
        testProjectApi.chooseTestPlan("set_the_test_plan");
        testProjectApi.getTestPlanApi().getTestCasesAndSetExecutionStatusToTestCaseMap(true);
        Instant startTimer = Instant.now();
        Integer[] testIds = new Integer[]{26735, 26737, 26741, 26743,26745};
        testProjectApi.getTestPlanApi().reportResult(ExecutionStatus.PASSED, testIds);

        logger.info("after getSummaries " + new SimpleDateFormat("yyyyMMdd_HH:mm:ss").format(Calendar.getInstance().getTime()) +
                " took " + Duration.between(Instant.now(), startTimer).getSeconds() + "sec");

        logger.info("finish");

    }


}
