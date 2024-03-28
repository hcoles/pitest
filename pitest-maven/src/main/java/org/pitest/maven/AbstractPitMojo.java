package org.pitest.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.pitest.coverage.CoverageSummary;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.statistics.MutationStatistics;
import org.pitest.mutationtest.tooling.CombinedStatistics;
import org.pitest.plugin.ToolClasspathPlugin;
import org.slf4j.bridge.SLF4JBridgeHandler;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

import javax.inject.Inject;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class AbstractPitMojo extends AbstractMojo {

  private final Predicate<MavenProject> notEmptyProject;
  
  private final Predicate<Artifact>   filter;

  private final PluginServices        plugins;

  /**
   * The current build session instance.
   */
  @Parameter(defaultValue = "${session}", readonly = true)
  private MavenSession session;


  // Concrete List types declared for all fields to work around maven 2 bug
  
  /**
   * Classes to include in mutation test
   */
  @Parameter(property = "targetClasses")
  private ArrayList<String>           targetClasses;

  /**
   * Tests to run
   */
  @Parameter(property = "targetTests")
  private ArrayList<String>           targetTests;

  /**
   * Methods not to mutate
   */
  @Parameter(property = "excludedMethods")
  private ArrayList<String>           excludedMethods;

  /**
   * Classes not to mutate
   */
  @Parameter(property = "excludedClasses")
  private ArrayList<String>           excludedClasses;
  
  /**
   * Classes not to run tests from
   */
  @Parameter(property = "excludedTestClasses")
  private ArrayList<String>           excludedTestClasses;


  /**
   * Globs to be matched against method calls. No mutations will be created on
   * the same line as a match.
   */
  @Parameter(property = "avoidCallsTo")
  private ArrayList<String>           avoidCallsTo;

  /**
   * Base directory where all reports are written to.
   */
  @Parameter(defaultValue = "${project.build.directory}/pit-reports", property = "reportsDirectory")
  private File                        reportsDirectory;

  /**
   * File to write history information to for incremental analysis
   */
  @Parameter(property = "historyOutputFile")
  private File                        historyOutputFile;

  /**
   * File to read history from for incremental analysis (can be same as output
   * file)
   */
  @Parameter(property = "historyInputFile")
  private File                        historyInputFile;

  /**
   * Convenience flag to read and write history to a local temp file.
   * 
   * Setting this flag is the equivalent to calling maven with -DhistoryInputFile=file -DhistoryOutputFile=file
   * 
   * Where file is a file named [groupid][artifactid][version]_pitest_history.bin in the temp directory
   * 
   */
  @Parameter(defaultValue = "false", property = "withHistory")
  private boolean                     withHistory;  

  /**
   * Number of threads to use
   */
  @Parameter(defaultValue = "1", property = "threads")
  private int                         threads;

  /**
   * Detect inlined code
   */
  @Parameter(defaultValue = "true", property = "detectInlinedCode")
  private boolean                     detectInlinedCode;

  /**
   * Mutation operators to apply
   */
  @Parameter(property = "mutators")
  private ArrayList<String>           mutators;
  
  /**
   * Features to activate/deactivate
   */
  @Parameter(property = "features")
  private ArrayList<String>           features;

  /**
   * Additional features activate/deactivate, use to
   * avoid overwriting features set in the build script when
   * specifying features from the command line
   */
  @Parameter(property = "extraFeatures")
  private ArrayList<String>           extraFeatures;


  /**
   * Weighting to allow for timeouts
   */
  @Parameter(defaultValue = "1.25", property = "timeoutFactor")
  private float                       timeoutFactor;

  /**
   * Constant factor to allow for timeouts 
   */
  @Parameter(defaultValue = "3000", property = "timeoutConstant")
  private long                        timeoutConstant;

  /**
   * Maximum number of mutations to allow per class
   */
  @Parameter(defaultValue = "-1", property = "maxMutationsPerClass")
  private int                         maxMutationsPerClass;

  /**
   * Arguments to pass to child processes
   */
  @Parameter(property = "jvmArgs")
  private ArrayList<String>           jvmArgs;

  /**
   * Single line commandline argument
   */
  @Parameter(property = "argLine")
  private String                      argLine;

  /**
   * Formats to output during analysis phase
   */
  @Parameter(property = "outputFormats")
  private ArrayList<String>           outputFormats;

  /**
   * Output verbose logging
   */
  @Parameter(defaultValue = "false", property = "verbose")
  private boolean                     verbose;

  /**
   * Throw error if no mutations found
   */
  @Parameter(defaultValue = "true", property = "failWhenNoMutations")
  private boolean                     failWhenNoMutations;

  /**
   * Create timestamped subdirectory for report
   */
  @Parameter(defaultValue = "false", property = "timestampedReports")
  private boolean                     timestampedReports;

  /**
   * TestNG Groups/JUnit Categories to exclude
   */
  @Parameter(property = "excludedGroups")
  private ArrayList<String>           excludedGroups;

  /**
   * TestNG Groups/JUnit Categories to include
   */
  @Parameter(property = "includedGroups")
  private ArrayList<String>           includedGroups;

  /**
   * Test methods that should be included for challenging the mutants
   */
  @Parameter(property = "includedTestMethods")
  private ArrayList<String>           includedTestMethods;

  /**
   * Whether to create a full mutation matrix.
   * 
   * If set to true all tests covering a mutation will be executed,
   * if set to false the test execution will stop after the first killing test.
   */
  @Parameter(property = "fullMutationMatrix", defaultValue = "false")

  private boolean                     fullMutationMatrix;
  /**
   * Maximum number of mutations to include in a single analysis unit.
   * 
   * If set to 1 will analyse very slowly, but with strong (jvm per mutant)
   * isolation.
   *
   */
  @Parameter(property = "mutationUnitSize")
  private int                         mutationUnitSize;

  /**
   * Export line coverage data
   */
  @Parameter(defaultValue = "false", property = "exportLineCoverage")
  private boolean                     exportLineCoverage;

  /**
   * Mutation score threshold at which to fail build
   */
  @Parameter(defaultValue = "0", property = "mutationThreshold")
  private int                         mutationThreshold;

  /**
   * Test strength score threshold at which to fail build
   */
  @Parameter(defaultValue = "0", property = "testStrengthThreshold")
  private int                         testStrengthThreshold;

  /**
   * Maximum surviving mutants to allow
   */
  @Parameter(defaultValue = "-1", property = "maxSurviving")
  private int                         maxSurviving = -1;
    
  /**
   * Line coverage threshold at which to fail build
   */
  @Parameter(defaultValue = "0", property = "coverageThreshold")
  private int                         coverageThreshold;

  /**
   * Path to java executable to use when running tests. Will default to
   * executable in JAVA_HOME if none set.
   */
  @Parameter
  private String                      jvm;

  /**
   * Engine to use when generating mutations.
   */
  @Parameter(defaultValue = "gregor", property = "mutationEngine")
  private String                      mutationEngine;

  /**
   * List of additional classpath entries to use when looking for tests and
   * mutable code. These will be used in addition to the classpath with which
   * PIT is launched.
   */
  @Parameter(property = "additionalClasspathElements")
  private ArrayList<String>           additionalClasspathElements;

  /**
   * List of classpath entries, formatted as "groupId:artifactId", which should
   * not be included in the classpath when running mutation tests. Modelled
   * after the corresponding Surefire/Failsafe property.
   */
  @Parameter(property = "classpathDependencyExcludes")
  private ArrayList<String>           classpathDependencyExcludes;
  
  /**
   * 
   */
  @Parameter(property = "excludedRunners")
  private ArrayList<String>           excludedRunners;

  /**
   * When set indicates that analysis of this project should be skipped
   */
  @Parameter(property = "skipPitest", defaultValue = "false")
  private boolean                     skip;

  /**
   * When set will try and create settings based on surefire configuration. This
   * may not give the desired result in some circumstances
   */
  @Parameter(defaultValue = "true")
  private boolean                     parseSurefireConfig;

  /**
   * When set will try and set the argLine based on surefire configuration. This
   * may not give the desired result in some circumstances
   */
  @Parameter(defaultValue = "true")
  private boolean                     parseSurefireArgLine;

  /**
   * honours common skipTests flag in a maven run
   */
  @Parameter(property = "skipTests", defaultValue = "false")
  private boolean                     skipTests;

  /**
   * When set will ignore failing tests when computing coverage. Otherwise, the
   * run will fail. If parseSurefireConfig is true, will be overridden from
   * surefire configuration property testFailureIgnore
   */
  @Parameter(defaultValue = "false")
  private boolean                     skipFailingTests;

  /**
   * Use slf4j for logging
   */
  @Parameter(defaultValue = "false", property = "useSlf4j")
  private boolean                     useSlf4j;

  @Parameter(property = "pit.inputEncoding", defaultValue = "${project.build.sourceEncoding}")
  private String inputEncoding;


  @Parameter(property = "pit.outputEncoding", defaultValue = "${project.reporting.outputEncoding}")
  private String outputEncoding;

  /**
   * The base directory of a multi-module project. Defaults to the execution
   * directory
   */
  @Parameter(defaultValue = "${session.executionRootDirectory}", property = "projectBase")
  private String                       projectBase;

  /**
   * Configuration properties.
   *
   * Value pairs may be used by pitest plugins.
   *
   */
  @Parameter
  private Map<String, String>         pluginConfiguration;


  /**
   * environment configuration
   *
   * Value pairs may be used by pitest plugins.
   */
  @Parameter
  private Map<String, String>         environmentVariables = new HashMap<>();

  /**
   * <i>Internal</i>: Project to interact with.
   *
   */
  @Parameter(property = "project", readonly = true, required = true)
  private MavenProject                project;

  /**
   * <i>Internal</i>: Map of plugin artifacts.
   */
  @Parameter(property = "plugin.artifactMap", readonly = true, required = true)
  private Map<String, Artifact>       pluginArtifactMap;
  
  
  /**
   * Communicate the classpath using a temporary jar with a classpath
   * manifest. This allows support of very large classpaths but may cause
   * issues with certain libraries.
   */
  @Parameter(property = "useClasspathJar", defaultValue = "false")
  private boolean                     useClasspathJar;

  /**
   * Amount of debug information/noise to output. The boolean
   * verbose flag overrides this value when it is set to true.
   */
  @Parameter(property = "verbosity", defaultValue = "DEFAULT")
  // should be able to use an enum here, but test harness is broken
  private String verbosity;

  private final GoalStrategy          goalStrategy;

  private final RepositorySystem repositorySystem;

  @Inject
  public AbstractPitMojo(RepositorySystem repositorySystem) {
    this(new RunPitStrategy(), new DependencyFilter(PluginServices.makeForLoader(
        AbstractPitMojo.class.getClassLoader())), PluginServices.makeForLoader(
        AbstractPitMojo.class.getClassLoader()), new NonEmptyProjectCheck(), repositorySystem);
  }

  public AbstractPitMojo(GoalStrategy strategy, Predicate<Artifact> filter,
      PluginServices plugins, Predicate<MavenProject> emptyProjectCheck, RepositorySystem repositorySystem) {
    this.goalStrategy = strategy;
    this.filter = filter;
    this.plugins = plugins;
    this.notEmptyProject = emptyProjectCheck;
    this.repositorySystem = repositorySystem;
  }

  @Override
  public final void execute() throws MojoExecutionException,
      MojoFailureException {

    switchLogging();
    RunDecision shouldRun = shouldRun();

    if (shouldRun.shouldRun()) {
      this.getLog().info("Root dir is : " + projectBase);
      for (ToolClasspathPlugin each : this.plugins
          .findToolClasspathPlugins()) {
          this.getLog().info("Found plugin : " + each.description());
      }

      this.plugins.findClientClasspathPlugins().stream()
              .filter(p -> !(p instanceof MethodMutatorFactory))
              .forEach(p -> this.getLog().info(
                      "Found shared classpath plugin : " + p.description()));

      String operators =  this.plugins.findMutationOperators().stream()
              .map(m -> m.getName())
              .collect(Collectors.joining(","));

      this.getLog().info("Available mutators : " + operators);

      final Optional<CombinedStatistics> result = analyse();
      if (result.isPresent()) {
        throwErrorIfTestStrengthBelowThreshold(result.get().getMutationStatistics());
        throwErrorIfScoreBelowThreshold(result.get().getMutationStatistics());
        throwErrorIfMoreThanMaximumSurvivors(result.get().getMutationStatistics());
        throwErrorIfCoverageBelowThreshold(result.get().getCoverageSummary());
      }

    } else {
      this.getLog().info("Skipping project because:");
      for (String reason : shouldRun.getReasons()) {
        this.getLog().info("  - " + reason);
      }
    }
  }

  private void switchLogging() {
    if (this.useSlf4j) {
      SLF4JBridgeHandler.removeHandlersForRootLogger();
      SLF4JBridgeHandler.install();
      Logger.getLogger("PIT").addHandler(new SLF4JBridgeHandler());
      SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();
    }
  }

  private void throwErrorIfCoverageBelowThreshold(
      final CoverageSummary coverageSummary) throws MojoFailureException {
    if ((this.coverageThreshold != 0)
        && (coverageSummary.getCoverage() < this.coverageThreshold)) {
      throw new MojoFailureException("Line coverage of "
          + coverageSummary.getCoverage() + "("
          + coverageSummary.getNumberOfCoveredLines() + "/"
          + coverageSummary.getNumberOfLines() + ") is below threshold of "
          + this.coverageThreshold);
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

  private void throwErrorIfTestStrengthBelowThreshold(final MutationStatistics result)
          throws MojoFailureException {
    if ((this.testStrengthThreshold != 0)
            && (result.getTestStrength() < this.testStrengthThreshold)) {
      throw new MojoFailureException("Test strength score of "
              + result.getTestStrength() + " is below threshold of "
              + this.testStrengthThreshold);
    }
  }
  
  private void throwErrorIfMoreThanMaximumSurvivors(final MutationStatistics result)
      throws MojoFailureException {
    if ((this.maxSurviving >= 0)
        && (result.getTotalSurvivingMutations() > this.maxSurviving)) {
      throw new MojoFailureException("Had "
          + result.getTotalSurvivingMutations() + " surviving mutants, but only "
          + this.maxSurviving + " survivors allowed");
    }
  }

  protected Optional<CombinedStatistics> analyse() throws MojoExecutionException {
    final ReportOptions data = new MojoToReportOptionsConverter(this,
        new SurefireConfigConverter(this.isParseSurefireArgLine()), this.filter).convert();

    // overwrite variable from surefire with any explicitly set
    // for pitest / add additional values
    data.getEnvironmentVariables().putAll(this.environmentVariables);

    return Optional.ofNullable(this.goalStrategy.execute(detectBaseDir(), data,
        this.plugins, data.getEnvironmentVariables()));
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

  protected Predicate<Artifact> getFilter() {
    return filter;
  }

  protected GoalStrategy getGoalStrategy() {
    return goalStrategy;
  }

  protected PluginServices getPlugins() {
    return plugins;
  }

  public List<String> getTargetClasses() {
    return withoutNulls(this.targetClasses);
  }

  public void setTargetClasses(ArrayList<String> targetClasses) {
    this.targetClasses = targetClasses;
  }

  public List<String> getTargetTests() {
    return withoutNulls(this.targetTests);
  }

  public void setTargetTests(ArrayList<String> targetTests) {
    this.targetTests = targetTests;
  }

  public List<String> getExcludedMethods() {
    return withoutNulls(this.excludedMethods);
  }

  public List<String> getExcludedClasses() {
    return withoutNulls(this.excludedClasses);
  }

  public List<String> getAvoidCallsTo() {
    return withoutNulls(this.avoidCallsTo);
  }

  public File getReportsDirectory() {
    return this.reportsDirectory;
  }

  public int getThreads() {
    return this.threads;
  }

  public List<String> getMutators() {
    return withoutNulls(this.mutators);
  }

  public float getTimeoutFactor() {
    return this.timeoutFactor;
  }

  public long getTimeoutConstant() {
    return this.timeoutConstant;
  }

  public ArrayList<String> getExcludedTestClasses() {
    return withoutNulls(excludedTestClasses);
  }

  public int getMaxMutationsPerClass() {
    return this.maxMutationsPerClass;
  }

  public List<String> getJvmArgs() {
    return withoutNulls(this.jvmArgs);
  }

  public String getArgLine() {
    return argLine;
  }

  public List<String> getOutputFormats() {
    return withoutNulls(this.outputFormats);
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

  public List<String> getExcludedGroups() {
    return withoutNulls(this.excludedGroups);
  }

  public List<String> getIncludedGroups() {
    return withoutNulls(this.includedGroups);
  }

  public List<String> getIncludedTestMethods() {
    return withoutNulls(this.includedTestMethods);
  }

  public boolean isFullMutationMatrix() {
    return fullMutationMatrix;
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

  public Charset getSourceEncoding() {
    if (inputEncoding != null) {
      return Charset.forName(inputEncoding);
    }
    return Charset.defaultCharset();
  }

  public Charset getOutputEncoding() {
    if (outputEncoding != null) {
      return Charset.forName(outputEncoding);
    }
    return Charset.defaultCharset();
  }

  protected RunDecision shouldRun() {
    RunDecision decision = new RunDecision();

    if (this.skip) {
      decision.addReason("Execution of PIT should be skipped.");
    }

    if (this.skipTests) {
      decision.addReason("Test execution should be skipped (-DskipTests).");
    }

    if ("pom".equalsIgnoreCase(this.project.getPackaging())) {
      decision.addReason("Packaging is POM.");
    }

    if (!notEmptyProject.test(project)) {
      decision.addReason("Project has either no tests or no production code.");
    }

    return decision;
  }

  public String getMutationEngine() {
    return this.mutationEngine;
  }

  public String getJavaExecutable() {
    return this.jvm;
  }

  public List<String> getAdditionalClasspathElements() {
    return withoutNulls(this.additionalClasspathElements);
  }

  public List<String> getClasspathDependencyExcludes() {
    return withoutNulls(this.classpathDependencyExcludes);
  }

  public boolean isParseSurefireConfig() {
    return this.parseSurefireConfig;
  }

  public boolean isParseSurefireArgLine() {
    return this.parseSurefireArgLine;
  }

  public boolean skipFailingTests() {
    return this.skipFailingTests;
  }

  public Map<String, String> getPluginProperties() {
    return pluginConfiguration;
  }

  public Map<String, String> getEnvironmentVariables() {
    return environmentVariables;
  }

  public boolean useHistory() {
    return this.withHistory;
  }

  public ArrayList<String> getExcludedRunners() {
    return withoutNulls(excludedRunners);
  }
  
  public ArrayList<String> getFeatures() {
    ArrayList<String> consolidated = emptyWithoutNulls(features);
    consolidated.addAll(emptyWithoutNulls(extraFeatures));
    return consolidated;
  }

  public boolean isUseClasspathJar() {
    return this.useClasspathJar;
  }

  public String getVerbosity() {
    return verbosity;
  }

  public String getProjectBase() {
    return projectBase;
  }

  public MavenSession session() {
    return session;
  }

  public RepositorySystem repositorySystem() {
    return repositorySystem;
  }

  static class RunDecision {
    private List<String> reasons = new ArrayList<>(4);

    boolean shouldRun() {
      return reasons.isEmpty();
    }

    public void addReason(String reason) {
      reasons.add(reason);
    }

    public List<String> getReasons() {
      return Collections.unmodifiableList(reasons);
    }
  }

  private <X> ArrayList<X> emptyWithoutNulls(List<X> originalList) {
    if (originalList == null) {
      return new ArrayList<>();
    }

    return withoutNulls(originalList);
  }

  private <X> ArrayList<X> withoutNulls(List<X> originalList) {
    if (originalList == null) {
      return null;
    }

    return originalList.stream()
        .filter(Objects::nonNull)
        .collect(Collectors.toCollection(ArrayList::new));
  }

}
