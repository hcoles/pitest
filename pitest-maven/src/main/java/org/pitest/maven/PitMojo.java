package org.pitest.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.pitest.coverage.CoverageSummary;
import org.pitest.functional.Option;
import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.statistics.MutationStatistics;
import org.pitest.mutationtest.tooling.CombinedStatistics;
import org.pitest.plugin.ClientClasspathPlugin;
import org.pitest.plugin.ToolClasspathPlugin;
import org.slf4j.bridge.SLF4JBridgeHandler;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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

  protected final PluginServices      plugins;

  // Concrete List types declared for all fields to work around maven 2 bug

  /**
   * Classes to include in mutation test
   *
   * @parameter expression="${targetClasses}"
   *
   */
  protected ArrayList<String>         targetClasses;

  /**
   * Tests to run
   *
   * @parameter expression="${targetTests}"
   *
   */
  protected ArrayList<String>         targetTests;

  /**
   * Methods not to mutate
   *
   * @parameter expression="${excludedMethods}"
   *
   */
  private ArrayList<String>           excludedMethods;

  /**
   * Classes not to mutate or run tests from
   *
   * @parameter expression="${excludedClasses}"
   *
   */
  private ArrayList<String>           excludedClasses;

  /**
   *
   * @parameter expression="${avoidCallsTo}"
   *
   */
  private ArrayList<String>           avoidCallsTo;

  /**
   * Base directory where all reports are written to.
   *
   * @parameter default-value="${project.build.directory}/pit-reports"
   *            expression="${reportsDirectory}"
   */
  private File                        reportsDirectory;

  /**
   * File to write history information to for incremental analysis
   *
   * @parameter expression="${historyOutputFile}"
   */
  private File                        historyOutputFile;

  /**
   * File to read history from for incremental analysis (can be same as output
   * file)
   *
   * @parameter expression="${historyInputFile}"
   */
  private File                        historyInputFile;

  /**
   * Maximum distance to look from test to class. Relevant when mutating static
   * initializers
   *
   * @parameter default-value="-1" expression="${maxDependencyDistance}"
   */
  private int                         maxDependencyDistance;

  /**
   * Number of threads to use
   *
   * @parameter default-value="1" expression="${threads}"
   */
  private int                         threads;

  /**
   * Mutate static initializers
   *
   * @parameter default-value="false" expression="${mutateStaticInitializers}"
   */
  private boolean                     mutateStaticInitializers;

  /**
   * Detect inlined code
   *
   * @parameter default-value="true" expression="${detectInlinedCode}"
   */
  private boolean                     detectInlinedCode;

  /**
   * Mutation operators to apply
   *
   * @parameter expression="${mutators}"
   */
  private ArrayList<String>           mutators;

  /**
   * Weighting to allow for timeouts
   *
   * @parameter default-value="1.25" expression="${timeoutFactor}"
   */
  private float                       timeoutFactor;

  /**
   * Constant factor to allow for timeouts
   *
   * @parameter default-value="3000" expression="${timeoutConstant}"
   */
  private long                        timeoutConstant;

  /**
   * Maximum number of mutations to allow per class
   *
   * @parameter default-value="-1" expression="${maxMutationsPerClass}"
   */
  private int                         maxMutationsPerClass;

  /**
   * Arguments to pass to child processes
   *
   * @parameter
   */
  private ArrayList<String>           jvmArgs;

  /**
   * Formats to output during analysis phase
   *
   * @parameter expression="${outputFormats}"
   */
  private ArrayList<String>           outputFormats;

  /**
   * Output verbose logging
   *
   * @parameter default-value="false" expression="${verbose}"
   */
  private boolean                     verbose;

  /**
   * Throw error if no mutations found
   *
   * @parameter default-value="true" expression="${failWhenNoMutations}"
   */
  private boolean                     failWhenNoMutations;

  /**
   * Create timestamped subdirectory for report
   *
   * @parameter default-value="true" expression="${timestampedReports}"
   */
  private boolean                     timestampedReports;

  /**
   * TestNG Groups/JUnit Categories to exclude
   *
   * @parameter expression="${excludedGroups}"
   */
  private ArrayList<String>           excludedGroups;

  /**
   * TestNG Groups/JUnit Categories to include
   *
   * @parameter expression="${includedGroups}"
   */
  private ArrayList<String>           includedGroups;

  /**
   * Maximum number of mutations to include in a single analysis unit.
   *
   * @parameter expression="${mutationUnitSize}"
   */
  private int                         mutationUnitSize;

  /**
   * Export line coverage data
   *
   * @parameter default-value="false" expression="${exportLineCoverage}"
   */
  private boolean                     exportLineCoverage;

  /**
   * Mutation score threshold at which to fail build
   *
   * @parameter default-value="0" expression="${mutationThreshold}"
   */
  private int                         mutationThreshold;

  /**
   * Line coverage threshold at which to fail build
   *
   * @parameter default-value="0" expression="${coverageThreshold}"
   */
  private int                         coverageThreshold;

  /**
   * Path to java executable to use when running tests. Will default to
   * executable in JAVA_HOME if none set.
   *
   * @parameter
   */
  private String                      jvm;

  /**
   * Engine to use when generating mutations.
   *
   * @parameter default-value="gregor" expression="${mutationEngine}"
   */
  private String                      mutationEngine;

  /**
   * List of additional classpath entries to use when looking for tests and
   * mutable code. These will be used in addition to the classpath with which
   * PIT is launched.
   *
   * @parameter expression="${additionalClasspathElements}"
   */
  private ArrayList<String>           additionalClasspathElements;

  /**
   * List of classpath entries, formatted as "groupId:artifactId", which should
   * not be included in the classpath when running mutation tests. Modelled
   * after the corresponding Surefire/Failsafe property.
   *
   * @parameter expression="${classpathDependencyExcludes}"
   */
  private ArrayList<String>           classpathDependencyExcludes;

  /**
   * When set indicates that analysis of this project should be skipped
   *
   * @parameter default-value="false"
   */
  private boolean                     skip;

  /**
   * When set will try and create settings based on surefire configuration. This
   * may not give the desired result in some circumstances
   *
   * @parameter default-value="true"
   */
  private boolean                     parseSurefireConfig;

  /**
   * honours common skipTests flag in a maven run
   *
   * @parameter default-value="false"
   */
  private boolean                     skipTests;

  /**
   * Use slf4j for logging
   *
   * @parameter default-value="false" expression="${useSlf4j}"
   */
  private boolean                     useSlf4j;

  /**
   * Configuration properties.
   *
   * Value pairs may be used by pitest plugins.
   *
   * @parameter
   */
  private Map<String, String> pluginConfiguration;

  /**
   * environment configuration
   *
   * Value pairs may be used by pitest plugins.
   *
   * @parameter
   */
  private Map<String, String> environmentVariables = new HashMap<String, String>();


  /**
   * <i>Internal</i>: Project to interact with.
   *
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  protected MavenProject              project;

  /**
   * <i>Internal</i>: Map of plugin artifacts.
   *
   * @parameter expression="${plugin.artifactMap}"
   * @required
   * @readonly
   */
  private Map<String, Artifact>       pluginArtifactMap;

  private final GoalStrategy        goalStrategy;

  public PitMojo() {
    this( new RunPitStrategy(),
          new DependencyFilter(new PluginServices(PitMojo.class.getClassLoader())),
          new PluginServices(PitMojo.class.getClassLoader()));
  }

  public PitMojo(GoalStrategy strategy, Predicate<Artifact> filter, PluginServices plugins) {
    this.goalStrategy = strategy;
    this.filter = filter;
    this.plugins = plugins;
  }

  public final void execute() throws MojoExecutionException, MojoFailureException {

    switchLogging();

    if (shouldRun()) {

      for (ToolClasspathPlugin each : plugins.findToolClasspathPlugins()) {
        getLog().info("Found plugin : " + each.description());
      }

      for (ClientClasspathPlugin each : plugins.findClientClasspathPlugins()) {
        getLog().info("Found shared classpath plugin : " + each.description());
      }

      Option<CombinedStatistics> result = analyse();
      if (result.hasSome()) {
        throwErrorIfScoreBelowThreshold(result.value().getMutationStatistics());
        throwErrorIfCoverageBelowThreshold(result.value().getCoverageSummary());
      }

    } else {
      getLog().info("Skipping project");
    }
  }

  private void switchLogging() {
    if (useSlf4j) {
      SLF4JBridgeHandler.removeHandlersForRootLogger();
      SLF4JBridgeHandler.install();
      Logger.getLogger("PIT").addHandler(new SLF4JBridgeHandler());
      SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();
    }
  }

  private void throwErrorIfCoverageBelowThreshold(CoverageSummary coverageSummary) throws MojoFailureException {
    if ((coverageThreshold != 0) && (coverageSummary.getCoverage() < coverageThreshold)) {
      throw new MojoFailureException("Line coverage of "
          + coverageSummary.getCoverage() + "("
          + coverageSummary.getNumberOfCoveredLines() + "/"
          + coverageSummary.getNumberOfLines() + ") is below threshold of "
          + coverageThreshold);
    }
  }

  private void throwErrorIfScoreBelowThreshold(MutationStatistics result) throws MojoFailureException {
    if ((mutationThreshold != 0) && (result.getPercentageDetected() < mutationThreshold)) {
      throw new MojoFailureException("Mutation score of "
          + result.getPercentageDetected() + " is below threshold of "
          + mutationThreshold);
    }
  }

  protected Option<CombinedStatistics> analyse() throws MojoExecutionException {
    ReportOptions data = new MojoToReportOptionsConverter(this, new SurefireConfigConverter(),filter).convert();
    CombinedStatistics statistics = goalStrategy.execute(detectBaseDir(), data, plugins, environmentVariables);
    return Option.some(statistics);
  }

  protected File detectBaseDir() {
    // execution project doesn't seem to always be available.
    // possibily a maven 2 vs maven 3 issue?
    MavenProject executionProject = project.getExecutionProject();
    if (executionProject == null) {
      return null;
    }
    return executionProject.getBasedir();
  }

  public List<String> getTargetClasses() {
    return targetClasses;
  }

  public List<String> getTargetTests() {
    return targetTests;
  }

  public List<String> getExcludedMethods() {
    return excludedMethods;
  }

  public List<String> getExcludedClasses() {
    return excludedClasses;
  }

  public List<String> getAvoidCallsTo() {
    return avoidCallsTo;
  }

  public File getReportsDirectory() {
    return reportsDirectory;
  }

  public int getMaxDependencyDistance() {
    return maxDependencyDistance;
  }

  public int getThreads() {
    return threads;
  }

  public boolean isMutateStaticInitializers() {
    return mutateStaticInitializers;
  }

  public List<String> getMutators() {
    return mutators;
  }

  public float getTimeoutFactor() {
    return timeoutFactor;
  }

  public long getTimeoutConstant() {
    return timeoutConstant;
  }

  public int getMaxMutationsPerClass() {
    return maxMutationsPerClass;
  }

  public List<String> getJvmArgs() {
    return jvmArgs;
  }

  public List<String> getOutputFormats() {
    return outputFormats;
  }

  public boolean isVerbose() {
    return verbose;
  }

  public MavenProject getProject() {
    return project;
  }

  public Map<String, Artifact> getPluginArtifactMap() {
    return pluginArtifactMap;
  }

  public boolean isFailWhenNoMutations() {
    return failWhenNoMutations;
  }

  public List<String> getExcludedGroups() {
    return excludedGroups;
  }

  public List<String> getIncludedGroups() {
    return includedGroups;
  }

  public int getMutationUnitSize() {
    return mutationUnitSize;
  }

  public boolean isTimestampedReports() {
    return timestampedReports;
  }

  public boolean isDetectInlinedCode() {
    return detectInlinedCode;
  }

  public void setTimestampedReports(boolean timestampedReports) {
    this.timestampedReports = timestampedReports;
  }

  public File getHistoryOutputFile() {
    return historyOutputFile;
  }

  public File getHistoryInputFile() {
    return historyInputFile;
  }

  public boolean isExportLineCoverage() {
    return exportLineCoverage;
  }

  private boolean shouldRun() {
    return !skip && !skipTests && !project.getPackaging().equalsIgnoreCase("pom");
  }

  public String getMutationEngine() {
    return mutationEngine;
  }

  public String getJavaExecutable() {
    return jvm;
  }

  public void setJavaExecutable(String javaExecutable) {
    this.jvm = javaExecutable;
  }

  public List<String> getAdditionalClasspathElements() {
    return additionalClasspathElements;
  }

  public List<String> getClasspathDependencyExcludes() {
    return classpathDependencyExcludes;
  }

  public boolean isParseSurefireConfig() {
    return parseSurefireConfig;
  }

  public Map<String, String> getPluginProperties() {
    return pluginConfiguration;
  }

  public Map<String, String> getEnvironmentVariables() {
    return environmentVariables;
  }

  public GoalStrategy getGoalStrategy() {
    return goalStrategy;
  }
}
