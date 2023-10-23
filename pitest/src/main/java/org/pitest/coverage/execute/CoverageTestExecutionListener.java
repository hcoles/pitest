package org.pitest.coverage.execute;

import org.pitest.coverage.CoverageReceiver;
import org.pitest.testapi.Description;
import org.pitest.testapi.TestUnitExecutionListener;
import org.pitest.util.Log;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class CoverageTestExecutionListener implements TestUnitExecutionListener {

    private static final Logger LOG = Log.getLogger();
    private final ThreadMXBean threads = ManagementFactory.getThreadMXBean();

    private final CoverageReceiver invokeQueue;
    private final Map<Description, Long> startTimes = new ConcurrentHashMap<>();
    private final Map<Description, Integer> threadsBeforeTest = new ConcurrentHashMap<>();
    private final AtomicLong firstThreadId = new AtomicLong();

    public CoverageTestExecutionListener(CoverageReceiver invokeQueue) {
        this.invokeQueue = invokeQueue;
    }

    @Override
    public void executionStarted(Description description, boolean suppressParallelWarning) {
        LOG.fine(() -> "Gathering coverage for test " + description);
        startTimes.put(description, System.nanoTime());
        if (!firstThreadId.compareAndSet(0, Thread.currentThread().getId())
                && (firstThreadId.get() != Thread.currentThread().getId())
                && (threadsBeforeTest.size() > 0)
                && !suppressParallelWarning) {
            LOG.warning("Tests are run in parallel. Coverage recording most likely will not work properly.");
        }
        threadsBeforeTest.put(description, threads.getThreadCount());
    }

    @Override
    public void executionFinished(Description description, boolean passed, Throwable maybeError) {

        if (maybeError != null) {
            LOG.log(Level.SEVERE, description.toString(), maybeError);
        }

        Long t0 = startTimes.remove(description);
        int executionTime;
        if (t0 == null) {
            LOG.warning("Recorded no start time. Test life cycle not as expected.");
            // substitute an unimportant, but high, time for this test, so it is unlikely to
            // be prioritised above others.
            executionTime = 120_000;
        } else {
            executionTime = (int) NANOSECONDS.toMillis(System.nanoTime() - t0);
            if (executionTime < 0) {
                LOG.warning("Recorded negative test time. Test life cycle not as expected.");
                // substitute an unimportant, but high, time for this test, so it is unlikely to
                // be prioritised above others.
                executionTime = 120_000;
            }
        }

        final int threadsAfterTest = threads.getThreadCount();
        if (threadsAfterTest > threadsBeforeTest.getOrDefault(description, 0)) {
            LOG.warning("More threads at end of test (" + threadsAfterTest + ") "
                    + description + " than start. ("
                    + threadsBeforeTest + ")");
        }
        threadsBeforeTest.remove(description);

        invokeQueue.recordTestOutcome(description, passed, executionTime);
    }

}
