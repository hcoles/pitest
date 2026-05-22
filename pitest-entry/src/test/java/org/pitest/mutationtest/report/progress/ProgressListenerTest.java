package org.pitest.mutationtest.report.progress;

import org.junit.Test;
import org.pitest.mutationtest.report.MutationTestResultMother;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.pitest.mutationtest.report.MutationTestResultMother.aMutationTestResult;
import static org.pitest.mutationtest.report.MutationTestResultMother.createClassResults;

public class ProgressListenerTest {

    private final ByteArrayOutputStream os = new ByteArrayOutputStream();
    private final PrintStream out = new PrintStream(os);

    @Test
    public void shouldCountIndividualMutationsAcrossMultipleResults() throws Exception {
        ProgressListener testee = new ProgressListener(out, 1);
        testee.runStart();
        try {
            testee.handleMutationResult(createClassResults(aMutationTestResult().build()));
            testee.handleMutationResult(createClassResults(
                    aMutationTestResult().build(),
                    aMutationTestResult().build(),
                    aMutationTestResult().build()));
            Thread.sleep(1500);
        } finally {
            testee.runEnd();
        }

        assertThat(os.toString()).contains("4 mutations completed");
    }

    @Test
    public void shouldNotLogProgressWhenIntervalHasNotElapsed() {
        ProgressListener testee = new ProgressListener(out, 60);
        testee.runStart();
        testee.handleMutationResult(createClassResults(aMutationTestResult().build()));
        testee.runEnd();

        assertThat(os.toString()).doesNotContain("mutations completed");
    }
}
