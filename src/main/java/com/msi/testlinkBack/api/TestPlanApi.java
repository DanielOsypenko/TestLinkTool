package com.msi.testlinkBack.api;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.constants.ExecutionStatus;
import br.eti.kinoshita.testlinkjavaapi.model.*;
import br.eti.kinoshita.testlinkjavaapi.util.TestLinkAPIException;
import com.msi.ConfigManager;
import com.msi.testlinkBack.TestCaseCustom;
import com.msi.testlinkBack.TestSuiteCustom;
import com.msi.testlinkBack.ToolManager;
import org.apache.commons.lang.exception.ExceptionUtils;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class TestPlanApi extends TestPlanListener {

    private TestLinkAPI api;
    TestProjectApi testProjectApi;
    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(TestPlanApi.class.getSimpleName());

    ConcurrentLinkedQueue <ReportTCResultResponse> reportTCResultResponse = new ConcurrentLinkedQueue<>();

    public TestPlanApi() {
        ToolManager manager = ToolManager.getInstance();
        this.api = manager.getApi();
        this.testProjectApi = manager.getTestProjectApi();
    }

    public TestPlan getTestPlan() {
        return testPlan;
    }

    public TestSuite[] getTestSuits(){
        if (testPlanId != null){
            return this.api.getTestSuitesForTestPlan(testPlanId);
        } else {
            logger.log(Level.SEVERE, "Can not get test suits. Choose Test Plan before");
        }
        return null;
    }

    public Build[] getBuilds() {
        return builds;
    }

    public Integer getBuildIdLast() {
        return buildIdLast;
    }


    public List<TestCase> getTestCasesByExecStatus(ExecutionStatus executionStatus) throws TestLinkAPIException {
        if (testCasesActual == null || testCasesActual.size() > 0) {
            getTestCasesForTestPlan(true);
        } else {
            logger.log(Level.SEVERE, "No test cases found");
        }
        return testCasesActual.stream().filter(tc -> (tc.getExecutionStatus() == executionStatus)).collect(Collectors.toList());
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

    public LinkedHashMap<String, TestCase> getSummaries() {
        Map<Integer, String> ids = testCasesActual.stream()
                .collect(Collectors.toMap(TestCase::getId, TestCase::getFullExternalId));

        return new LinkedHashMap<>(ids.entrySet().parallelStream()
                .map(entry -> this.tryGetTestCaseSumByExternalId(entry.getKey(), entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    public List<TestCase> getTestCasesPerTestSuite(TestSuite testSuite){
        return Arrays.asList(api.getTestCasesForTestSuite(testSuite.getId(), true, null));
    }

    private Map.Entry<TestSuite, List<TestCase>> getTestSuiteEntry(TestSuite ts) {
        List<TestCase> tcs = this.getTestCasesPerTestSuite(ts);
        return Map.entry(ts, tcs);
    }

    // Get the map with summaries
    public LinkedHashMap<TestSuite, List<TestCase>> getTestSuitesPerTestCases(){
        TestSuite[] testSuits = testProjectApi.getTestPlanApi().getTestSuits();
        return new LinkedHashMap<>(Arrays.asList(testSuits).parallelStream()
                .map(this::getTestSuiteEntry).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    public LinkedHashMap<TestSuiteCustom, TestCaseCustom[]> getTestSuitesPerTestCasesCustom(){
        return new LinkedHashMap<>(getTestSuitesPerTestCases().entrySet().stream().collect(Collectors.toMap(e-> new TestSuiteCustom(e.getKey())
                , e -> e.getValue().stream().map(TestCaseCustom::new).toArray(TestCaseCustom[]::new))));
    }

    public LinkedHashMap<String, String[]> getTestSuitesPerTestCasesCustomStr(){
        return new LinkedHashMap<>(getTestSuitesPerTestCases().entrySet().stream().collect(Collectors.toMap(e-> new TestSuiteCustom(e.getKey()).toString()
                , e -> e.getValue().stream().map(TestCase::toString).toArray(String[]::new))));
    }

    public List<String> getSummariesAndStatus()  throws TestLinkAPIException{
        Instant startTimer = Instant.now();
        logger.info("before getSummariesAndStatus " + new SimpleDateFormat("yyyyMMdd_HH:mm:ss").format(Calendar.getInstance().getTime()));
        getTestCasesForTestPlan(true);
        LinkedHashMap<Integer, String> ids = new LinkedHashMap<>(testCasesActual.stream().collect(Collectors.toMap(TestCase::getId, TestCase::getFullExternalId)));
        LinkedHashMap<String, TestCase> summaries = getSummaries();
        summaries.forEach((key, value) -> logger.info(key + ":" + value.toString()));
        logger.info("after getSummaries " + new SimpleDateFormat("yyyyMMdd_HH:mm:ss").format(Calendar.getInstance().getTime()) +
                " took " + Duration.between(Instant.now(), startTimer).getSeconds() + "sec");
        return summaries.values().stream().map(TestCase::toString).collect(Collectors.toList());
    }

    /*   The map with summaries always contains more test cases than Test plan.
     To get a test plan user chooses from all test cases
     Method is a helper to create map with only relevant test cases from the test plan, but with summaries.
     @return the ma
     */
    public LinkedHashMap<TestSuite, List<TestCase>> filterMapWithSummaries(LinkedHashMap<TestSuite, List<TestCase>> mapWithSummaries) {
        List <TestCase> testCases = getTestCasesForTestPlan(false);
        if (testCases != null) {
            List<Integer> idsFromTestPlanTestCases = getTestCasesForTestPlan(false)
                    .stream().map(TestCase::getId).collect(Collectors.toList());
            mapWithSummaries.entrySet().forEach(entry ->
                    entry.setValue(entry.getValue().stream().filter(tc -> idsFromTestPlanTestCases.contains(tc.getId())).collect(Collectors.toList()))
            );
        } else {
            logger.log(Level.SEVERE, "Got exception on getTestCasesForTestPlan(). Assuming no permissions for test plan");
        }
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
            logger.log(Level.SEVERE, "execute report results interrupted: " + ExceptionUtils.getStackTrace(e));
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
