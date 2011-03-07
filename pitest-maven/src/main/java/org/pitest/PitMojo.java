package org.pitest;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.predicate.Predicate;
import org.pitest.internal.ClassPath;
import org.pitest.internal.IsolationUtils;
import org.pitest.internal.classloader.DefaultPITClassloader;
import org.pitest.mutationtest.CodeCentricReport;
import org.pitest.mutationtest.DefaultMutationConfigFactory;
import org.pitest.mutationtest.HtmlReportFactory;
import org.pitest.mutationtest.MutationCoverageReport;
import org.pitest.mutationtest.Mutator;
import org.pitest.mutationtest.ReportOptions;
import org.pitest.mutationtest.TestCentricReport;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.instrument.KnownLocationJavaAgentJarFinder;
import org.pitest.util.Glob;

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
   * @parameter
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
   * Run in test centric mode
   * 
   * @parameter default-value="false"
   */
  private boolean               testCentric;

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

  @SuppressWarnings("unchecked")
  public void execute() throws MojoExecutionException {
    final Set<String> classPath = new HashSet<String>();

    try {
      classPath.addAll(this.project.getTestClasspathElements());
      classPath.addAll(this.project.getCompileClasspathElements());
      classPath.addAll(this.project.getRuntimeClasspathElements());
      classPath.addAll(this.project.getSystemClasspathElements());

    } catch (final DependencyResolutionRequiredException e1) {
      getLog().info(e1);
      e1.printStackTrace();
    }

    final Artifact pitVersionInfo = this.pluginArtifactMap
        .get("org.pitest:pitest");

    addOwnDependenciesToClassPath(classPath);

    final ReportOptions data = new ReportOptions();
    data.setClassPathElements(classPath);
    data.setIsTestCentric(false);
    data.setDependencyAnalysisMaxDistance(this.maxDependencyDistance);
    data.setIncludeJarFiles(this.includeJarFiles);

    data.setTargetClasses(determineTargetClasses());
    data.setTargetTests(determineTargetTests());
    data.setClassesInScope(determineClassesInScope());
    data.setMutateStaticInitializers(this.mutateStaticInitializers);
    data.setNumberOfThreads(this.threads);

    data.setReportDir(this.reportsDirectory.getAbsolutePath());

    data.setMutators(determineMutators());
    data.setIsTestCentric(this.testCentric);
    data.setTimeoutConstant(this.timeoutConstant);
    data.setTimeoutFactor(this.timeoutFactor);

    final List<String> sourceRoots = new ArrayList<String>();
    sourceRoots.addAll(this.project.getCompileSourceRoots());
    sourceRoots.addAll(this.project.getTestCompileSourceRoots());

    data.setSourceDirs(stringsTofiles(sourceRoots));

    System.out.println("Running report with " + data);

    final MutationCoverageReport report = pickReportType(data, pitVersionInfo);

    // FIXME will we get a clash between junit & possibly PIT jars by using the
    // plugin loader?
    final ClassLoader loader = new DefaultPITClassloader(data
        .getClassPath(true).getOrElse(new ClassPath()),
        IsolationUtils.getContextClassLoader());
    final ClassLoader original = IsolationUtils.getContextClassLoader();

    try {
      IsolationUtils.setContextClassLoader(loader);

      final Runnable run = (Runnable) IsolationUtils.cloneForLoader(report,
          loader);

      run.run();

    } catch (final Exception e) {
      throw new MojoExecutionException("fail", e);
    } finally {
      IsolationUtils.setContextClassLoader(original);
    }
  }

  private Collection<Predicate<String>> determineTargetTests() {
    return FCollection.map(this.targetTests, Glob.toGlobPredicate());
  }

  private void addOwnDependenciesToClassPath(final Set<String> classPath) {
    for (final Artifact dependency : this.pluginArtifactMap.values()) {
      classPath.add(dependency.getFile().getAbsolutePath());
    }
  }

  private MutationCoverageReport pickReportType(final ReportOptions data,
      final Artifact pitVersionInfo) {
    if (!this.testCentric) {
      return new CodeCentricReport(data, new KnownLocationJavaAgentJarFinder(
          pitVersionInfo.getFile().getAbsolutePath()), new HtmlReportFactory(),
          true);
    } else {
      return new TestCentricReport(data, new KnownLocationJavaAgentJarFinder(
          pitVersionInfo.getFile().getAbsolutePath()), new HtmlReportFactory(),
          true);
    }
  }

  private Collection<MethodMutatorFactory> determineMutators() {
    if (this.mutators != null) {
      return FCollection.map(this.mutators, stringToMutator());
    } else {
      return DefaultMutationConfigFactory.DEFAULT_MUTATORS;
    }
  }

  private F<String, MethodMutatorFactory> stringToMutator() {
    return new F<String, MethodMutatorFactory>() {
      public Mutator apply(final String a) {
        return Mutator.valueOf(a);
      }

    };
  }

  private Collection<Predicate<String>> determineClassesInScope() {
    return returnOrDefaultToClassesLikeGroupName(this.inScopeClasses);
  }

  private Collection<Predicate<String>> determineTargetClasses() {
    return returnOrDefaultToClassesLikeGroupName(this.targetClasses);
  }

  private Collection<Predicate<String>> returnOrDefaultToClassesLikeGroupName(
      final Collection<String> filters) {
    if (filters == null) {
      return Collections.<Predicate<String>> singleton(new Glob(this.project
          .getGroupId() + "*"));
    } else {
      return FCollection.map(filters, Glob.toGlobPredicate());
    }
  }

  private Collection<File> stringsTofiles(final List<String> sourceRoots) {
    return FCollection.map(sourceRoots, stringToFile());
  }

  private F<String, File> stringToFile() {
    return new F<String, File>() {
      public File apply(final String a) {
        return new File(a);
      }

    };
  }
}
