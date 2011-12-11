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
 * Suite of tests that check that the {@see DefaultProjectFileParser} parses the project file according to the rules.
 *
 * @author Aidan Morgan
 */
public class DefaultProjectConfigurationParserTests {

  @Test
  public void shouldThrowExceptionIfFileDoesNotExist() throws ProjectConfigurationException {
    StubFileSystemDelegate delegate = new StubFileSystemDelegate();
    delegate.setFileExists(false);

    try {
      ProjectConfigurationParser parser = ProjectConfigurationParserFactory.createParser();
      parser.loadProject("project");

      Assert.fail("Should not be able to load a project from a file that does not exist.");
    } catch (ProjectConfigurationParserException e) {
      // expected
    }
  }

  @Test
  public void shouldThrowExceptionIfFileIsADirectory() throws ProjectConfigurationException {
    StubFileSystemDelegate delegate = new StubFileSystemDelegate();
    delegate.setFile(false);

    try {
      ProjectConfigurationParser parser = ProjectConfigurationParserFactory.createParser();
      parser.loadProject("project");

      Assert.fail("Should not be able to load a project from a directory.");
    } catch (ProjectConfigurationParserException e) {
      // expected
    }
  }

  @Test
  public void shouldThrowExceptionIfFileCannotBeRead() throws ProjectConfigurationException {
    StubFileSystemDelegate delegate = new StubFileSystemDelegate();
    delegate.setCanRead(false);

    try {
      ProjectConfigurationParser parser = ProjectConfigurationParserFactory.createParser();
      parser.loadProject("project");

      Assert.fail("Should not be able to load a project from a file that cannot be read.");
    } catch (ProjectConfigurationParserException e) {
      // expected
    }
  }

  @Test
  public void shouldThrowExceptionForInvalidRootElement() throws ProjectConfigurationParserException, ProjectConfigurationException {
    String fileContents =
        "<pitest>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "</pitest>";

    try {
      DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
      parser.loadProject("project");

      Assert.fail("Should not be able to parse a project file which has the wrong document element");
    } catch (ProjectConfigurationException e) {
      // expected
    }
  }

  @Test
  public void shouldParseSuccessfullyForProjectDirectory() throws ProjectConfigurationParserException, ProjectConfigurationException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "</project>";

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals("./reports", ro.getReportDir());
  }

  @Test
  public void shouldThrowExceptionWhenNoProjectDirectorySpecified() throws ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "</project>";

    try {
      DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
      ReportOptions ro = parser.loadProject("project");

      Assert.fail("Should not be able to parse a project file that does not have a reportDir property set.");
    } catch (ProjectConfigurationException e) {
      // expected
    }
  }

  @Test
  public void shouldLoadVerbosePropertyFromFile() throws ProjectConfigurationException, ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"verbose\" value=\"true\"/>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "</project>";

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);
    Assert.assertTrue(ro.isVerbose());
  }

  @Test
  public void shouldUseDefaultVerboseValue() throws ProjectConfigurationException, ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "</project>";

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertFalse(ro.isVerbose());
  }

  @Test
  public void shouldLoadTimeoutFactorFromFile() throws ProjectConfigurationException, ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"timeoutFactor\" value=\"1.75\"/>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "</project>";

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(1.75, ro.getTimeoutFactor(), 0.001);
  }

  @Test
  public void shouldUseDefaultTimeoutFactor() throws ProjectConfigurationException, ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "</project>";

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(PercentAndConstantTimeoutStrategy.DEFAULT_FACTOR, ro.getTimeoutFactor(), 0.001);
  }

  @Test
  public void shouldLoadTimeoutConstantFromFile() throws ProjectConfigurationException, ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "    <property name=\"timeoutConst\" value=\"1500\"/>\n" +
            "</project>";

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(1500, ro.getTimeoutConstant());
  }

  @Test
  public void shouldUseDefaultTimeoutConstant() throws ProjectConfigurationException, ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "</project>";

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(PercentAndConstantTimeoutStrategy.DEFAULT_CONSTANT, ro.getTimeoutConstant());
  }

  @Test
  public void shouldAllowNoTargetTestsSpecified() throws ProjectConfigurationException, ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "    <targetTests/>\n" +
            "</project>";

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(0, ro.getTargetTests().size());
  }

  @Test
  public void shouldLoadTargetTestsFromFile() throws ProjectConfigurationException, ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "    <targetTests>\n" +
            "        <filter name=\"org.pitest.*\"/>\n" +
            "    </targetTests>\n" +
            "</project>";

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(1, ro.getTargetTests().size());
  }

  @Test
  public void shouldAllowNoTargetClassesSpecified() throws ProjectConfigurationException, ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "    <targetClasses/>\n" +
            "</project>";

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(0, ro.getTargetClasses().size());
  }

  @Test
  public void shouldLoadTargetClassesFromFile() throws ProjectConfigurationException, ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "    <targetClasses>\n" +
            "        <filter name=\"org.pitest.*\"/>\n" +
            "    </targetClasses>\n" +
            "</project>";

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(1, ro.getTargetClasses().size());
  }

  @Test
  public void shouldAllowNoSourceDirs() throws ProjectConfigurationException, ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "    <sourceDirs/>\n" +
            "</project>";

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(0, ro.getSourceDirs().size());
  }

  @Test
  public void shouldLoadSourceDirsFromFile() throws ProjectConfigurationException, ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "    <sourceDirs>\n" +
            "        <dir name=\"./src\"/>\n" +
            "    </sourceDirs>\n" +
            "</project>";

    StubFileSystemDelegate fileSystem = new StubFileSystemDelegate();
    fileSystem.setFileExists(true);

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);
    Assert.assertEquals(1, ro.getSourceDirs().size());
  }

  @Test
  public void shouldThrowExceptionIfSourceDirsDoesntExist() throws ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "    <sourceDirs>\n" +
            "        <dir name=\"./src\"/>\n" +
            "    </sourceDirs>\n" +
            "</project>";


    try {
      StubFileSystemDelegate fileSystem = new StubFileSystemDelegate(fileContents);
      fileSystem.setFileExists("./src", false);

      DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(fileSystem);
      parser.loadProject("project");

      Assert.fail("Should not be able to load a Project File if a source directory does not exist.");
    } catch (ProjectConfigurationException e) {
      // expected exception
    }
  }

  @Test
  public void shouldLoadNumberOfThreadsFromFile() throws ProjectConfigurationException, ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "    <property name=\"threads\" value=\"4\"/>\n" +
            "</project>";

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(4, ro.getNumberOfThreads());
  }

  @Test
  public void shouldUseDefaultThreadNumber() throws ProjectConfigurationException, ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "</project>";

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(1, ro.getNumberOfThreads());
  }

  @Test
  public void shouldLoadStaticMutatorsFromFile() throws ProjectConfigurationException, ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "    <property name=\"mutateStaticInits\" value=\"true\"/>\n" +
            "</project>";

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertTrue(ro.isMutateStaticInitializers());
  }

  @Test
  public void shouldUseDefaultStaticMutatorsValue() throws ProjectConfigurationException, ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "</project>";

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertFalse(ro.isMutateStaticInitializers());
  }

  @Test
  public void shouldLoadMaxMutationsFromFile() throws ProjectConfigurationException, ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "    <property name=\"maxMutationsPerClass\" value=\"5\"/>\n" +
            "</project>";

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(5, ro.getMaxMutationsPerClass());
  }

  @Test
  public void shouldUseDefaultMaxMutationsValue() throws ProjectConfigurationException, ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "</project>";

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(0, ro.getMaxMutationsPerClass());
  }

  @Test
  public void shouldLoadIncludeJarFilesFromFile() throws ProjectConfigurationException, ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "    <property name=\"includeJarFiles\" value=\"true\"/>\n" +
            "</project>";

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertTrue(ro.isIncludeJarFiles());
  }

  @Test
  public void shouldUseDefaultIncludeJarFilesValue() throws ProjectConfigurationException, ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "</project>";

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertFalse(ro.isIncludeJarFiles());
  }

  @Test
  public void shouldAllowNoExcludedMethodsSpecified() throws ProjectConfigurationException, ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "    <excludedMethods/>\n" +
            "</project>";

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(0, ro.getExcludedMethods().size());
  }

  @Test
  public void shouldLoadExcludedMethodsFromFile() throws ProjectConfigurationException, ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "    <excludedMethods>\n" +
            "        <filter name=\"toString\"/>\n" +
            "    </excludedMethods>\n" +
            "</project>";

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(1, ro.getExcludedMethods().size());
  }

  @Test
  public void shouldAllowNoExcludedClassesSpecified() throws ProjectConfigurationException, ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "    <excludedClasses/>\n" +
            "</project>";

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(0, ro.getExcludedClasses().size());
  }

  @Test
  public void shouldLoadExcludedClassesFromFile() throws ProjectConfigurationException, ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "    <excludedClasses>\n" +
            "        <filter name=\"Integer\"/>\n" +
            "    </excludedClasses>\n" +
            "</project>";

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(1, ro.getExcludedClasses().size());
  }

  @Test
  public void shouldLoadDependencyAnalysisMaxDistanceFromFile() throws ProjectConfigurationException, ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "    <property name=\"dependencyDistance\" value=\"5\"/>\n" +
            "</project>";

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(5, ro.getDependencyAnalysisMaxDistance());
  }

  @Test
  public void shouldUseDefaultDependencyAnalysisMaxDistanceValue() throws ProjectConfigurationException, ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "</project>";

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(-1, ro.getDependencyAnalysisMaxDistance());
  }

  @Test
  public void shouldAllowNoClassPathElementsSpecified() throws ProjectConfigurationException, ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "    <classPath/>\n" +
            "</project>";

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(0, ro.getClassPathElements().size());
  }

  @Test
  public void shouldLoadClassPathElementsAsDirectoriesFromFile() throws ProjectConfigurationException, ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "    <classPath>\n" +
            "        <dir name=\"./build/obj\"/>\n" +
            "    </classPath>\n" +
            "</project>";

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(1, ro.getClassPathElements().size());
  }

  @Test
  public void shouldLoadClassPathElementsAsJarsFromFile() throws ProjectConfigurationException, ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "    <classPath>\n" +
            "        <jar name=\"./build/obj\"/>\n" +
            "    </classPath>\n" +
            "</project>";

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(1, ro.getClassPathElements().size());
  }

  @Test
  public void shouldAllowNoClassesInScopeSpecified() throws ProjectConfigurationException, ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "    <inScopeClasses/>\n" +
            "</project>";

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(0, ro.getClassesInScope().size());
  }

  @Test
  public void shouldLoadClassesInScopeFromFile() throws ProjectConfigurationException, ProjectConfigurationParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "    <inScopeClasses>\n" +
            "        <filter name=\"org.pitest.*\"/>\n" +
            "    </inScopeClasses>\n" +
            "</project>";

    DefaultProjectConfigurationParser parser = new DefaultProjectConfigurationParser(new StubFileSystemDelegate(fileContents));
    ReportOptions ro = parser.loadProject("project");

    Assert.assertNotNull(ro);

    Assert.assertEquals(1, ro.getClassesInScope().size());
  }


}
