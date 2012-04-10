package org.pitest.project.impl;

import junit.framework.Assert;

import org.junit.Test;
import org.pitest.mutationtest.ReportOptions;
import org.pitest.mutationtest.instrument.PercentAndConstantTimeoutStrategy;
import org.pitest.project.ProjectConfigurationException;
import org.pitest.project.ProjectConfigurationParser;
import org.pitest.project.ProjectConfigurationParserException;
import org.pitest.project.ProjectConfigurationParserFactory;

/**
 * Suite of tests that check that the DefaultProjectFileParser parses the
 * project file according to the rules.
 * 
 * @author Aidan Morgan
 */
public class DefaultProjectConfigurationParserTests {

  @Test(expected = ProjectConfigurationParserException.class)
  public void shouldThrowExceptionIfFileDoesNotExist()
      throws ProjectConfigurationException, ProjectConfigurationParserException {
    final StubFileSystemDelegate delegate = new StubFileSystemDelegate();
    delegate.setFileExists(false);

    final ProjectConfigurationParser parser = ProjectConfigurationParserFactory
        .createParser();
    parser.loadProject("project");

  }

  @Test(expected = ProjectConfigurationParserException.class)
  public void shouldThrowExceptionIfFileIsADirectory()
      throws ProjectConfigurationException, ProjectConfigurationParserException {
    final StubFileSystemDelegate delegate = new StubFileSystemDelegate();
    delegate.setFile(false);

    final ProjectConfigurationParser parser = ProjectConfigurationParserFactory
        .createParser();
    parser.loadProject("project");

  }

  @Test(expected = ProjectConfigurationParserException.class)
  public void shouldThrowExceptionIfFileCannotBeRead()
      throws ProjectConfigurationException, ProjectConfigurationParserException {
    final StubFileSystemDelegate delegate = new StubFileSystemDelegate();
    delegate.setCanRead(false);

    final ProjectConfigurationParser parser = ProjectConfigurationParserFactory
        .createParser();
    parser.loadProject("project");

  }

  @Test(expected = ProjectConfigurationException.class)
  public void shouldThrowExceptionForInvalidRootElement()
      throws ProjectConfigurationParserException, ProjectConfigurationException {
    final String fileContents = "<pitest>\n"
        + "    <property name=\"reportDir\" value=\"./reports\"/>\n"
        + "</pitest>";

    final DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(
        new StubFileSystemDelegate(fileContents));
    parser.loadProject("project");

  }

  @Test
  public void shouldParseSuccessfullyForProjectDirectory()
      throws ProjectConfigurationParserException, ProjectConfigurationException {
    final String fileContents = "<project>\n"
        + "    <property name=\"reportDir\" value=\"./reports\"/>\n"
        + "</project>";

    final DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(
        new StubFileSystemDelegate(fileContents));
    final ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals("./reports", ro.getReportDir());
  }

  @Test(expected = ProjectConfigurationException.class)
  public void shouldThrowExceptionWhenNoProjectDirectorySpecified()
      throws ProjectConfigurationParserException, ProjectConfigurationException {
    final String fileContents = "<project>\n" + "</project>";

    final DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(
        new StubFileSystemDelegate(fileContents));
    parser.loadProject("project");

  }

  @Test
  public void shouldLoadVerbosePropertyFromFile()
      throws ProjectConfigurationException, ProjectConfigurationParserException {
    final String fileContents = "<project>\n"
        + "    <property name=\"verbose\" value=\"true\"/>\n"
        + "    <property name=\"reportDir\" value=\"./reports\"/>\n"
        + "</project>";

    final DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(
        new StubFileSystemDelegate(fileContents));
    final ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);
    Assert.assertTrue(ro.isVerbose());
  }

  @Test
  public void shouldUseDefaultVerboseValue()
      throws ProjectConfigurationException, ProjectConfigurationParserException {
    final String fileContents = "<project>\n"
        + "    <property name=\"reportDir\" value=\"./reports\"/>\n"
        + "</project>";

    final DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(
        new StubFileSystemDelegate(fileContents));
    final ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertFalse(ro.isVerbose());
  }

  @Test
  public void shouldLoadTimeoutFactorFromFile()
      throws ProjectConfigurationException, ProjectConfigurationParserException {
    final String fileContents = "<project>\n"
        + "    <property name=\"timeoutFactor\" value=\"1.75\"/>\n"
        + "    <property name=\"reportDir\" value=\"./reports\"/>\n"
        + "</project>";

    final DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(
        new StubFileSystemDelegate(fileContents));
    final ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(1.75, ro.getTimeoutFactor(), 0.001);
  }

  @Test
  public void shouldUseDefaultTimeoutFactor()
      throws ProjectConfigurationException, ProjectConfigurationParserException {
    final String fileContents = "<project>\n"
        + "    <property name=\"reportDir\" value=\"./reports\"/>\n"
        + "</project>";

    final DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(
        new StubFileSystemDelegate(fileContents));
    final ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(PercentAndConstantTimeoutStrategy.DEFAULT_FACTOR,
        ro.getTimeoutFactor(), 0.001);
  }

  @Test
  public void shouldLoadTimeoutConstantFromFile()
      throws ProjectConfigurationException, ProjectConfigurationParserException {
    final String fileContents = "<project>\n"
        + "    <property name=\"reportDir\" value=\"./reports\"/>\n"
        + "    <property name=\"timeoutConst\" value=\"1500\"/>\n"
        + "</project>";

    final DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(
        new StubFileSystemDelegate(fileContents));
    final ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(1500, ro.getTimeoutConstant());
  }

  @Test
  public void shouldUseDefaultTimeoutConstant()
      throws ProjectConfigurationException, ProjectConfigurationParserException {
    final String fileContents = "<project>\n"
        + "    <property name=\"reportDir\" value=\"./reports\"/>\n"
        + "</project>";

    final DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(
        new StubFileSystemDelegate(fileContents));
    final ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(PercentAndConstantTimeoutStrategy.DEFAULT_CONSTANT,
        ro.getTimeoutConstant());
  }

  @Test
  public void shouldAllowNoTargetTestsSpecified()
      throws ProjectConfigurationException, ProjectConfigurationParserException {
    final String fileContents = "<project>\n"
        + "    <property name=\"reportDir\" value=\"./reports\"/>\n"
        + "    <targetTests/>\n" + "</project>";

    final DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(
        new StubFileSystemDelegate(fileContents));
    final ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(0, ro.getTargetTests().size());
  }

  @Test
  public void shouldLoadTargetTestsFromFile()
      throws ProjectConfigurationException, ProjectConfigurationParserException {
    final String fileContents = "<project>\n"
        + "    <property name=\"reportDir\" value=\"./reports\"/>\n"
        + "    <targetTests>\n" + "        <filter name=\"org.pitest.*\"/>\n"
        + "    </targetTests>\n" + "</project>";

    final DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(
        new StubFileSystemDelegate(fileContents));
    final ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(1, ro.getTargetTests().size());
  }

  @Test
  public void shouldAllowNoTargetClassesSpecified()
      throws ProjectConfigurationException, ProjectConfigurationParserException {
    final String fileContents = "<project>\n"
        + "    <property name=\"reportDir\" value=\"./reports\"/>\n"
        + "    <targetClasses/>\n" + "</project>";

    final DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(
        new StubFileSystemDelegate(fileContents));
    final ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(0, ro.getTargetClasses().size());
  }

  @Test
  public void shouldLoadTargetClassesFromFile()
      throws ProjectConfigurationException, ProjectConfigurationParserException {
    final String fileContents = "<project>\n"
        + "    <property name=\"reportDir\" value=\"./reports\"/>\n"
        + "    <targetClasses>\n" + "        <filter name=\"org.pitest.*\"/>\n"
        + "    </targetClasses>\n" + "</project>";

    final DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(
        new StubFileSystemDelegate(fileContents));
    final ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(1, ro.getTargetClasses().size());
  }

  @Test
  public void shouldAllowNoSourceDirs() throws ProjectConfigurationException,
      ProjectConfigurationParserException {
    final String fileContents = "<project>\n"
        + "    <property name=\"reportDir\" value=\"./reports\"/>\n"
        + "    <sourceDirs/>\n" + "</project>";

    final DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(
        new StubFileSystemDelegate(fileContents));
    final ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(0, ro.getSourceDirs().size());
  }

  @Test
  public void shouldLoadSourceDirsFromFile()
      throws ProjectConfigurationException, ProjectConfigurationParserException {
    final String fileContents = "<project>\n"
        + "    <property name=\"reportDir\" value=\"./reports\"/>\n"
        + "    <sourceDirs>\n" + "        <dir name=\"./src\"/>\n"
        + "    </sourceDirs>\n" + "</project>";

    final StubFileSystemDelegate fileSystem = new StubFileSystemDelegate();
    fileSystem.setFileExists(true);

    final DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(
        new StubFileSystemDelegate(fileContents));
    final ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);
    Assert.assertEquals(1, ro.getSourceDirs().size());
  }

  @Test(expected = ProjectConfigurationException.class)
  public void shouldThrowExceptionIfSourceDirsDoesntExist()
      throws ProjectConfigurationParserException, ProjectConfigurationException {
    final String fileContents = "<project>\n"
        + "    <property name=\"reportDir\" value=\"./reports\"/>\n"
        + "    <sourceDirs>\n" + "        <dir name=\"./src\"/>\n"
        + "    </sourceDirs>\n" + "</project>";

    final StubFileSystemDelegate fileSystem = new StubFileSystemDelegate(
        fileContents);
    fileSystem.setFileExists("./src", false);

    final DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(
        fileSystem);
    parser.loadProject("project");

  }

  @Test
  public void shouldLoadNumberOfThreadsFromFile()
      throws ProjectConfigurationException, ProjectConfigurationParserException {
    final String fileContents = "<project>\n"
        + "    <property name=\"reportDir\" value=\"./reports\"/>\n"
        + "    <property name=\"threads\" value=\"4\"/>\n" + "</project>";

    final DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(
        new StubFileSystemDelegate(fileContents));
    final ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(4, ro.getNumberOfThreads());
  }

  @Test
  public void shouldUseDefaultThreadNumber()
      throws ProjectConfigurationException, ProjectConfigurationParserException {
    final String fileContents = "<project>\n"
        + "    <property name=\"reportDir\" value=\"./reports\"/>\n"
        + "</project>";

    final DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(
        new StubFileSystemDelegate(fileContents));
    final ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(1, ro.getNumberOfThreads());
  }

  @Test
  public void shouldLoadStaticMutatorsFromFile()
      throws ProjectConfigurationException, ProjectConfigurationParserException {
    final String fileContents = "<project>\n"
        + "    <property name=\"reportDir\" value=\"./reports\"/>\n"
        + "    <property name=\"mutateStaticInits\" value=\"true\"/>\n"
        + "</project>";

    final DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(
        new StubFileSystemDelegate(fileContents));
    final ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertTrue(ro.isMutateStaticInitializers());
  }

  @Test
  public void shouldUseDefaultStaticMutatorsValue()
      throws ProjectConfigurationException, ProjectConfigurationParserException {
    final String fileContents = "<project>\n"
        + "    <property name=\"reportDir\" value=\"./reports\"/>\n"
        + "</project>";

    final DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(
        new StubFileSystemDelegate(fileContents));
    final ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertFalse(ro.isMutateStaticInitializers());
  }

  @Test
  public void shouldLoadMaxMutationsFromFile()
      throws ProjectConfigurationException, ProjectConfigurationParserException {
    final String fileContents = "<project>\n"
        + "    <property name=\"reportDir\" value=\"./reports\"/>\n"
        + "    <property name=\"maxMutationsPerClass\" value=\"5\"/>\n"
        + "</project>";

    final DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(
        new StubFileSystemDelegate(fileContents));
    final ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(5, ro.getMaxMutationsPerClass());
  }

  @Test
  public void shouldUseDefaultMaxMutationsValue()
      throws ProjectConfigurationException, ProjectConfigurationParserException {
    final String fileContents = "<project>\n"
        + "    <property name=\"reportDir\" value=\"./reports\"/>\n"
        + "</project>";

    final DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(
        new StubFileSystemDelegate(fileContents));
    final ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(0, ro.getMaxMutationsPerClass());
  }

  @Test
  public void shouldAllowNoExcludedMethodsSpecified()
      throws ProjectConfigurationException, ProjectConfigurationParserException {
    final String fileContents = "<project>\n"
        + "    <property name=\"reportDir\" value=\"./reports\"/>\n"
        + "    <excludedMethods/>\n" + "</project>";

    final DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(
        new StubFileSystemDelegate(fileContents));
    final ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(0, ro.getExcludedMethods().size());
  }

  @Test
  public void shouldLoadExcludedMethodsFromFile()
      throws ProjectConfigurationException, ProjectConfigurationParserException {
    final String fileContents = "<project>\n"
        + "    <property name=\"reportDir\" value=\"./reports\"/>\n"
        + "    <excludedMethods>\n" + "        <filter name=\"toString\"/>\n"
        + "    </excludedMethods>\n" + "</project>";

    final DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(
        new StubFileSystemDelegate(fileContents));
    final ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(1, ro.getExcludedMethods().size());
  }

  @Test
  public void shouldAllowNoExcludedClassesSpecified()
      throws ProjectConfigurationException, ProjectConfigurationParserException {
    final String fileContents = "<project>\n"
        + "    <property name=\"reportDir\" value=\"./reports\"/>\n"
        + "    <excludedClasses/>\n" + "</project>";

    final DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(
        new StubFileSystemDelegate(fileContents));
    final ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(0, ro.getExcludedClasses().size());
  }

  @Test
  public void shouldLoadExcludedClassesFromFile()
      throws ProjectConfigurationException, ProjectConfigurationParserException {
    final String fileContents = "<project>\n"
        + "    <property name=\"reportDir\" value=\"./reports\"/>\n"
        + "    <excludedClasses>\n" + "        <filter name=\"Integer\"/>\n"
        + "    </excludedClasses>\n" + "</project>";

    final DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(
        new StubFileSystemDelegate(fileContents));
    final ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(1, ro.getExcludedClasses().size());
  }

  @Test
  public void shouldLoadDependencyAnalysisMaxDistanceFromFile()
      throws ProjectConfigurationException, ProjectConfigurationParserException {
    final String fileContents = "<project>\n"
        + "    <property name=\"reportDir\" value=\"./reports\"/>\n"
        + "    <property name=\"dependencyDistance\" value=\"5\"/>\n"
        + "</project>";

    final DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(
        new StubFileSystemDelegate(fileContents));
    final ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(5, ro.getDependencyAnalysisMaxDistance());
  }

  @Test
  public void shouldUseDefaultDependencyAnalysisMaxDistanceValue()
      throws ProjectConfigurationException, ProjectConfigurationParserException {
    final String fileContents = "<project>\n"
        + "    <property name=\"reportDir\" value=\"./reports\"/>\n"
        + "</project>";

    final DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(
        new StubFileSystemDelegate(fileContents));
    final ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(-1, ro.getDependencyAnalysisMaxDistance());
  }

  @Test
  public void shouldAllowNoClassPathElementsSpecified()
      throws ProjectConfigurationException, ProjectConfigurationParserException {
    final String fileContents = "<project>\n"
        + "    <property name=\"reportDir\" value=\"./reports\"/>\n"
        + "    <classPath/>\n" + "</project>";

    final DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(
        new StubFileSystemDelegate(fileContents));
    final ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(0, ro.getClassPathElements().size());
  }

  @Test
  public void shouldLoadClassPathElementsAsDirectoriesFromFile()
      throws ProjectConfigurationException, ProjectConfigurationParserException {
    final String fileContents = "<project>\n"
        + "    <property name=\"reportDir\" value=\"./reports\"/>\n"
        + "    <classPath>\n" + "        <dir name=\"./build/obj\"/>\n"
        + "    </classPath>\n" + "</project>";

    final DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(
        new StubFileSystemDelegate(fileContents));
    final ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(1, ro.getClassPathElements().size());
  }

  @Test
  public void shouldLoadClassPathElementsAsJarsFromFile()
      throws ProjectConfigurationException, ProjectConfigurationParserException {
    final String fileContents = "<project>\n"
        + "    <property name=\"reportDir\" value=\"./reports\"/>\n"
        + "    <classPath>\n" + "        <jar name=\"./build/obj\"/>\n"
        + "    </classPath>\n" + "</project>";

    final DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(
        new StubFileSystemDelegate(fileContents));
    final ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(1, ro.getClassPathElements().size());
  }



}
