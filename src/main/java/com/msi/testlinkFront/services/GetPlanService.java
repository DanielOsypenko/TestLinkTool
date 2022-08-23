package com.msi.testlinkFront.services;

import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestSuite;
import com.msi.testlinkBack.ToolManager;
import com.msi.testlinkBack.api.TestPlanApi;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class GetPlanService extends Service<Map<TestSuite, List<TestCase>>> {

    TestPlanApi testPlanApi;
    int secAbort;
    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(ToolManager.class.getSimpleName());


    public final void setTestPlanApi(TestPlanApi testPlanApi) {
        this.testPlanApi = testPlanApi;
    }

    public int getSecAbort() {
        return secAbort;
    }

    public void setSecAbort(int secAbort) {
        this.secAbort = secAbort;
    }

    @Override
    protected Task<Map<TestSuite, List<TestCase>>> createTask() {
        return new Task<>() {

            Boolean taskSucceeded = false;

            @Override
            protected Map<TestSuite, List<TestCase>> call() {
                final Timer[] timer = new Timer[1];

                class Limiter {
                    final Task task;

                    public Limiter(Task task, int sec) {
                        this.task = task;
                        timer[0] = new Timer();
                        timer[0].schedule(new LimitTask(), sec * 1000);
                    }

                    class LimitTask extends TimerTask {

                        @Override
                        public void run() {
                            if (!taskSucceeded)
                                logger.info("Time's up. No resp from TestLink server");
                            task.cancel();
                            timer[0].cancel();
                        }
                    }
                }

                new Limiter(this, secAbort);
                Map<TestSuite, List<TestCase>> res = testPlanApi.getTestSuitesPerTestCases();
                taskSucceeded = true;
                return res;
            }
        };
    }
}
