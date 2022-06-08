package com.msi.testlinkBack.api;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.constants.ExecutionStatus;
import br.eti.kinoshita.testlinkjavaapi.model.Build;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.util.TestLinkAPIException;
import com.msi.testlinkBack.ToolManager;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class TestPlanListener implements Runnable{

    protected TestLinkAPI api;
    protected TestProjectApi testProjectApi;
    protected TestPlan testPlan;
    protected Integer testPlanId;
    protected String testPlanName;
    protected LinkedHashMap<ExecutionStatus, List<TestCase>> testCasesToStatusMap;
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

    static final Logger logger = LoggerFactory.getLogger(TestPlanListener.class.getSimpleName());
    final Object lock = ToolManager.getInstance().getLock();

    public TestPlanListener() {
        ToolManager manager = ToolManager.getInstance();
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
            // method is synchronized
            try {
                getTestCasesAndSetExecutionStatusToTestCaseMap(true);
            } catch (TestLinkAPIException e) {
                logger.error("Got exception on requesting test suite:\n"+ ExceptionUtils.getStackTrace(e));
                // raise popup
                synchronized (lock){
                    lock.notifyAll();
                }
            }
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
        if (planName != null) {
            this.testPlan = api.getTestPlanByName(planName, testProjectApi.getProjectName());
            this.testPlanId = testPlan.getId();
            this.testPlanName = testPlan.getName();
            Executors.newCachedThreadPool().execute(() -> this.setBuilds(api.getBuildsForTestPlan(this.testPlanId)));
        } else {
            this.testPlan = null;
            this.testPlanId = null;
            this.testPlanName = null;
        }
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

    synchronized public Map<ExecutionStatus, List<TestCase>> getTestCasesAndSetExecutionStatusToTestCaseMap(boolean update) throws TestLinkAPIException{
        Map<ExecutionStatus, List<TestCase>> res = null;
        if (testPlan != null && testPlanId != null && testPlanName != null) {
            getTestCasesForTestPlan(update);
            res = getExecutionStatusToTestCasesMap();
        }
        return res;
    }

    private LinkedHashMap<ExecutionStatus, List<TestCase>> getExecutionStatusToTestCasesMap() {
        this.testCasesToStatusMap = new LinkedHashMap<>(testCasesActual.stream().collect(Collectors.groupingBy(TestCase::getExecutionStatus)));
        Arrays.stream(ExecutionStatus.values()).forEach(es->this.testCasesToStatusMap.putIfAbsent(es, new ArrayList<>()));
        getTestCasesNotRun();
        getTestCasesPassed();
        getTestCasesBlocked();
        getTestCasesFailed();
        return this.testCasesToStatusMap;
    }

    public List<TestCase> getTestCasesForTestPlan(boolean update) throws TestLinkAPIException {
        if (ToolManager.getInstance().getTestProjectApi().getProjectName() != null && this.testPlanName != null) {
            if (update) {
                TestCase[] testCases = api.getTestCasesForTestPlan(
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
                        , null);
                this.testCasesActual = Arrays.asList(testCases);

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
        if (this.testCasesToStatusMap != null && this.testCasesToStatusMap.isEmpty()) {
            getTestCasesAndSetExecutionStatusToTestCaseMap(false);
        }
        if (this.testCasesToStatusMap != null) {
            testCasesActualFailed = testCasesToStatusMap.get(ExecutionStatus.FAILED);
            testCasesActualFailedNum = testCasesActualFailed.size();
        }
        return testCasesActualFailed;
    }
}
