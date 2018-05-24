package org.pitest.maven;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.apache.maven.model.Build;
import org.apache.maven.model.Scm;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.scm.ChangeFile;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.command.changelog.ChangeLogScmRequest;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;

public class ScmMojoTest extends BasePitMojoTest {

  private ScmMojo       testee;

  @Mock
  private Build         build;

  @Mock
  private Scm           scm;

  @Mock
  private ScmManager    manager;

  @Mock
  private ScmRepository repository;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    this.testee = new ScmMojo(this.executionStrategy, this.manager,
        this.filter, this.plugins, false,  i -> true);
    this.testee.setScmRootDir(new File("foo"));
    when(this.project.getBuild()).thenReturn(this.build);
    when(this.project.getParent()).thenReturn(null);
    when(this.project.getBasedir()).thenReturn(new File("."));
    when(this.build.getSourceDirectory()).thenReturn("foo");
    when(this.build.getOutputDirectory()).thenReturn("foo");
    when(this.build.getTestOutputDirectory()).thenReturn("foo");
    when(this.project.getScm()).thenReturn(this.scm);
    when(this.manager.makeScmRepository(any(String.class))).thenReturn(
        this.repository);
    configurePitMojo(this.testee, createPomWithConfiguration(""));
  }

  public void testThrowsAnExceptionWhenNoScmConfigured() throws Exception {
    try {
      when(this.project.getScm()).thenReturn(null);
      this.testee.execute();
      fail("Exception expected");
    } catch (final MojoExecutionException ex) {
      assertEquals("No SCM Connection configured.", ex.getMessage());
    }
  }

  public void testUsesCorrectConnectionWhenDeveloperConnectionSet()
      throws Exception {
    final String devUrl = "devcon";
    when(this.scm.getDeveloperConnection()).thenReturn(devUrl);
    setupToReturnNoModifiedFiles();
    this.testee.setConnectionType("developerconnection");
    this.testee.execute();
    verify(this.manager).makeScmRepository(devUrl);

  }

  public void testUsesCorrectConnectionWhenNonDeveloperConnectionSet()
      throws Exception {
    final String url = "prodcon";
    when(this.scm.getConnection()).thenReturn(url);
    setupToReturnNoModifiedFiles();
    this.testee.setConnectionType("connection");
    this.testee.execute();
    verify(this.manager).makeScmRepository(url);

  }

  public void testClassesAddedToScmAreMutationTested() throws Exception {
    setupConnection();
    setFileWithStatus(ScmFileStatus.ADDED);
    this.testee.execute();
    verify(this.executionStrategy).execute(any(File.class),
        any(ReportOptions.class), any(PluginServices.class), anyMap());
  }

  private void setFileWithStatus(final ScmFileStatus status)
      throws ScmException {
    when(this.manager.status(any(ScmRepository.class), any(ScmFileSet.class)))
        .thenReturn(
            new StatusScmResult("", Arrays.asList(new ScmFile(
                "foo/bar/Bar.java", status))));
  }

  public void testModifiedClassesAreMutationTested() throws Exception {
    setupConnection();
    setFileWithStatus(ScmFileStatus.MODIFIED);
    this.testee.execute();
    verify(this.executionStrategy).execute(any(File.class),
        any(ReportOptions.class), any(PluginServices.class), anyMap());
  }

  public void testLastCommitIsMutationTested() throws Exception {
    setupConnection();
    configurePitMojo(
            this.testee,
            createPomWithConfiguration("<analyseLastCommit>true</analyseLastCommit>"));
    givenChangeLogWithLastCommit();
    this.testee.execute();
    verify(this.executionStrategy).execute(any(File.class),
            any(ReportOptions.class), any(PluginServices.class), anyMap());
  }

  private void givenChangeLogWithLastCommit() throws ScmException {
    when(this.manager.changeLog(any(ChangeLogScmRequest.class)))
            .thenReturn(new ChangeLogScmResult("", new ChangeLogSet(Arrays.asList(aChangeSetWithAddedFile()),
                    new Date(), new Date())));
  }

  private ChangeSet aChangeSetWithAddedFile() {
    return new ChangeSet(new Date(), "", "", Arrays.asList(aChangeFile(ScmFileStatus.ADDED)));
  }

  private ChangeFile aChangeFile(ScmFileStatus fileStatus) {
    ChangeFile changeFile = new ChangeFile("foo/bar/Bar.java");
    changeFile.setAction(fileStatus);
    return changeFile;
  }

  public void testUnknownAndDeletedClassesAreNotMutationTested()
      throws Exception {
    setupConnection();
    when(this.manager.status(any(ScmRepository.class), any(ScmFileSet.class)))
        .thenReturn(
            new StatusScmResult("", Arrays.asList(new ScmFile(
                "foo/bar/Bar.java", ScmFileStatus.DELETED), new ScmFile(
                "foo/bar/Bar.java", ScmFileStatus.UNKNOWN))));
    this.testee.execute();
    verify(this.executionStrategy, never()).execute(any(File.class),
        any(ReportOptions.class), any(PluginServices.class), anyMap());
  }

  public void testCanOverrideInspectedStatus() throws Exception {
    setupConnection();
    setFileWithStatus(ScmFileStatus.UNKNOWN);
    configurePitMojo(
        this.testee,
        createPomWithConfiguration("<include><value>DELETED</value><value>UNKNOWN</value></include>"));
    this.testee.execute();
    verify(this.executionStrategy, times(1)).execute(any(File.class),
        any(ReportOptions.class), any(PluginServices.class), anyMap());
  }

  public void testDoesNotAnalysePomProjects() throws Exception {
    setupConnection();
    setFileWithStatus(ScmFileStatus.MODIFIED);
    when(this.project.getPackaging()).thenReturn("pom");
    this.testee.execute();
    verify(this.executionStrategy, never()).execute(any(File.class),
        any(ReportOptions.class), any(PluginServices.class), anyMap());
  }

  private void setupConnection() {
    when(this.scm.getConnection()).thenReturn("url");
    this.testee.setConnectionType("connection");
  }

  private void setupToReturnNoModifiedFiles() throws ScmException {
    when(this.manager.status(any(ScmRepository.class), any(ScmFileSet.class)))
        .thenReturn(new StatusScmResult("", Collections.<ScmFile> emptyList()));
  }

  private Map<String, String> anyMap() {
    return Matchers.<Map<String, String>> any();
  }
}