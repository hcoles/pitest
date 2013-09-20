package org.pitest.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.pitest.functional.Option;
import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.ReportOptions;
import org.pitest.mutationtest.statistics.MutationStatistics;

/**
 * Goal which runs a coverage mutation report
 * 
 * @goal mutationCoverage
 * 
 * @requiresDependencyResolution test
 * 
 * @phase integration-test
 */
public class PitMojo extends AbstractMojo {
  
  protected final Predicate<Artifact> filter;
  
  // Concrete List types declared for all fields to work around maven 2 bug

  /**
   * Classes to include in mutation test
   * 
   * @parameter expression="${targetClasses}"
   * 
   */
  protected ArrayList<String>        targetClasses;

  /**
   * Tests to run
   * 
   * @parameter expression="${targetTests}"
   * 
   */
  protected ArrayList<String>        targetTests;

  /**
   * Methods not to mutate
   * 
   * @parameter expression="${excludedMethods}"
   * 
   */
  private ArrayList<String>          excludedMethods;

  /**
   * Classes not to mutate or run tests from
   * 
   * @parameter expression="${excludedClasses}"
   * 
   */
  private ArrayList<String>          excludedClasses;

  /**
   * 
   * @parameter expression="${avoidCallsTo}"
   * 
   */
  private ArrayList<String>          avoidCallsTo;

  /**
   * Base directory where all reports are written to.
   * 
   * @parameter default-value="${project.build.directory}/pit-reports"
   *            expression="${reportsDirectory}"
   */
  private File                  reportsDirectory;

  /**
   * File to write history information to for incremental analysis
   * 
   * @parameter expression="${historyOutputFile}"
   */
  private File                  historyOutputFile;

  /**
   * File to read history from for incremental analysis (can be same as output
   * file)
   * 
   * @parameter expression="${historyInputFile}"
   */
  private File                  historyInputFile;

  /**
   * Maximum distance to look from test to class. Relevant when mutating static
   * initializers
   * 
   * @parameter default-value="-1" expression="${maxDependencyDistance}"
   */
  private int                   maxDependencyDistance;

  /**
   * Number of threads to use
   * 
   * @parameter default-value="1" expression="${threads}"
   */
  private int                   threads;

  /**
   * Mutate static initializers
   * 
   * @parameter default-value="false" expression="${mutateStaticInitializers}"
   */
  private boolean               mutateStaticInitializers;

  /**
   * Detect inlined code
   * 
   * @parameter default-value="true" expression="${detectInlinedCode}"
   */
  private boolean               detectInlinedCode;

  /**
   * Mutation operators to apply
   * 
   * @parameter expression="${mutators}"
   */
  private ArrayList<String>          mutators;

  /**
   * Weighting to allow for timeouts
   * 
   * @parameter default-value="1.25" expression="${timeoutFactor}"
   */
  private float                 timeoutFactor;

  /**
   * Constant factor to allow for timeouts
   * 
   * @parameter default-value="3000" expression="${timeoutConstant}"
   */
  private long                  timeoutConstant;

  /**
   * Maximum number of mutations to allow per class
   * 
   * @parameter default-value="-1" expression="${maxMutationsPerClass}"
   */
  private int                   maxMutationsPerClass;

  /**
   * Arguments to pass to child processes
   * 
   * @parameter
   */
  private ArrayList<String>          jvmArgs;

  /**
   * Formats to output during analysis phase
   * 
   * @parameter expression="${outputFormats}"
   */
  private ArrayList<String>          outputFormats;

  /**
   * Output verbose logging
   * 
   * @parameter default-value="false" expression="${verbose}"
   */
  private boolean               verbose;

  /**
   * Throw error if no mutations found
   * 
   * @parameter default-value="true" expression="${failWhenNoMutations}"
   */
  private boolean               failWhenNoMutations;

  /**
   * Create timestamped subdirectory for report
   * 
   * @parameter default-value="true" expression="${timestampedReports}"
   */
  private boolean               timestampedReports;

  /**
   * TestNG Groups to exclude
   * 
   * @parameter expression="${excludedTestNGGroups}"
   */
  private ArrayList<String>          excludedTestNGGroups;

  /**
   * TestNG Groups to include
   * 
   * @parameter expression="${includedTestNGGroups}"
   */
  private ArrayList<String>          includedTestNGGroups;

  /**
   * Maximum number of mutations to include in a single analysis unit.
   * 
   * @parameter expression="${mutationUnitSize}"
   */
  private int                   mutationUnitSize;

  /**
   * Export line coverage data
   * 
   * @parameter default-value="false" expression="${exportLineCoverage}"
   */
  private boolean               exportLineCoverage;

  /**
   * Mutation score threshold at which to fail build
   * 
   * @parameter default-value="0" expression="${mutationThreshold}"
   */
  private int                   mutationThreshold;

  /**
   * Engine to use when generating mutations.
   * 
   * @parameter default-value="gregor" expression="${mutationEngine}"
   */
  private String                mutationEngine;

  /**
   * <i>Internal</i>: Project to interact with.
   * 
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  protected MavenProject        project;

  /**
   * <i>Internal</i>: Map of plugin artifacts.
   * 
   * @parameter expression="${plugin.artifactMap}"
   * @required
   * @readonly
   */
  private Map<String, Artifact> pluginArtifactMap;

  protected final GoalStrategy  goalStrategy;

  public PitMojo() {
    this(new RunPitStrategy(), new DependencyFilter());
  }

  public PitMojo(final GoalStrategy strategy, Predicate<Artifact> filter) {
    this.goalStrategy = strategy;
    this.filter = filter;
  }

  public final void execute() throws MojoExecutionException,
      MojoFailureException {
    if (shouldRun()) {
      final Option<MutationStatistics> result = analyse();
      if (result.hasSome()) {
        throwErrorIfScoreBelowThreshold(result.value());
      }

    } else {
      this.getLog().info("Skipping project");
    }
  }

  private void throwErrorIfScoreBelowThreshold(final MutationStatistics result)
      throws MojoFailureException {
    if ((this.mutationThreshold != 0)
        && (result.getPercentageDetected() < this.mutationThreshold)) {
      throw new MojoFailureException("Mutation score of "
          + result.getPercentageDetected() + " is below threshold of "
          + this.mutationThreshold);
    }
  }

  protected Option<MutationStatistics> analyse() throws MojoExecutionException {
    final ReportOptions data = new MojoToReportOptionsConverter(this, filter).convert();
    return Option.some(this.goalStrategy.execute(detectBaseDir(), data));
  }

  protected File detectBaseDir() {
    // execution project doesn't seem to always be available.
    // possibily a maven 2 vs maven 3 issue?
    final MavenProject executionProject = this.project.getExecutionProject();
    if (executionProject == null) {
      return null;
    }
    return executionProject.getBasedir();
  }

  public List<String> getTargetClasses() {
    return this.targetClasses;
  }

  public List<String> getTargetTests() {
    return this.targetTests;
  }

  public List<String> getExcludedMethods() {
    return this.excludedMethods;
  }

  public List<String> getExcludedClasses() {
    return this.excludedClasses;
  }

  public List<String> getAvoidCallsTo() {
    return this.avoidCallsTo;
  }

  public File getReportsDirectory() {
    return this.reportsDirectory;
  }

  public int getMaxDependencyDistance() {
    return this.maxDependencyDistance;
  }

  public int getThreads() {
    return this.threads;
  }

  public boolean isMutateStaticInitializers() {
    return this.mutateStaticInitializers;
  }

  public List<String> getMutators() {
    return this.mutators;
  }

  public float getTimeoutFactor() {
    return this.timeoutFactor;
  }

  public long getTimeoutConstant() {
    return this.timeoutConstant;
  }

  public int getMaxMutationsPerClass() {
    return this.maxMutationsPerClass;
  }

  public List<String> getJvmArgs() {
    return this.jvmArgs;
  }

  public List<String> getOutputFormats() {
    return this.outputFormats;
  }

  public boolean isVerbose() {
    return this.verbose;
  }

  public MavenProject getProject() {
    return this.project;
  }

  public Map<String, Artifact> getPluginArtifactMap() {
    return this.pluginArtifactMap;
  }

  public boolean isFailWhenNoMutations() {
    return this.failWhenNoMutations;
  }

  public List<String> getExcludedTestNGGroups() {
    return this.excludedTestNGGroups;
  }

  public List<String> getIncludedTestNGGroups() {
    return this.includedTestNGGroups;
  }

  public int getMutationUnitSize() {
    return this.mutationUnitSize;
  }

  public boolean isTimestampedReports() {
    return this.timestampedReports;
  }

  public boolean isDetectInlinedCode() {
    return this.detectInlinedCode;
  }

  public void setTimestampedReports(final boolean timestampedReports) {
    this.timestampedReports = timestampedReports;
  }

  public File getHistoryOutputFile() {
    return this.historyOutputFile;
  }

  public File getHistoryInputFile() {
    return this.historyInputFile;
  }

  public boolean isExportLineCoverage() {
    return this.exportLineCoverage;
  }

  protected boolean shouldRun() {
    return !this.project.getPackaging().equalsIgnoreCase("pom");
  }

  public String getMutationEngine() {
    return this.mutationEngine;
  }

}
