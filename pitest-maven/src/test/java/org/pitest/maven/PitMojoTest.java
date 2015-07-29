package org.pitest.maven;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Map;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.pitest.coverage.CoverageSummary;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.statistics.MutationStatistics;
import org.pitest.mutationtest.tooling.CombinedStatistics;

public class PitMojoTest extends BasePitMojoTest {

  private PitMojo testee;

  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  public void testRunsAMutationReportWhenMutationCoverageGoalTrigered()
      throws Exception {
    this.testee = createPITMojo(createPomWithConfiguration(""));
    final Build build = new Build();
    build.setOutputDirectory("foo");
    this.testee.getProject().setBuild(build);
    this.testee.execute();
    verify(this.executionStrategy).execute(any(File.class),
        any(ReportOptions.class), any(PluginServices.class), anyMap());
  }

  public void testDoesNotAnalysePomProjects() throws Exception {
    when(this.project.getPackaging()).thenReturn("pom");
    this.testee = createPITMojo(createPomWithConfiguration(""));
    this.testee.execute();
    verify(this.executionStrategy, never()).execute(any(File.class),
        any(ReportOptions.class), any(PluginServices.class), anyMap());
  }

  public void testDoesNotAnalyseProjectsWithSkipFlagSet() throws Exception {
    this.testee = createPITMojo(createPomWithConfiguration("<skip>true</skip>"));
    this.testee.execute();
    verify(this.executionStrategy, never()).execute(any(File.class),
        any(ReportOptions.class), any(PluginServices.class), anyMap());
  }

  public void testThrowsMojoFailureExceptionWhenMutationScoreBelowThreshold()
      throws Exception {
    this.testee = createPITMojo(createPomWithConfiguration("<mutationThreshold>21</mutationThreshold>"));
    setupCoverage(20l, 1, 1);
    try {
      this.testee.execute();
      fail();
    } catch (final MojoFailureException ex) {
      // pass
    }
  }

  public void testDoesNotThrowsMojoFailureExceptionWhenMutationScoreOnThreshold()
      throws Exception {
    this.testee = createPITMojo(createPomWithConfiguration("<mutationThreshold>21</mutationThreshold>"));
    setupCoverage(21l, 1, 1);
    try {
      this.testee.execute();
      // pass
    } catch (final MojoFailureException ex) {
      fail();
    }
  }

  public void testThrowsMojoFailureExceptionWhenCoverageBelowThreshold()
      throws Exception {
    this.testee = createPITMojo(createPomWithConfiguration("<coverageThreshold>50</coverageThreshold>"));
    setupCoverage(100l, 100, 40);
    try {
      this.testee.execute();
      fail();
    } catch (final MojoFailureException ex) {
      // pass
    }
  }

  public void testDoesNotThrowMojoFailureExceptionWhenCoverageOnThreshold()
      throws Exception {
    this.testee = createPITMojo(createPomWithConfiguration("<coverageThreshold>50</coverageThreshold>"));
    setupCoverage(100l, 100, 50);
    try {
      this.testee.execute();
      // pass
    } catch (final MojoFailureException ex) {
      fail();
    }
  }

  public void testConfigureEnvironmentVariable() throws Exception {

    PitMojo mojo = createPITMojo(createPomWithConfiguration("\n"
        + "                    <environmentVariables>\n"
        + "                        <DISPLAY>:20</DISPLAY>\n"
        + "                    </environmentVariables>"));

    assertEquals(mojo.getEnvironmentVariables().get("DISPLAY"), ":20");
  }

  private void setupCoverage(long mutationScore, int lines, int linesCovered)
      throws MojoExecutionException {
    final MutationStatistics stats = Mockito.mock(MutationStatistics.class);
    when(stats.getPercentageDetected()).thenReturn(mutationScore);
    CoverageSummary sum = new CoverageSummary(lines, linesCovered);
    final CombinedStatistics cs = new CombinedStatistics(stats, sum);
    when(
        this.executionStrategy.execute(any(File.class),
            any(ReportOptions.class), any(PluginServices.class), anyMap()))
            .thenReturn(cs);
  }

  private Map<String, String> anyMap() {
    return Matchers.<Map<String, String>> any();
  }

}
