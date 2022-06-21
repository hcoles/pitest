package org.pitest.coverage.execute;

import org.pitest.coverage.CoverageReceiver;
import org.pitest.testapi.Description;
import org.pitest.testapi.TestUnitExecutionListener;
import org.pitest.util.Log;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.logging.Logger;

public class CoverageTestExecutionListener implements TestUnitExecutionListener {

    private static final Logger LOG = Log.getLogger();
    private final ThreadMXBean threads = ManagementFactory.getThreadMXBean();

    private final CoverageReceiver invokeQueue;
    private long t0;
    private int threadsBeforeTest;

    public CoverageTestExecutionListener(CoverageReceiver invokeQueue) {
        this.invokeQueue = invokeQueue;
    }

    @Override
    public void executionStarted(Description description) {
        LOG.fine(() -> "Gathering coverage for test " + description);
        t0 = System.currentTimeMillis();
        threadsBeforeTest = this.threads.getThreadCount();
    }

    @Override
    public void executionFinished(Description description, boolean passed) {
        int executionTime = (int) (System.currentTimeMillis() - t0);
        if (executionTime < 0) {
            LOG.warning("Recorded negative test time. Test life cycle not as expected.");
            // substitute an unimportant, but high, time for this test so it is unlikely to
            // be prioritised above others.
            executionTime = 120000;
        }

        final int threadsAfterTest = this.threads.getThreadCount();
        if (threadsAfterTest > threadsBeforeTest) {
            LOG.warning("More threads at end of test (" + threadsAfterTest + ") "
                    + description + " than start. ("
                    + threadsBeforeTest + ")");
        }
        this.invokeQueue.recordTestOutcome(description, passed, executionTime);
    }
}
