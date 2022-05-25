package com.msi.testlinkBack.api;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.constants.ExecutionStatus;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestSuite;
import com.msi.testlinkBack.TestCaseCustom;
import com.msi.testlinkBack.TestSuiteCustom;
import com.msi.testlinkBack.ToolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class TestPlanApi {

    private TestLinkAPI api;
    private TestProjectApi testProjectApi;
    private TestPlan testPlan;
    private Integer testPlanId;
    private String testPlanName;
    private static final Logger logger = LoggerFactory.getLogger(TestPlanApi.class.getSimpleName());
    private Map<ExecutionStatus, List<TestCase>> testCasesToStatusMap;
    List<TestCase> testCasesActual = null;
    List<TestCase> testCasesActualNotRun = null;
    List<TestCase> testCasesActualPassed = null;
    List<TestCase> testCasesActualBlocked = null;
    List<TestCase> testCasesActualFailed = null;
    int testCasesActualNum;
    int testCasesActualNotRunNum;
    int testCasesActualPassedNum;
    int testCasesActualBlockedNum;
    int testCasesActualFailedNum;

    public TestPlanApi() {
        ToolManager manager = ToolManager.getManager();
        this.api = manager.getApi();
        this.testProjectApi = manager.getTestProjectApi();
    }

    public TestPlanApi setTestPlan(String planName) {
        this.testPlan = api.getTestPlanByName(planName, testProjectApi.getProjectName());
        this.testPlanId = testPlan.getId();
        this.testPlanName = testPlan.getName();
        return this;
    }

    public TestPlan getTestPlan() {
        return testPlan;
    }

    public int getTestPlanId() {
        return testPlanId;
    }

    public String getTestPlanName() {
        return testPlanName;
    }

    public TestSuite[] getTestSuits(){
        if (testPlanId != null){
            return this.api.getTestSuitesForTestPlan(testPlan.getId());
        } else {
            logger.error("Can not get test suits. Choose Test Plan before");
        }
        return null;
    }

    public List<TestCase> getTestCases() {
        if (ToolManager.getManager().getTestProjectApi().getProjectName() != null && this.testPlanName != null) {
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
            this.testCasesActualNum = testCasesActual.size();
        } else {
            logger.error("Can not get test cases. Set projectName && testPlanName");
        }
        return testCasesActual;
    }


    public List<TestCase> getTestCasesByExecStatus(ExecutionStatus executionStatus) {
        if (testCasesActual == null || testCasesActual.size() > 0) {
            getTestCases();
        } else {
            logger.error("No test cases found");
        }
        return testCasesActual.stream().filter(tc -> (tc.getExecutionStatus() == executionStatus)).collect(Collectors.toList());
    }

    public Map<ExecutionStatus, List<TestCase>> getTestCasesToStatusMap() {
//        if (testCasesActual == null || testCasesActual.size() > 0) {
//            getTestCases();
//        } else {
//            logger.error("No test cases found");
//        }
        getTestCases();
//        this.testCasesToStatusMap = Arrays.stream(testCasesActual).collect(Collectors.toMap(TestCase::getExecutionStatus, Function.identity()));
        this.testCasesToStatusMap = testCasesActual.stream().collect(Collectors.groupingBy(TestCase::getExecutionStatus));
        Arrays.stream(ExecutionStatus.values()).forEach(es->this.testCasesToStatusMap.putIfAbsent(es, new ArrayList<>()));
        getTestCasesNotRun();
        getTestCasesPassed();
        getTestCasesBlocked();
        getTestCasesFailed();
        return this.testCasesToStatusMap;
    }

    public List<TestCase> getTestCasesNotRun() {
        if (this.testCasesToStatusMap == null || this.testCasesToStatusMap.isEmpty()){
            getTestCasesToStatusMap();
        }
        testCasesActualNotRun = testCasesToStatusMap.get(ExecutionStatus.NOT_RUN);
        testCasesActualNotRunNum = testCasesActualNotRun.size();
        return testCasesActualNotRun;
    }

    public List<TestCase> getTestCasesPassed() {
        if (this.testCasesToStatusMap.isEmpty()){
            getTestCasesToStatusMap();
        }
        testCasesActualPassed = testCasesToStatusMap.get(ExecutionStatus.PASSED);
        this.testCasesActualPassedNum = testCasesActualPassed.size();
        return testCasesActualPassed;
    }

    public List<TestCase> getTestCasesBlocked() {
        if (this.testCasesToStatusMap.isEmpty()) {
            getTestCasesToStatusMap();
        }
        testCasesActualBlocked = testCasesToStatusMap.get(ExecutionStatus.BLOCKED);
        testCasesActualBlockedNum = testCasesActualBlocked.size();
        return testCasesActualBlocked;
    }

    public List<TestCase> getTestCasesFailed() {
        if (this.testCasesToStatusMap.isEmpty()) {
            getTestCasesToStatusMap();
        }
        testCasesActualFailed = testCasesToStatusMap.get(ExecutionStatus.FAILED);
        testCasesActualFailedNum = testCasesActualFailed.size();
        return testCasesActualFailed;
    }

    public int getTestCasesActualNotRunNum() {
        return testCasesActualNotRunNum;
    }

    public int getTestCasesActualPassedNum() {
        return testCasesActualPassedNum;
    }

    public int getTestCasesActualBlockedNum() {
        return testCasesActualBlockedNum;
    }

    public int getTestCasesActualFailedNum() {
        return testCasesActualFailedNum;
    }

    private Map.Entry<String, TestCase> tryGetTestCaseSumByExternalId(Integer id, String externalId) {
        logger.info("get summary (name) for TC: " + externalId);
        Integer extId = Integer.parseInt(externalId.substring(externalId.lastIndexOf("-") + 1));
        return Map.entry(externalId, this.api.getTestCase(id, extId, null));

    }

    public Map<String, TestCase> getSummaries() {
        Map<Integer, String> ids = testCasesActual.stream()
                .collect(Collectors.toMap(TestCase::getId, TestCase::getFullExternalId));

        return ids.entrySet().parallelStream()
                .map(entry -> this.tryGetTestCaseSumByExternalId(entry.getKey(), entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public List<TestCase> getTestCasesPerTestSuite(TestSuite testSuite){
        return Arrays.asList(api.getTestCasesForTestSuite(testSuite.getId(), true, null));
    }

    private Map.Entry<TestSuite, List<TestCase>> getTestSuiteEntry(TestSuite ts) {
        List<TestCase> tcs = this.getTestCasesPerTestSuite(ts);
        return Map.entry(ts, tcs);
    }

    public Map<TestSuite, List<TestCase>> getTestSuitesPerTestCases(){
        TestSuite[] testSuits = this.testProjectApi.getTestPlanApi().getTestSuits();
        return Arrays.asList(testSuits).parallelStream()
                .map(this::getTestSuiteEntry).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<TestSuiteCustom, TestCaseCustom[]> getTestSuitesPerTestCasesCustom(){
        return getTestSuitesPerTestCases().entrySet().stream().collect(Collectors.toMap(e-> new TestSuiteCustom(e.getKey())
                , e -> e.getValue().stream().map(TestCaseCustom::new).toArray(TestCaseCustom[]::new)));
    }

    public Map<String, String[]> getTestSuitesPerTestCasesCustomStr(){
        return getTestSuitesPerTestCases().entrySet().stream().collect(Collectors.toMap(e-> new TestSuiteCustom(e.getKey()).toString()
                , e -> e.getValue().stream().map(TestCase::toString).toArray(String[]::new)));
    }

    public List<String> getSummariesAndStatus()  {

        Instant startTimer = Instant.now();

        logger.info("before getTestCasesFailed " + new SimpleDateFormat("yyyyMMdd_HH:mm:ss").format(Calendar.getInstance().getTime()));
        getTestCases();
//        List<String> ids = Arrays.stream(g3INCAR_api.testCasesActual).map(TestCase::getFullExternalId).collect(Collectors.toList());
        Map<Integer, String> ids = testCasesActual.stream().collect(Collectors.toMap(TestCase::getId, TestCase::getFullExternalId));
        logger.info("after getTestCasesFailed " + new SimpleDateFormat("yyyyMMdd_HH:mm:ss").format(Calendar.getInstance().getTime()));
        Map<String, TestCase> summaries = getSummaries();
        summaries.forEach((key, value) -> logger.info(key + ":" + value.toString()));
//        logger.info("after getSummaries " + new SimpleDateFormat("yyyyMMdd_HH:mm:ss").format(Calendar.getInstance().getTime()) +
//                " took " + Duration.between(Instant.now(), startTimer).getSeconds() + "sec");
        return summaries.values().stream().map(TestCase::toString).collect(Collectors.toList());
    }
}
