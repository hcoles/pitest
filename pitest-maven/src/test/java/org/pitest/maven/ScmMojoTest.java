package org.pitest.maven;

import org.apache.maven.model.Build;
import org.apache.maven.model.Scm;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.ReflectionUtils;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

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
    testee = new ScmMojo(executionStrategy, manager, filter, plugins);
    testee.setScmRootDir(new File("foo"));

    Set<String> includeScmStatuses = new HashSet<String>();
    includeScmStatuses.add("ADDED");
    includeScmStatuses.add("MODIFIED");

    ReflectionUtils.setVariableValueInObject(testee,"include",includeScmStatuses);

    when(project.getBuild()).thenReturn(build);
    when(build.getSourceDirectory()).thenReturn("foo");
    when(build.getOutputDirectory()).thenReturn("foo");
    when(project.getScm()).thenReturn(scm);
    when(manager.makeScmRepository(any(String.class))).thenReturn(repository);

    configurePitMojo(testee, createPomWithConfiguration(""));
  }

  public void testThrowsAnExceptionWhenNoScmConfigured() throws Exception {
    try {
      when(project.getScm()).thenReturn(null);
      testee.execute();
      fail("Exception expected");
    } catch (MojoExecutionException ex) {
      assertEquals("No SCM Connection configured.", ex.getMessage());
    }
  }

  public void testUsesCorrectConnectionWhenDeveloperConnectionSet()
      throws Exception {
    String devUrl = "devcon";
    when(scm.getDeveloperConnection()).thenReturn(devUrl);
    setupToReturnNoModifiedFiles();
    testee.setConnectionType("developerconnection");
    testee.execute();
    verify(manager).makeScmRepository(devUrl);

  }

  public void testUsesCorrectConnectionWhenNonDeveloperConnectionSet()
      throws Exception {
    String url = "prodcon";
    when(scm.getConnection()).thenReturn(url);
    setupToReturnNoModifiedFiles();
    testee.setConnectionType("connection");
    testee.execute();
    verify(manager).makeScmRepository(url);

  }

  public void testClassesAddedToScmAreMutationTested() throws Exception {
    setupConnection();
    setFileWithStatus(ScmFileStatus.ADDED);

    testee.execute();

    verify(executionStrategy).execute(  any(File.class),
                                        any(ReportOptions.class),
                                        any(PluginServices.class),
                                        anyMap());
  }

  private void setFileWithStatus(ScmFileStatus status) throws ScmException {
    ScmFile scmFile = new ScmFile("foo/bar/Bar.java", status);
    when(manager.status(any(ScmRepository.class),any(ScmFileSet.class)))
                .thenReturn(new StatusScmResult("",Arrays.asList(scmFile)));
  }

  public void testModifiedClassesAreMutationTested() throws Exception {
    setupConnection();
    setFileWithStatus(ScmFileStatus.MODIFIED);

    testee.execute();

    verify(executionStrategy).execute( any(File.class),
                                       any(ReportOptions.class),
                                       any(PluginServices.class),
                                       anyMap());
  }

  public void testUnknownAndDeletedClassesAreNotMutationTested()
      throws Exception {
    setupConnection();
    when(manager.status(any(ScmRepository.class), any(ScmFileSet.class)))
        .thenReturn(
            new StatusScmResult("", Arrays.asList(new ScmFile(
                "foo/bar/Bar.java", ScmFileStatus.DELETED), new ScmFile(
                "foo/bar/Bar.java", ScmFileStatus.UNKNOWN))));
    testee.execute();
    verify(executionStrategy, never()).execute(any(File.class),
        any(ReportOptions.class), any(PluginServices.class),anyMap());
  }

  public void testCanOverrideInspectedStatus() throws Exception {
    setupConnection();
    setFileWithStatus(ScmFileStatus.UNKNOWN);
    configurePitMojo(
        testee,
        createPomWithConfiguration("<include><value>DELETED</value><value>UNKNOWN</value></include>"));
    testee.execute();
    verify(executionStrategy, times(1)).execute(any(File.class),
        any(ReportOptions.class), any(PluginServices.class),anyMap());
  }

  public void testDoesNotAnalysePomProjects() throws Exception {
    setupConnection();
    setFileWithStatus(ScmFileStatus.MODIFIED);
    when(project.getPackaging()).thenReturn("pom");
    testee.execute();
    verify(executionStrategy, never()).execute(any(File.class),
        any(ReportOptions.class), any(PluginServices.class),anyMap());
  }

  private void setupConnection() {
    when(scm.getConnection()).thenReturn("url");
    testee.setConnectionType("connection");
  }

  private void setupToReturnNoModifiedFiles() throws ScmException {
    when(manager.status(any(ScmRepository.class), any(ScmFileSet.class)))
        .thenReturn(new StatusScmResult("", Collections.<ScmFile> emptyList()));
  }

  private Map<String, String> anyMap() {
    return Matchers.<Map<String,String>>any();
  }
}