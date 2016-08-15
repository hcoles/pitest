package org.pitest.maven;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.mockito.Matchers;
import org.pitest.coverage.CoverageSummary;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.statistics.MutationStatistics;
import org.pitest.mutationtest.statistics.Score;
import org.pitest.mutationtest.tooling.CombinedStatistics;

public class PitMojoTest extends BasePitMojoTest {

  private AbstractPitMojo testee;

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
    setupCoverage(20, 1, 1);
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
    setupCoverage(21, 1, 1);
    try {
      this.testee.execute();
      // pass
    } catch (final MojoFailureException ex) {
      fail();
    }
  }

  public void testThrowsMojoFailureExceptionWhenSurvivingMutantsAboveThreshold()
      throws Exception {
    this.testee = createPITMojo(createPomWithConfiguration("<maxSurviving>19</maxSurviving>"));
    setupSuvivingMutants(20);
    try {
      this.testee.execute();
      fail();
    } catch (final MojoFailureException ex) {
      // pass
    }
  }
  
  public void testDoesNotThrowsMojoFailureExceptionWhenSurvivingMutantsOnThreshold()
      throws Exception {
    this.testee = createPITMojo(createPomWithConfiguration("<maxSurviving>19</maxSurviving>"));
    setupSuvivingMutants(19);
    try {
      this.testee.execute();
    } catch (final MojoFailureException ex) {
      fail();
    }
  }
  
  public void testAllowsSurvivingMutantsThresholdToBeZero()
      throws Exception {
    this.testee = createPITMojo(createPomWithConfiguration("<maxSurviving>0</maxSurviving>"));
    setupSuvivingMutants(1);
    try {
      this.testee.execute();
      fail();
    } catch (final MojoFailureException ex) {
      // pass
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

    AbstractPitMojo mojo = createPITMojo(createPomWithConfiguration("\n"
        + "                    <environmentVariables>\n"
        + "                        <DISPLAY>:20</DISPLAY>\n"
        + "                    </environmentVariables>"));

    assertEquals(mojo.getEnvironmentVariables().get("DISPLAY"), ":20");
  }

  private void setupCoverage(long mutationScore, int lines, int linesCovered)
      throws MojoExecutionException {
    Iterable<Score> scores = Collections.<Score>emptyList();
    final MutationStatistics stats = new MutationStatistics(scores, 100, mutationScore, 0);
    CoverageSummary sum = new CoverageSummary(lines, linesCovered);
    final CombinedStatistics cs = new CombinedStatistics(stats, sum);
    when(
        this.executionStrategy.execute(any(File.class),
            any(ReportOptions.class), any(PluginServices.class), anyMap()))
            .thenReturn(cs);
  }
  
  private void setupSuvivingMutants(long survivors)
      throws MojoExecutionException {
    Iterable<Score> scores = Collections.<Score>emptyList();
    int detected = 100;
    final MutationStatistics stats = new MutationStatistics(scores, detected + survivors, detected, 0);
    CoverageSummary sum = new CoverageSummary(0, 0);
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
