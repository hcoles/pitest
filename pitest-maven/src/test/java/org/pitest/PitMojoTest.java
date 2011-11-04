package org.pitest;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import org.pitest.mutationtest.ReportOptions;

public class PitMojoTest extends BasePitMojoTest {

  private PitMojo testee;

  @Override
  public void setUp() throws Exception {
    super.setUp();

  }

  public void testRunsAMutationReportWhenMutationCoverageGoalTrigered()
      throws Exception {
    this.testee = createPITMojo(createPomWithConfiguration(""));
    this.testee.execute();
    verify(this.executionStrategy).execute(any(ReportOptions.class));
  }

}
