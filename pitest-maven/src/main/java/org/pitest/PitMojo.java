package org.pitest;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.pitest.mutationtest.ReportOptions;

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

  /**
   * Classes to include in mutation test
   * 
   * @parameter
   * 
   */
  private List<String>          targetClasses;

  /**
   * Tests to run
   * 
   * @parameter
   * 
   */
  private List<String>          targetTests;

  /**
   * Methods not to mutate
   * 
   * @parameter
   * 
   */
  private List<String>          excludedMethods;

  /**
   * Classes not to mutate or run tests from
   * 
   * @parameter
   * 
   */
  private List<String>          excludedClasses;

  /**
   * 
   * @parameter
   * 
   */
  private List<String>          avoidCallsTo;

  /**
   * Classes in scope for dependency and coverage analysis
   * 
   * @parameter
   * 
   */
  private List<String>          inScopeClasses;

  /**
   * Base directory where all reports are written to.
   * 
   * @parameter default-value="${project.build.directory}/pit-reports"
   */
  private File                  reportsDirectory;

  /**
   * Maximum distance to look from test to class. Relevant when mutating static
   * initializers
   * 
   * @parameter default-value="-1"
   */
  private int                   maxDependencyDistance;

  /**
   * Number of threads to use
   * 
   * @parameter default-value="1"
   */
  private int                   threads;

  /**
   * Mutate static initializers
   * 
   * @parameter default-value="false"
   */
  private boolean               mutateStaticInitializers;

  /**
   * Mutate classes within jar files and other archives
   * 
   * @parameter default-value="false"
   */
  private boolean               includeJarFiles;

  /**
   * Maximum distance to look from test to class
   * 
   * @parameter
   */
  private List<String>          mutators;

  /**
   * Weighting to allow for timeouts
   * 
   * @parameter default-value="1.25"
   */
  private float                 timeoutFactor;

  /**
   * Constant factor to allow for timeouts
   * 
   * @parameter default-value="3000"
   */
  private long                  timeoutConstant;

  /**
   * Maximum number of mutations to allow per class
   * 
   * @parameter default-value="-1"
   */
  private int                   maxMutationsPerClass;

  /**
   * Arguments to pass to child processes
   * 
   * @parameter
   */
  private List<String>          jvmArgs;

  /**
   * Formats to output during analysis phase
   * 
   * @parameter
   */
  private List<String>          outputFormats;

  /**
   * Output verbose logging
   * 
   * @parameter default-value="false"
   */
  private boolean               verbose;

  /**
   * Throw error if no mutations found
   * 
   * @parameter default-value="true"
   */
  private boolean               failWhenNoMutations;

  /**
   * Type of tests to look for
   * 
   * @parameter default-value="JUNIT"
   */
  private String                testType;

  /**
   * TestNG Groups to exclude
   * 
   * @parameter
   */
  private List<String>          excludedTestNGGroups;

  /**
   * TestNG Groups to include
   * 
   * @parameter
   */
  private List<String>          includedTestNGGroups;

  /**
   * <i>Internal</i>: Project to interact with.
   * 
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  private MavenProject          project;

  /**
   * <i>Internal</i>: Map of plugin artifacts.
   * 
   * @parameter expression="${plugin.artifactMap}"
   * @required
   * @readonly
   */
  private Map<String, Artifact> pluginArtifactMap;

  /**
   * Location of the local repository.
   * 
   * @parameter expression="${localRepository}"
   * @readonly
   * @required
   */
  protected ArtifactRepository  localRepository;

  /**
   * Used to look up Artifacts in the remote repository.
   * 
   * @parameter expression=
   *            "${component.org.apache.maven.artifact.factory.ArtifactFactory}"
   * @required
   * @readonly
   */
  protected ArtifactFactory     factory;

  private final GoalStrategy    goalStrategy;

  public PitMojo() {
    this(new RunPitStrategy());
  }

  public PitMojo(final GoalStrategy strategy) {
    this.goalStrategy = strategy;
  }

  public void execute() throws MojoExecutionException {

    final ReportOptions data = new MojoToReportOptionsConverter(this).convert();
    this.goalStrategy.execute(data);

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

  public List<String> getInScopeClasses() {
    return this.inScopeClasses;
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

  public boolean isIncludeJarFiles() {
    return this.includeJarFiles;
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

  public ArtifactRepository getLocalRepository() {
    return this.localRepository;
  }

  public ArtifactFactory getFactory() {
    return this.factory;
  }

  public boolean isFailWhenNoMutations() {
    return this.failWhenNoMutations;
  }

  public String getTestType() {
    return this.testType;
  }

  public List<String> getExcludedTestNGGroups() {
    return this.excludedTestNGGroups;
  }

  public List<String> getIncludedTestNGGroups() {
    return this.includedTestNGGroups;
  }

}
