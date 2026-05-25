package org.pitest.mutationtest.report.progress;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.pitest.mutationtest.report.MutationTestResultMother.aMutationTestResult;
import static org.pitest.mutationtest.report.MutationTestResultMother.createClassResults;

public class ProgressListenerTest {

    private final ByteArrayOutputStream os = new ByteArrayOutputStream();
    private final PrintStream out = new PrintStream(os);

    @Test
    public void shouldCountIndividualMutationsAcrossMultipleResults() {
        FakedScheduledExecutorService scheduler = new FakedScheduledExecutorService();
        ProgressListener testee = new ProgressListener(out, scheduler, 3);
        testee.runStart();
        testee.handleMutationResult(createClassResults(aMutationTestResult().build()));
        testee.handleMutationResult(createClassResults(
                aMutationTestResult().build(),
                aMutationTestResult().build(),
                aMutationTestResult().build()));

        scheduler.tick();
        scheduler.tick();
        scheduler.tick();

        testee.runEnd();

        assertThat(os.toString()).contains("4 mutations completed");
    }


    @Test
    public void shouldNotLogProgressWhenIntervalHasNotElapsed() {
        FakedScheduledExecutorService scheduler = new FakedScheduledExecutorService();
        ProgressListener testee = new ProgressListener(out, scheduler, 2);
        testee.runStart();
        testee.handleMutationResult(createClassResults(aMutationTestResult().build()));

        scheduler.tick();

        testee.runEnd();

        assertThat(os.toString()).doesNotContain("mutations completed");
    }


    private static class FakedScheduledExecutorService extends ScheduledThreadPoolExecutor {

        public FakedScheduledExecutorService() {
            super(0);
        }

        Runnable command;
        int secondsElapsed = 0;
        int triggerSeconds = 0;

        public void tick() {
            secondsElapsed += 1;
            if (triggerSeconds == secondsElapsed) {
                command.run();
            }
        }

        @Override
        @NonNull
        public ScheduledFuture<?> scheduleAtFixedRate(@NonNull Runnable command, long initialDelay, long period, @NonNull TimeUnit unit) {
            this.command = command;
            this.triggerSeconds = (int) unit.toSeconds(period);
            return Mockito.mock(ScheduledFuture.class);
        }

    }

}
