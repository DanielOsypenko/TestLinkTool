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
    TestCase[] testCases = null;
    TestCase[] testCasesActual = null;

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

    public TestCase[] getTestCasesPerTestSuite(TestSuite testSuite){
        return api.getTestCasesForTestSuite(testSuite.getId(), true, null);
    }



    public TestCase[] getTestCases() {
        if (this.projectName != null && this.testPlanName != null) {
            this.testCases = api.getTestCasesForTestPlan(
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
        } else {
            logger.error("Set projectName && testPlanName");
        }
        return testCases;
    }


    public TestCase[] getTestCases(List<Integer> testCaseIds, Boolean executed, String[] executeStatus) {
        return api.getTestCasesForTestPlan(testPlanId
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
        );
    }


    public TestCase[] getTestCasesByExecStatus(ExecutionStatus executionStatus) {
        if (testCases == null || testCases.length > 0) {
            getTestCases();
        } else {
            logger.error("No test cases found");
        }
        return Arrays.stream(testCases).filter(tc -> (tc.getExecutionStatus() == executionStatus)).toArray(TestCase[]::new);
    }

    public TestCase[] getTestCasesNotRun() {
        testCasesActual = getTestCasesByExecStatus(ExecutionStatus.NOT_RUN);
        return testCasesActual;
    }

    public TestCase[] getTestCasesPassed() {
        testCasesActual = getTestCasesByExecStatus(ExecutionStatus.PASSED);
        return testCasesActual;
    }

    public TestCase[] getTestCasesBlocked() {
        testCasesActual = getTestCasesByExecStatus(ExecutionStatus.BLOCKED);
        return testCasesActual;
    }

    public TestCase[] getTestCasesFailed() {
        testCasesActual = getTestCasesByExecStatus(ExecutionStatus.FAILED);
        return testCasesActual;
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


//new TestLinkAPI(new URL(SERVER_URL), DEV_KEY).getTestCaseByExternalId(tc.getFullExternalId());
//        Flux.fromIterable(Arrays.asList(testCasesActual))
//                .flatMap(tc -> tryGetTestCaseSumByExternalId(tc))
//                .parallel()
//                .runOn(Schedulers.parallel())
//                .sequential()
//                .collectList()
//                .block();

//        return Flux.fromIterable(ids)
//                .mapNotNull(this::tryGetTestCaseSumByExternalId)
//                .parallel(300)
//                .runOn(Schedulers.parallel())
//                .sequential()
//                .collectList()
//                .block();


        Map<Integer, String> ids = Arrays.stream(this.testCasesActual)
                .collect(Collectors.toMap(TestCase::getId, TestCase::getFullExternalId));

        return ids.entrySet().parallelStream()
                .map(entry -> this.tryGetTestCaseSumByExternalId(entry.getKey(), entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<TestSuite, TestCase[]> getTestSuitesPerTestCases(){
        TestSuite[] testSuits = this.getTestSuits();
        return Arrays.asList(testSuits).parallelStream()
                .map(this::getTestSuiteEntry).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<TestSuiteCustom, TestCaseCustom[]> getTestSuitesPerTestCasesCustom(){
        return getTestSuitesPerTestCases().entrySet().stream().collect(Collectors.toMap(e-> new TestSuiteCustom(e.getKey())
                , e -> Arrays.stream(e.getValue()).map(TestCaseCustom::new).toArray(TestCaseCustom[]::new)));
    }

    public Map<String, String[]> getTestSuitesPerTestCasesCustomStr(){
        return getTestSuitesPerTestCases().entrySet().stream().collect(Collectors.toMap(e-> new TestSuiteCustom(e.getKey()).toString()
                , e -> Arrays.stream(e.getValue()).map(TestCase::toString).toArray(String[]::new)));
    }


    public static void main(String[] args) throws MalformedURLException {

        G3INCAR_API g3INCAR_api = new G3INCAR_API().chooseProject("G3INCAR");
        g3INCAR_api.chooseTestPlan("Manual ECN - 1.0.11");


        Instant startTimer = Instant.now();
        HashMap<TestSuite, TestCase[]> res = new HashMap<>(g3INCAR_api.getTestSuitesPerTestCases());

//        Arrays.stream(testSuits).map((TestSuite ts, TestCase[] tcs) -> g3INCAR_api.api.getTestCasesForTestSuite(ts.getId(), true, null)).collect(Collectors.toMap(TestSuite))
//        Arrays.stream(testSuits).collect(Collectors.toMap(testSuits, g3INCAR_api.api.getTestCasesForTestSuite(ts.getId(), true, null), Function.identity()))



//        getSummaries_performance();
//
        // all + Failed (19 tests) = 51 sec (only failed 15sec) // all (1790) + passed (1771)

//        g3INCAR_api.api.getTestCaseByExternalId("G3INCAR-TC-33444873", null);
//        g3INCAR_api.api.getTestCasesForTestSuite()
        // I am TERMINATOR!!!!! //
//        Arrays.stream(tcs).


//        g3INCAR_api.getTestCases();
//        TestCase[] tcs = g3INCAR_api.getTestCasesFailed();
//        TestCase tc_one = tcs[0];
        logger.info("after getSummaries " + new SimpleDateFormat("yyyyMMdd_HH:mm:ss").format(Calendar.getInstance().getTime()) +
                " took " + Duration.between(Instant.now(), startTimer).getSeconds() + "sec");
        logger.info("finish");

    }

    private Map.Entry<TestSuite, TestCase[]> getTestSuiteEntry(TestSuite ts) {
        TestCase[] tcs = this.getTestCasesPerTestSuite(ts);
        return Map.entry(ts, tcs);
    }

    // ============================= ============================= ============================= ==========================//
    public List<String> getSummaries_performance()  {


        Instant startTimer = Instant.now();

        logger.info("before getTestCasesFailed " + new SimpleDateFormat("yyyyMMdd_HH:mm:ss").format(Calendar.getInstance().getTime()));
        getTestCasesPassed();
//        List<String> ids = Arrays.stream(g3INCAR_api.testCasesActual).map(TestCase::getFullExternalId).collect(Collectors.toList());
        Map<Integer, String> ids = Arrays.stream(testCasesActual).collect(Collectors.toMap(TestCase::getId, TestCase::getFullExternalId));
        logger.info("after getTestCasesFailed " + new SimpleDateFormat("yyyyMMdd_HH:mm:ss").format(Calendar.getInstance().getTime()));
        Map<String, TestCase> summaries = getSummaries();
        summaries.forEach((key, value) -> logger.info(key + ":" + value.toString()));
        logger.info("after getSummaries " + new SimpleDateFormat("yyyyMMdd_HH:mm:ss").format(Calendar.getInstance().getTime()) +
                " took " + Duration.between(Instant.now(), startTimer).getSeconds() + "sec");
        return summaries.values().stream().map(TestCase::toString).collect(Collectors.toList());
    }


}
