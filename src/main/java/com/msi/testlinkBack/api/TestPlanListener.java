package com.msi.testlinkBack.api;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.constants.ExecutionStatus;
import br.eti.kinoshita.testlinkjavaapi.model.Build;
import br.eti.kinoshita.testlinkjavaapi.model.ReportTCResultResponse;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.util.TestLinkAPIException;
import com.msi.testlinkBack.ToolManager;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class TestPlanListener implements Runnable{

    protected TestLinkAPI api;
    protected TestProjectApi testProjectApi;
    protected TestPlan testPlan;
    protected Integer testPlanId;
    protected String testPlanName;
    protected Map<ExecutionStatus, List<TestCase>> testCasesToStatusMap;
    protected List<TestCase> testCasesActual = null;
    protected List<TestCase> testCasesActualNotRun = null;
    protected List<TestCase> testCasesActualPassed = null;
    protected List<TestCase> testCasesActualBlocked = null;
    protected List<TestCase> testCasesActualFailed = null;
    protected int testCasesActualNum;
    protected int testCasesActualNotRunNum;
    protected int testCasesActualPassedNum;
    protected int testCasesActualBlockedNum;
    protected int testCasesActualFailedNum;
    protected Build[] builds;
    protected Integer buildIdLast;
//    ConcurrentLinkedQueue<ReportTCResultResponse> reportTCResultResponse = new ConcurrentLinkedQueue<>();

    static final Logger logger = LoggerFactory.getLogger(TestPlanListener.class.getSimpleName());

    public TestPlanListener() {
        ToolManager manager = ToolManager.getManager();
        this.api = manager.getApi();
        this.testProjectApi = manager.getTestProjectApi();
    }

    public TestPlan getTestPlan() {
        return testPlan;
    }

    public int getTestPlanId() {
        return testPlanId;
    }

    synchronized public String getTestPlanName() {
        return testPlanName;
    }

    @Override
    public void run() {

        logger.info("start updating test plan: " + getTestPlanName());
        while (testProjectApi != null){

            getTestCasesAndSetExecutionStatusToTestCaseMap(true);
//            try {
//                // TODO - remove getTestSuits, make it automatically after choosing test plan, add progress spinner
//                getTestCasesAndSetExecutionStatusToTestCaseMap(true);
//            } catch (NullPointerException e) {
//                logger.info(e.getMessage());
//            }
            sleep(5);
        }
    }

    private void sleep(long sec){
        try {
            Thread.sleep(sec*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public TestPlanListener setTestPlan(String planName) {
        this.testPlan = api.getTestPlanByName(planName, testProjectApi.getProjectName());
        this.testPlanId = testPlan.getId();
        this.testPlanName = testPlan.getName();
        Executors.newCachedThreadPool().execute(() -> this.setBuilds(api.getBuildsForTestPlan(this.testPlanId)));

//        Thread testPlanListenerThread = new Thread(this);
//        testPlanListenerThread.start();
        return this;
    }

    public void setBuilds(Build[] builds) {
        this.builds = builds;
        if (this.builds != null && this.builds.length > 0){
            this.buildIdLast = builds[builds.length-1].getId();
            logger.info("builds for Test plan '" + this.getTestPlanName() + "' found: " + builds.length);
        } else {
            logger.info("no builds for Test plan '" + this.getTestPlanName() + " found");
        }
    }

    synchronized public Map<ExecutionStatus, List<TestCase>> getTestCasesAndSetExecutionStatusToTestCaseMap(boolean update) {
        getTestCasesForTestPlan(update);
        return getExecutionStatusToTestCasesMap();
    }

    private Map<ExecutionStatus, List<TestCase>> getExecutionStatusToTestCasesMap() {
        this.testCasesToStatusMap = testCasesActual.stream().collect(Collectors.groupingBy(TestCase::getExecutionStatus));
        Arrays.stream(ExecutionStatus.values()).forEach(es->this.testCasesToStatusMap.putIfAbsent(es, new ArrayList<>()));
        getTestCasesNotRun();
        getTestCasesPassed();
        getTestCasesBlocked();
        getTestCasesFailed();
        return this.testCasesToStatusMap;
    }

    public List<TestCase> getTestCasesForTestPlan(boolean update) {
        if (ToolManager.getManager().getTestProjectApi().getProjectName() != null && this.testPlanName != null) {
            if (update) {
                try {
                    this.testCasesActual = Arrays.asList(api.getTestCasesForTestPlan(
                            this.testPlanId
                            , null
                            , null
                            , null
                            , null
                            , null
                            , null
                            , null
                            , null
                            , null
                            , null));
                } catch (TestLinkAPIException e) {
                    logger.error(ExceptionUtils.getStackTrace(e));
                }
                this.testCasesActualNum = testCasesActual.size();
            }
        } else {
            logger.error("Can not get test cases. Set projectName && testPlanName");
        }
        return testCasesActual;
    }

    public List<TestCase> getTestCasesNotRun() {
        if (this.testCasesToStatusMap == null || this.testCasesToStatusMap.isEmpty()){
            getTestCasesAndSetExecutionStatusToTestCaseMap(false);
        }
        testCasesActualNotRun = testCasesToStatusMap.get(ExecutionStatus.NOT_RUN);
        testCasesActualNotRunNum = testCasesActualNotRun.size();
        return testCasesActualNotRun;
    }

    public List<TestCase> getTestCasesPassed() {
        if (this.testCasesToStatusMap.isEmpty()){
            getTestCasesAndSetExecutionStatusToTestCaseMap(false);
        }
        testCasesActualPassed = testCasesToStatusMap.get(ExecutionStatus.PASSED);
        this.testCasesActualPassedNum = testCasesActualPassed.size();
        return testCasesActualPassed;
    }

    public List<TestCase> getTestCasesBlocked() {
        if (this.testCasesToStatusMap.isEmpty()) {
            getTestCasesAndSetExecutionStatusToTestCaseMap(false);
        }
        testCasesActualBlocked = testCasesToStatusMap.get(ExecutionStatus.BLOCKED);
        testCasesActualBlockedNum = testCasesActualBlocked.size();
        return testCasesActualBlocked;
    }

    public List<TestCase> getTestCasesFailed() {
        if (this.testCasesToStatusMap.isEmpty()) {
            getTestCasesAndSetExecutionStatusToTestCaseMap(false);
        }
        testCasesActualFailed = testCasesToStatusMap.get(ExecutionStatus.FAILED);
        testCasesActualFailedNum = testCasesActualFailed.size();
        return testCasesActualFailed;
    }
}
