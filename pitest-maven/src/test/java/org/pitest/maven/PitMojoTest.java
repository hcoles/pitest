package org.pitest.maven;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.apache.maven.model.Build;
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
    Build build = new Build();
    build.setOutputDirectory("foo");
    this.testee.getProject().setBuild(build);
    this.testee.execute();
    verify(this.executionStrategy).execute(any(File.class),any(ReportOptions.class));
  }
  
  public void testDoesNotAnalysePomProjects()
      throws Exception {
    when(this.project.getPackaging()).thenReturn("pom");
    this.testee = createPITMojo(createPomWithConfiguration(""));
    this.testee.execute();
    verify(this.executionStrategy, never()).execute(any(File.class),any(ReportOptions.class));
  }


}
