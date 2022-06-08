package com.msi.testlinkBack;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.constants.ExecutionStatus;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import com.msi.testlinkBack.api.TestProjectApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.locks.ReentrantLock;

public class ToolManager {

    private final static String SERVER_URL = "http://testlink.watchguardvideo.local/lib/api/xmlrpc/v1/xmlrpc.php";
    private final static String DEV_KEY = "7f2baca03137da97cb6358d62737d0bd";
    private static final Logger logger = LoggerFactory.getLogger(ToolManager.class.getSimpleName());

    TestLinkAPI api;
    TestProjectApi testProjectApi;
    String projectName;

    ReentrantLock lock = new ReentrantLock();

    public static ToolManager toolManager;

    private ToolManager(){
        try {
            api = new TestLinkAPI(new URL(SERVER_URL), DEV_KEY);
        } catch (MalformedURLException e) {
            logger.error(Arrays.toString(e.getStackTrace()));
        }
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public static ToolManager getInstance(){
        if (toolManager == null){
            toolManager = new ToolManager();
        }
        return toolManager;
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

        TestProjectApi testProjectApi = ToolManager.getInstance().chooseProject("G3INCAR");
        testProjectApi.chooseTestPlan("ECN Automation");
        testProjectApi.getTestPlanApi().getTestCasesAndSetExecutionStatusToTestCaseMap(true);
        Instant startTimer = Instant.now();
        Integer[] testIds = new Integer[]{26735, 26737, 26741, 26743,26745};
        testProjectApi.getTestPlanApi().reportResult(ExecutionStatus.PASSED, testIds);
//        ToolManager.getManager().api.reportTCResult(null,G3INCAR-TC-1807)


//
//        logger.info("test cases not run:" + testProjectApi.getTestPlanApi().getTestCasesActualFailedNum());
//        logger.info("test cases passed:" + testProjectApi.getTestPlanApi().getTestCasesActualPassedNum());
//        logger.info("test cases failed:" + testProjectApi.getTestPlanApi().getTestCasesActualFailedNum());
//        logger.info("test cases blocked:" + testProjectApi.getTestPlanApi().getTestCasesActualBlockedNum());
//
        logger.info("after getSummaries " + new SimpleDateFormat("yyyyMMdd_HH:mm:ss").format(Calendar.getInstance().getTime()) +
                " took " + Duration.between(Instant.now(), startTimer).getSeconds() + "sec");
//




        logger.info("finish");

    }



    // ============================= ============================= ============================= ==========================//
//    public List<String> getSummariesAndStatus()  {
//
//
//        Instant startTimer = Instant.now();
//
//        logger.info("before getTestCasesFailed " + new SimpleDateFormat("yyyyMMdd_HH:mm:ss").format(Calendar.getInstance().getTime()));
//        getTestCases();
////        List<String> ids = Arrays.stream(g3INCAR_api.testCasesActual).map(TestCase::getFullExternalId).collect(Collectors.toList());
//        Map<Integer, String> ids = testCasesActual.stream().collect(Collectors.toMap(TestCase::getId, TestCase::getFullExternalId));
//        logger.info("after getTestCasesFailed " + new SimpleDateFormat("yyyyMMdd_HH:mm:ss").format(Calendar.getInstance().getTime()));
//        Map<String, TestCase> summaries = getSummaries();
//        summaries.forEach((key, value) -> logger.info(key + ":" + value.toString()));
////        logger.info("after getSummaries " + new SimpleDateFormat("yyyyMMdd_HH:mm:ss").format(Calendar.getInstance().getTime()) +
////                " took " + Duration.between(Instant.now(), startTimer).getSeconds() + "sec");
//        return summaries.values().stream().map(TestCase::toString).collect(Collectors.toList());
//    }


}
