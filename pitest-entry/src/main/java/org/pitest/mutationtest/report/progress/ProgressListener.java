package org.pitest.mutationtest.report.progress;

import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.MutationResultListener;

import java.io.PrintStream;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

class ProgressListener implements MutationResultListener {

    private final PrintStream out;
    private final long intervalSeconds;
    private final AtomicLong mutationsCompleted = new AtomicLong();
    private final ScheduledExecutorService scheduler;

    ProgressListener(PrintStream out, ScheduledExecutorService scheduler, long intervalSeconds) {
        this.out = out;
        this.scheduler = scheduler;
        this.intervalSeconds = intervalSeconds;
    }

    long intervalSeconds() {
        return intervalSeconds;
    }

    @Override
    public void runStart() {
        scheduler.scheduleAtFixedRate(
                () -> out.println(mutationsCompleted.get() + " mutations completed"),
                intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void handleMutationResult(ClassMutationResults results) {
        mutationsCompleted.addAndGet(results.getMutations().size());
    }

    @Override
    public void runEnd() {
        scheduler.shutdownNow();
    }
}
