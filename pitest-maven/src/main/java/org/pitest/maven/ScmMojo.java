package org.pitest.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.StringUtils;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.tooling.CombinedStatistics;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Goal which runs a coverage mutation report only for files that have been
 * modified or introduced locally based on the source control configured in
 * maven.
 *
 * @goal scmMutationCoverage
 *
 * @requiresDependencyResolution test
 *
 * @phase integration-test
 */
public class ScmMojo extends PitMojo {

  /**
   * @component
   */
  private ScmManager  manager;

  /**
   * List of scm status to include. Names match those defined by the maven scm
   * plugin.
   *
   * Common values include ADDED,MODIFIED (the defaults) & UNKNOWN.
   *
   * @parameter expression="${include}"
   */
  private HashSet<String> include;

  /**
   * Connection type to use when querying scm for changed files. Can either be
   * "connection" or "developerConnection".
   *
   * @parameter default-value="connection" expression="${connectionType}"
   */
  private String      connectionType;

  /**
   * Project basedir
   *
   * @parameter expression="${basedir}"
   * @required
   */
  private File        basedir;

  /**
   * Base of scm root. For a multi module project this is probably the parent
   * project.
   *
   * @parameter expression="${project.parent.basedir}"
   */
  private File        scmRootDir;

  public ScmMojo( RunPitStrategy executionStrategy,
                  ScmManager manager,
                  Predicate<Artifact> filter,
                  PluginServices plugins) {
    super(executionStrategy, filter, plugins);
    this.manager = manager;
  }

  public ScmMojo() {

  }

  @Override
  protected Option<CombinedStatistics> analyse() throws MojoExecutionException {
    targetClasses = makeConcreteList(findModifiedClassNames());

    if (targetClasses.isEmpty()) {
      getLog().info("No locally modified files found - nothing to mutation test");
      return Option.none();
    }

    logClassNames();
    defaultTargetTestsToGroupNameIfNoValueSet();
    ReportOptions data = new MojoToReportOptionsConverter(this, new SurefireConfigConverter(),filter).convert();
    data.setFailWhenNoMutations(false);

    return Option.some(goalStrategy.execute(detectBaseDir(), data, plugins,new HashMap<String, String>()));
  }

  private void defaultTargetTestsToGroupNameIfNoValueSet() {
    if (getTargetTests() == null) {
      targetTests = makeConcreteList(Collections.singletonList(this.getProject()
          .getGroupId() + "*"));
    }
  }

  private void logClassNames() {
    for (String each : targetClasses) {
      getLog().info("Will mutate locally changed class " + each);
    }
  }

  private List<String> findModifiedClassNames() throws MojoExecutionException {

    File sourceRoot = new File(project.getBuild().getSourceDirectory());

    List<String> modifiedPaths = findModifiedPaths();
    PathToJavaClassConverter converter = new PathToJavaClassConverter(sourceRoot.getAbsolutePath());
    return FCollection.flatMap(modifiedPaths,converter);
  }

  private List<String> findModifiedPaths() throws MojoExecutionException {
    try {
      Set<ScmFileStatus> statusToInclude = makeStatusSet();
      List<String> modifiedPaths = new ArrayList<String>();
      ScmRepository repository = manager.makeScmRepository(getSCMConnection());
      File scmRoot = scmRoot();
      getLog().info("Scm root dir is " + scmRoot);
      StatusScmResult status = manager.status(repository,new ScmFileSet(scmRoot));

      for (ScmFile file : status.getChangedFiles()) {
        if (statusToInclude.contains(file.getStatus())) {
          modifiedPaths.add(file.getPath());
        }
      }
      return modifiedPaths;
    } catch (ScmException e) {
      throw new MojoExecutionException("Error while querying scm", e);
    }
  }

  private Set<ScmFileStatus> makeStatusSet() {
    if ((include == null) || include.isEmpty()) {
      return new HashSet<ScmFileStatus>(Arrays.asList(ScmStatus.ADDED.getStatus(), ScmStatus.MODIFIED.getStatus()));
    }
    Set<ScmFileStatus> s = new HashSet<ScmFileStatus>();
    FCollection.mapTo(include, stringToMavenScmStatus(), s);
    return s;
  }

  private static F<String, ScmFileStatus> stringToMavenScmStatus() {
    return new F<String, ScmFileStatus>() {
      public ScmFileStatus apply(String a) {
        return ScmStatus.valueOf(a.toUpperCase()).getStatus();
      }
    };
  }

  private File scmRoot() {
    if (scmRootDir != null) {
      return scmRootDir;
    }
    return basedir;
  }

  private String getSCMConnection() throws MojoExecutionException {

    if (project.getScm() == null) {
      throw new MojoExecutionException("No SCM Connection configured.");
    }

    String scmConnection = project.getScm().getConnection();
    if ("connection".equalsIgnoreCase(connectionType) && StringUtils.isNotEmpty(scmConnection)) {
      return scmConnection;
    }

    String scmDeveloper = project.getScm().getDeveloperConnection();
    if ("developerconnection".equalsIgnoreCase(connectionType) && StringUtils.isNotEmpty(scmDeveloper)) {
      return scmDeveloper;
    }

    throw new MojoExecutionException("SCM Connection is not set.");
  }

  public void setConnectionType(String connectionType) {
    this.connectionType = connectionType;
  }

  public void setScmRootDir(File scmRootDir) {
    this.scmRootDir = scmRootDir;
  }

  /**
   * A bug in maven 2 requires that all list fields
   * declare a concrete list type
   */
  private static ArrayList<String> makeConcreteList(List<String> list) {
    return new ArrayList<String>(list);
  }

}
