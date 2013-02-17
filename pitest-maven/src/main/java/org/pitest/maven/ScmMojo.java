package org.pitest.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.StringUtils;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.ReportOptions;

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
  private ScmManager manager;

  /**
   * Connection type to use when querying scm for changed files. Can either be
   * "connection" or "developerConnection".
   * 
   * @parameter default-value="connection"
   */
  private String     connectionType;

  /**
   * Project basedir
   * 
   * @parameter expression="${basedir}"
   * @required
   */
  private File       basedir;

  /**
   * Base of scm root. For a multi module project this is probably the parent
   * project.
   * 
   * @parameter expression="${project.parent.basedir}"
   */
  private File       scmRootDir;

  public ScmMojo(final RunPitStrategy executionStrategy,
      final ScmManager manager) {
    super(executionStrategy);
    this.manager = manager;
  }

  public ScmMojo() {

  }

  @Override
  public void execute() throws MojoExecutionException {

    this.targetClasses = findModifiedClassNames();

    if (this.targetClasses.isEmpty()) {
      this.getLog().info(
          "No locally modified files found - nothing to mutation test");
    } else {
      logClassNames();
      defaultTargetTestsToGroupNameIfNoValueSet();
      final ReportOptions data = new MojoToReportOptionsConverter(this)
          .convert();
      data.setFailWhenNoMutations(false);

      this.goalStrategy.execute(detectBaseDir(), data);
    }
  }

  private void defaultTargetTestsToGroupNameIfNoValueSet() {
    if (this.getTargetTests() == null) {
      this.targetTests = Collections.singletonList(this.getProject()
          .getGroupId() + "*");
    }
  }

  private void logClassNames() {
    for (final String each : this.targetClasses) {
      this.getLog().info("Will mutate locally changed class " + each);
    }
  }

  private List<String> findModifiedClassNames() throws MojoExecutionException {

    final File sourceRoot = new File(this.project.getBuild()
        .getSourceDirectory());

    final List<String> modifiedPaths = findModifiedPaths();
    return FCollection.flatMap(modifiedPaths, new PathToJavaClassConverter(
        sourceRoot.getAbsolutePath()));

  }

  private List<String> findModifiedPaths() throws MojoExecutionException {
    try {
      final List<String> modifiedPaths = new ArrayList<String>();
      final ScmRepository repository = this.manager
          .makeScmRepository(getSCMConnection());
      final StatusScmResult status = this.manager.status(repository,
          new ScmFileSet(scmRoot()));

      for (final ScmFile file : status.getChangedFiles()) {
        if (file.getStatus().equals(ScmFileStatus.ADDED)
            || file.getStatus().equals(ScmFileStatus.MODIFIED)) {
          modifiedPaths.add(file.getPath());
        }
      }
      return modifiedPaths;
    } catch (final ScmException e) {
      throw new MojoExecutionException("Error while querying scm", e);
    }

  }

  private File scmRoot() {
    if (this.scmRootDir != null) {
      return this.scmRootDir;
    }
    return this.basedir;
  }

  private String getSCMConnection() throws MojoExecutionException {

    if (this.project.getScm() == null) {
      throw new MojoExecutionException("No SCM Connection configured.");
    }

    final String scmConnection = this.project.getScm().getConnection();
    if ("connection".equalsIgnoreCase(this.connectionType)
        && StringUtils.isNotEmpty(scmConnection)) {
      return scmConnection;
    }

    final String scmDeveloper = this.project.getScm().getDeveloperConnection();
    if ("developerconnection".equalsIgnoreCase(this.connectionType)
        && StringUtils.isNotEmpty(scmDeveloper)) {
      return scmDeveloper;
    }

    throw new MojoExecutionException("SCM Connection is not set.");

  }

  public void setConnectionType(final String connectionType) {
    this.connectionType = connectionType;
  }

  public void setScmRootDir(final File scmRootDir) {
    this.scmRootDir = scmRootDir;
  }

}
