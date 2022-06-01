package com.msi.testlinkBack.api;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.constants.ExecutionStatus;
import br.eti.kinoshita.testlinkjavaapi.model.*;
import com.msi.testlinkBack.TestCaseCustom;
import com.msi.testlinkBack.TestSuiteCustom;
import com.msi.testlinkBack.ToolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TestPlanApi extends TestPlanListener {

    private TestLinkAPI api;
    TestProjectApi testProjectApi;
    private TestPlan testPlan;
//    private Integer testPlanId;
//    private String testPlanName;
//    static final Logger logger = LoggerFactory.getLogger(TestPlanApi.class.getSimpleName());
//    Map<ExecutionStatus, List<TestCase>> testCasesToStatusMap;
//    List<TestCase> testCasesActual = null;
//    List<TestCase> testCasesActualNotRun = null;
//    List<TestCase> testCasesActualPassed = null;
//    List<TestCase> testCasesActualBlocked = null;
//    List<TestCase> testCasesActualFailed = null;
//    int testCasesActualNum;
//    int testCasesActualNotRunNum;
//    int testCasesActualPassedNum;
//    int testCasesActualBlockedNum;
//    int testCasesActualFailedNum;

//
//    Build[] builds;
//    private Integer buildIdLast;


    ConcurrentLinkedQueue <ReportTCResultResponse> reportTCResultResponse = new ConcurrentLinkedQueue<>();

    public TestPlanApi() {
//        this.testPlanListener = ((TestPlanListener) this);
        ToolManager manager = ToolManager.getManager();
        this.api = manager.getApi();
        this.testProjectApi = manager.getTestProjectApi();
    }

//    public TestPlanApi setTestPlan(String planName) {
//        this.testPlan = api.getTestPlanByName(planName, testProjectApi.getProjectName());
//        this.testPlanId = testPlan.getId();
//        this.testPlanName = testPlan.getName();
//        Executors.newCachedThreadPool().execute(() -> this.setBuilds(api.getBuildsForTestPlan(this.testPlanId)));
////        Thread testPlanListenerThread = new Thread(this);
////        testPlanListenerThread.start();
//        return this;
//    }

//    public void setBuilds(Build[] builds) {
//        this.builds = builds;
//        if (this.builds != null && this.builds.length > 0){
//            this.buildIdLast = builds[builds.length-1].getId();
//            logger.info("builds for Test plan '" + this.getTestPlanName() + "' found: " + builds.length);
//        } else {
//            logger.info("no builds for Test plan '" + this.getTestPlanName() + " found");
//        }
//    }



    public TestPlan getTestPlan() {
        return testPlan;
    }

//    public int getTestPlanId() {
//        return testPlanId;
//    }
//
//    public String getTestPlanName() {
//        return testPlanName;
//    }
//
    public TestSuite[] getTestSuits(){
        if (testPlanId != null){
            return this.api.getTestSuitesForTestPlan(testPlanId);
        } else {
            logger.error("Can not get test suits. Choose Test Plan before");
        }
        return null;
    }

    public Build[] getBuilds() {
        return builds;
    }

    public Integer getBuildIdLast() {
        return buildIdLast;
    }

//    public List<TestCase> getTestCasesForTestPlan(boolean update) {
//        if (ToolManager.getManager().getTestProjectApi().getProjectName() != null && this.testPlanName != null) {
//            if (update) {
//                this.testCasesActual = Arrays.asList(api.getTestCasesForTestPlan(
//                        this.testPlanId
//                        , null
//                        , null
//                        , null
//                        , null
//                        , null
//                        , null
//                        , null
//                        , null
//                        , null
//                        , null));
//                this.testCasesActualNum = testCasesActual.size();
//            }
//        } else {
//            logger.error("Can not get test cases. Set projectName && testPlanName");
//        }
//        return testCasesActual;
//    }


    public List<TestCase> getTestCasesByExecStatus(ExecutionStatus executionStatus) {
        if (testCasesActual == null || testCasesActual.size() > 0) {
            getTestCasesForTestPlan(true);
        } else {
            logger.error("No test cases found");
        }
        return testCasesActual.stream().filter(tc -> (tc.getExecutionStatus() == executionStatus)).collect(Collectors.toList());
    }

//    synchronized public Map<ExecutionStatus, List<TestCase>> getTestCasesAndSetExecutionStatusToTestCaseMap(boolean update) {
//        getTestCasesForTestPlan(update);
//        return getExecutionStatusToTestCasesMap();
//    }
//
//    private Map<ExecutionStatus, List<TestCase>> getExecutionStatusToTestCasesMap() {
//        this.testCasesToStatusMap = testCasesActual.stream().collect(Collectors.groupingBy(TestCase::getExecutionStatus));
//        Arrays.stream(ExecutionStatus.values()).forEach(es->this.testCasesToStatusMap.putIfAbsent(es, new ArrayList<>()));
//        getTestCasesNotRun();
//        getTestCasesPassed();
//        getTestCasesBlocked();
//        getTestCasesFailed();
//        return this.testCasesToStatusMap;
//    }

//    public List<TestCase> getTestCasesNotRun() {
//        if (this.testCasesToStatusMap == null || this.testCasesToStatusMap.isEmpty()){
//            getTestCasesAndSetExecutionStatusToTestCaseMap(false);
//        }
//        testCasesActualNotRun = testCasesToStatusMap.get(ExecutionStatus.NOT_RUN);
//        testCasesActualNotRunNum = testCasesActualNotRun.size();
//        return testCasesActualNotRun;
//    }
//
//    public List<TestCase> getTestCasesPassed() {
//        if (this.testCasesToStatusMap.isEmpty()){
//            getTestCasesAndSetExecutionStatusToTestCaseMap(false);
//        }
//        testCasesActualPassed = testCasesToStatusMap.get(ExecutionStatus.PASSED);
//        this.testCasesActualPassedNum = testCasesActualPassed.size();
//        return testCasesActualPassed;
//    }
//
//    public List<TestCase> getTestCasesBlocked() {
//        if (this.testCasesToStatusMap.isEmpty()) {
//            getTestCasesAndSetExecutionStatusToTestCaseMap(false);
//        }
//        testCasesActualBlocked = testCasesToStatusMap.get(ExecutionStatus.BLOCKED);
//        testCasesActualBlockedNum = testCasesActualBlocked.size();
//        return testCasesActualBlocked;
//    }
//
//    public List<TestCase> getTestCasesFailed() {
//        if (this.testCasesToStatusMap.isEmpty()) {
//            getTestCasesAndSetExecutionStatusToTestCaseMap(false);
//        }
//        testCasesActualFailed = testCasesToStatusMap.get(ExecutionStatus.FAILED);
//        testCasesActualFailedNum = testCasesActualFailed.size();
//        return testCasesActualFailed;
//    }

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

    // Get the map with summaries
    public Map<TestSuite, List<TestCase>> getTestSuitesPerTestCases(){
        TestSuite[] testSuits = testProjectApi.getTestPlanApi().getTestSuits();
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
        getTestCasesForTestPlan(true);
//        List<String> ids = Arrays.stream(g3INCAR_api.testCasesActual).map(TestCase::getFullExternalId).collect(Collectors.toList());
        Map<Integer, String> ids = testCasesActual.stream().collect(Collectors.toMap(TestCase::getId, TestCase::getFullExternalId));
        logger.info("after getTestCasesFailed " + new SimpleDateFormat("yyyyMMdd_HH:mm:ss").format(Calendar.getInstance().getTime()));
        Map<String, TestCase> summaries = getSummaries();
        summaries.forEach((key, value) -> logger.info(key + ":" + value.toString()));
//        logger.info("after getSummaries " + new SimpleDateFormat("yyyyMMdd_HH:mm:ss").format(Calendar.getInstance().getTime()) +
//                " took " + Duration.between(Instant.now(), startTimer).getSeconds() + "sec");
        return summaries.values().stream().map(TestCase::toString).collect(Collectors.toList());
    }

    /*   The map with summaries always contains more test cases than Test plan.
     To get a test plan user chooses from all test cases
     Method is a helper to create map with only relevant test cases from the test plan, but with summaries.
     @return the ma
     */
    public Map<TestSuite, List<TestCase>> filterMapWithSummaries(Map<TestSuite, List<TestCase>> mapWithSummaries) {
        List<Integer> idsFromTestPlanTestCases = getTestCasesForTestPlan(false)
                .stream().map(TestCase::getId).collect(Collectors.toList());
        mapWithSummaries.entrySet().forEach(entry ->
                entry.setValue(entry.getValue().stream().filter(tc -> idsFromTestPlanTestCases.contains(tc.getId())).collect(Collectors.toList()))
        );
        return mapWithSummaries;
    }

    public ConcurrentLinkedQueue <ReportTCResultResponse> reportResult(ExecutionStatus executionStatus, Integer... testCaseIds){
        return reportResult(executionStatus, null, null , testCaseIds);
    }

    /* The base Method to sent reports to TestLink service
        Method sends post requests simultaneously and awaits for all the executors finish in max of 20 sec timeout
    * */
    public ConcurrentLinkedQueue <ReportTCResultResponse> reportResult(ExecutionStatus executionStatus
            , Map<String, String> customFields
            , String notes
            , Integer... testCaseIds){

        ExecutorService executionService = Executors.newCachedThreadPool();
        for (Integer testCaseId: testCaseIds) {
            executionService.submit(() -> {
                ReportTCResultResponse reportRes = api.reportTCResult(testCaseId
                        , null
                        , getTestPlanId()
                        , executionStatus
                        , getBuildIdLast()
                        , null
                        , notes
                        , null
                        , null
                        , null
                        , null
                        , customFields
                        , null);
                reportTCResultResponse.add(reportRes);
                logger.info("report result for tc-id " + testCaseId + ": \n\t" + reportRes.getMessage());
            });
        }
        executionService.shutdown();
        try {
            boolean tasksLeft = executionService.awaitTermination(20, TimeUnit.SECONDS);
            logger.info("all reports sent: " + tasksLeft);
        } catch (InterruptedException e) {
            logger.error("execute report results interrupted: " + Arrays.toString(e.getStackTrace()));
        }
        return reportTCResultResponse;
    }

    public List<TestCase> getAllTestCasesFromSuiteTree() {
        return this.getTestSuitesPerTestCases().values().stream().flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public List<Integer> getAllTestCaseIdsFromSuiteTree() {
        return this.getTestSuitesPerTestCases().values().stream().flatMap(List::stream).map(tc->tc.getId())
                .collect(Collectors.toList());
    }
}
