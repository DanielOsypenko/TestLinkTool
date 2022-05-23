package com.msi.testlinkBack;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.constants.ExecutionStatus;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import br.eti.kinoshita.testlinkjavaapi.model.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class G3INCAR_API {



    private final static String SERVER_URL = "http://testlink.watchguardvideo.local/lib/api/xmlrpc/v1/xmlrpc.php";
    private final static String DEV_KEY = "7f2baca03137da97cb6358d62737d0bd";
    private static final Logger logger = LoggerFactory.getLogger(G3INCAR_API.class.getSimpleName());

    TestLinkAPI api;
    TestProject G3INCARproject;
    String projectName;
    TestPlan testPlan;
    String testPlanName;
    TestSuite[] testSuite;
    int testPlanId;
//    TestCase[] testCases = null;
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
    private Map<ExecutionStatus, List<TestCase>> testCasesToStatusMap;


    public G3INCAR_API() throws MalformedURLException {
        api = new TestLinkAPI(new URL(SERVER_URL), DEV_KEY);
    }


    public G3INCAR_API chooseProject(String projectName) {
        this.projectName = projectName;
        this.G3INCARproject = api.getTestProjectByName(projectName);
        return this;
    }

    public G3INCAR_API chooseTestPlan(String planName) {
        this.testPlan = api.getTestPlanByName(planName, projectName);
        this.testPlanId = testPlan.getId();
        this.testPlanName = planName;
        return this;
    }

    public TestLinkAPI chooseTestPlan(String projectName, String testPlanName) {
        this.testPlan = api.getTestPlanByName(testPlanName, projectName);
        this.testPlanId = testPlan.getId();
        this.testPlanName = testPlanName;
        return api;
    }

    public G3INCAR_API chooseTestSuite(List<Integer> testSuiteIds) {
        this.testSuite = api.getTestSuiteByID(testSuiteIds);
        return this;
    }

    public TestLinkAPI getApi() {
        return api;
    }

    public TestProject[] getAllProjects() {
        return api.getProjects();
    }

    public TestPlan[] getTestPlans() {
        return api.getProjectTestPlans(G3INCARproject.getId());
    }

    public TestSuite[] getTestSuits(int testPlanId) {
        return api.getTestSuitesForTestPlan(testPlanId);
    }

    public TestSuite[] getTestSuits() {
        chooseTestPlan(this.projectName, this.testPlanName);
        return api.getTestSuitesForTestPlan(this.testPlanId);
    }

    public List<TestCase> getTestCasesPerTestSuite(TestSuite testSuite){
        return Arrays.asList(api.getTestCasesForTestSuite(testSuite.getId(), true, null));
    }

    private void setTestPlan(TestPlan testPlan) {
        this.testPlan = testPlan;
    }

    public String getTestPlanName() {
        return testPlanName;
    }

    private void setTestPlanName(String testPlanName) {
        this.testPlanName = testPlanName;
    }

    public int getTestCasesActualNum() {
        return testCasesActualNum;
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

    public List<TestCase> getTestCases() {
        if (this.projectName != null && this.testPlanName != null) {
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
        } else {
            logger.error("Set projectName && testPlanName");
        }
        this.testCasesActualNum = testCasesActual.size();
        return testCasesActual;
    }


    public List<TestCase> getTestCases(List<Integer> testCaseIds, Boolean executed, String[] executeStatus) {
        return Arrays.asList(api.getTestCasesForTestPlan(testPlanId
                , testCaseIds
                , null
                , null
                , null
                , executed
                , null
                , executeStatus
                , null
                , null
                , null
        ));
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
        if (testCasesActual == null || testCasesActual.size() > 0) {
            getTestCases();
        } else {
            logger.error("No test cases found");
        }
//        this.testCasesToStatusMap = Arrays.stream(testCasesActual).collect(Collectors.toMap(TestCase::getExecutionStatus, Function.identity()));
        this.testCasesToStatusMap = testCasesActual.stream().collect(Collectors.groupingBy(TestCase::getExecutionStatus));
        Arrays.stream(ExecutionStatus.values()).forEach(es->this.testCasesToStatusMap.putIfAbsent(es, new ArrayList<>()));
        getTestCasesNotRun();
        getTestCasesPassed();
        getTestCasesBlocked();
        getTestCasesFailed();
        return this.testCasesToStatusMap;
    }

    public Map<ExecutionStatus, List<TestCase>> updateTestCasesToStatusMap() {
        testCasesActual = null;
        return getTestCasesToStatusMap();
    }

    public List<TestCase> getTestCasesNotRun() {
        if (this.testCasesToStatusMap == null || this.testCasesToStatusMap.isEmpty()){
            getTestCasesToStatusMap();
        }
//        testCasesActualNotRun = this.testCasesToStatusMap.values().stream()
//                .filter(tc-> (tc.getExecutionStatus() == ExecutionStatus.NOT_RUN))
//                .toArray(TestCase[]::new);
        testCasesActualNotRun = testCasesToStatusMap.get(ExecutionStatus.NOT_RUN);
        testCasesActualNotRunNum = testCasesActualNotRun.size();
        return testCasesActualNotRun;
    }

    public List<TestCase> getTestCasesPassed() {
        if (this.testCasesToStatusMap.isEmpty()){
            getTestCasesToStatusMap();
        }
        testCasesActualPassed = testCasesToStatusMap.get(ExecutionStatus.PASSED);
        testCasesActualPassedNum = testCasesActualPassed.size();
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


    //    Manual ECN - 1.0.11
    public List<String> getTestPlanNames() {
        return Arrays.stream(getTestPlans()).map(TestPlan::getName).collect(Collectors.toList());
    }

    private Map.Entry<String, TestCase> tryGetTestCaseSumByExternalId(Integer id, String externalId) {
        logger.info("get summary (name) for TC: " + externalId);
        Integer extId = Integer.parseInt(externalId.substring(externalId.lastIndexOf("-") + 1));
        return Map.entry(externalId, this.api.getTestCase(id, extId, null));

    }



//    private static String tryGetTestCaseSumByExternalId(String tcId, String externalId){
//        try {
//            logger.info("get summary (name) for TC: "+ externalId);
//            return new TestLinkAPI(new URL(G3INCAR_API.SERVER_URL), G3INCAR_API.DEV_KEY).getTestCaseByExternalId(externalId, null).getName();
//        } catch (MalformedURLException e) {
//            logger.error("Can not get TC from External id" + externalId);
//        }
//        return null;
//    }

    public Map<String, TestCase> getSummaries() {

        Map<Integer, String> ids = testCasesActual.stream()
                .collect(Collectors.toMap(TestCase::getId, TestCase::getFullExternalId));

        return ids.entrySet().parallelStream()
                .map(entry -> this.tryGetTestCaseSumByExternalId(entry.getKey(), entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<TestSuite, List<TestCase>> getTestSuitesPerTestCases(){
        TestSuite[] testSuits = this.getTestSuits();
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


    public static void main(String[] args) throws MalformedURLException {

        G3INCAR_API g3INCAR_api = new G3INCAR_API().chooseProject("G3INCAR");
        g3INCAR_api.chooseTestPlan("Manual ECN - 1.0.11");


        Instant startTimer = Instant.now();

//        HashMap<TestSuite, TestCase[]> res = new HashMap<>(g3INCAR_api.getTestSuitesPerTestCases());
//
////

        g3INCAR_api.getTestCasesToStatusMap();
        logger.info("test cases not run:" + g3INCAR_api.getTestCasesActualFailedNum());
        logger.info("test cases passed:" + g3INCAR_api.getTestCasesActualPassedNum());
        logger.info("test cases failed:" + g3INCAR_api.getTestCasesActualFailedNum());
        logger.info("test cases blocked:" + g3INCAR_api.getTestCasesActualBlockedNum());

        logger.info("after getSummaries " + new SimpleDateFormat("yyyyMMdd_HH:mm:ss").format(Calendar.getInstance().getTime()) +
                " took " + Duration.between(Instant.now(), startTimer).getSeconds() + "sec");
        logger.info("finish");

    }

    private Map.Entry<TestSuite, List<TestCase>> getTestSuiteEntry(TestSuite ts) {
        List<TestCase> tcs = this.getTestCasesPerTestSuite(ts);
        return Map.entry(ts, tcs);
    }

    // ============================= ============================= ============================= ==========================//
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
