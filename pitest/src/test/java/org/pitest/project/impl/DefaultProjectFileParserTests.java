package org.pitest.project.impl;

import junit.framework.Assert;
import org.junit.Test;
import org.pitest.mutationtest.ReportOptions;
import org.pitest.mutationtest.instrument.PercentAndConstantTimeoutStrategy;
import org.pitest.project.ProjectConfigurationException;
import org.pitest.project.ProjectFileParser;
import org.pitest.project.ProjectFileParserException;
import org.pitest.project.ProjectFileParserFactory;
import org.powermock.api.easymock.PowerMock;

import java.io.ByteArrayInputStream;

/**
 * Suite of tests that check that the {@see DefaultProjectFileParser} parses the project file according to the rules.
 *
 * @author Aidan Morgan
 */
public class DefaultProjectFileParserTests {

  @Test
  public void shouldThrowExceptionForInvalidRootElement() throws ProjectFileParserException, ProjectConfigurationException {
    String fileContents =
        "<pitest>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "</pitest>";

    try {
      ProjectFileParser parser = ProjectFileParserFactory.createParser();
      ReportOptions ro = parser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));

      Assert.fail("Should not be able to parse a project file which has the wrong document element");
    } catch (ProjectConfigurationException e) {
      // expected
    }
  }

  @Test
  public void shouldParseSuccessfullyForProjectDirectory() throws ProjectFileParserException, ProjectConfigurationException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "</project>";

    ProjectFileParser parser = ProjectFileParserFactory.createParser();
    ReportOptions ro = parser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));

    Assert.assertNotNull(ro);

    Assert.assertEquals("./reports", ro.getReportDir());
  }

  @Test
  public void shouldThrowExceptionWhenNoProjectDirectorySpecified() throws ProjectFileParserException {
    String fileContents =
        "<project>\n" +
            "</project>";

    try {
      ProjectFileParser parser = ProjectFileParserFactory.createParser();
      parser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));

      Assert.fail("Should not be able to parse a project file that does not have a reportDir property set.");
    } catch (ProjectConfigurationException e) {
      // expected
    }
  }

  @Test
  public void shouldLoadVerbosePropertyFromFile() throws ProjectConfigurationException, ProjectFileParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"verbose\" value=\"true\"/>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "</project>";

    ProjectFileParser parser = ProjectFileParserFactory.createParser();
    ReportOptions ro = parser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));

    Assert.assertNotNull(ro);
    Assert.assertTrue(ro.isVerbose());
  }

  @Test
  public void shouldUseDefaultVerboseValue() throws ProjectConfigurationException, ProjectFileParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "</project>";

    ProjectFileParser parser = ProjectFileParserFactory.createParser();
    ReportOptions ro = parser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));

    Assert.assertNotNull(ro);

    Assert.assertFalse(ro.isVerbose());
  }

  @Test
  public void shouldLoadTimeoutFactorFromFile() throws ProjectConfigurationException, ProjectFileParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"timeoutFactor\" value=\"1.75\"/>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "</project>";

    ProjectFileParser parser = ProjectFileParserFactory.createParser();
    ReportOptions ro = parser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));

    Assert.assertNotNull(ro);

    Assert.assertEquals(1.75, ro.getTimeoutFactor(), 0.001);
  }

  @Test
  public void shouldUseDefaultTimeoutFactor() throws ProjectConfigurationException, ProjectFileParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "</project>";

    ProjectFileParser parser = ProjectFileParserFactory.createParser();
    ReportOptions ro = parser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));

    Assert.assertNotNull(ro);

    Assert.assertEquals(PercentAndConstantTimeoutStrategy.DEFAULT_FACTOR, ro.getTimeoutFactor(), 0.001);
  }

  @Test
  public void shouldLoadTimeoutConstantFromFile() throws ProjectConfigurationException, ProjectFileParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "    <property name=\"timeoutConst\" value=\"1500\"/>\n" +
            "</project>";

    ProjectFileParser parser = ProjectFileParserFactory.createParser();
    ReportOptions ro = parser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));

    Assert.assertNotNull(ro);

    Assert.assertEquals(1500, ro.getTimeoutConstant());
  }

  @Test
  public void shouldUseDefaultTimeoutConstant() throws ProjectConfigurationException, ProjectFileParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "</project>";

    ProjectFileParser parser = ProjectFileParserFactory.createParser();
    ReportOptions ro = parser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));

    Assert.assertNotNull(ro);

    Assert.assertEquals(PercentAndConstantTimeoutStrategy.DEFAULT_CONSTANT, ro.getTimeoutConstant());
  }

  @Test
  public void shouldAllowNoTargetTestsSpecified() throws ProjectConfigurationException, ProjectFileParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "    <targetTests/>\n" +
            "</project>";

    ProjectFileParser parser = ProjectFileParserFactory.createParser();
    ReportOptions ro = parser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));

    Assert.assertNotNull(ro);

    Assert.assertEquals(0, ro.getTargetTests().size());
  }

  @Test
  public void shouldLoadTargetTestsFromFile() throws ProjectConfigurationException, ProjectFileParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "    <targetTests>\n" +
            "        <filter name=\"org.pitest.*\"/>\n" +
            "    </targetTests>\n" +
            "</project>";

    ProjectFileParser parser = ProjectFileParserFactory.createParser();
    ReportOptions ro = parser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));

    Assert.assertNotNull(ro);

    Assert.assertEquals(1, ro.getTargetTests().size());
  }

  @Test

  public void shouldAllowNoTargetClassesSpecified() throws ProjectConfigurationException, ProjectFileParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "    <targetClasses/>\n" +
            "</project>";

    ProjectFileParser parser = ProjectFileParserFactory.createParser();
    ReportOptions ro = parser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));

    Assert.assertNotNull(ro);

    Assert.assertEquals(0, ro.getTargetClasses().size());
  }

  @Test
  public void shouldLoadTargetClassesFromFile() throws ProjectConfigurationException, ProjectFileParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "    <targetClasses>\n" +
            "        <filter name=\"org.pitest.*\"/>\n" +
            "    </targetClasses>\n" +
            "</project>";

    ProjectFileParser parser = ProjectFileParserFactory.createParser();
    ReportOptions ro = parser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));

    Assert.assertNotNull(ro);

    Assert.assertEquals(1, ro.getTargetClasses().size());
  }

  @Test
  public void shouldAllowNoSourceDirs() throws ProjectConfigurationException, ProjectFileParserException {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "    <sourceDirs/>\n" +
            "</project>";

    ProjectFileParser parser = ProjectFileParserFactory.createParser();

    ReportOptions ro = parser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));

    Assert.assertNotNull(ro);

    Assert.assertEquals(0, ro.getSourceDirs().size());
  }

  @Test
  // I hate that this method has to throw Exception, blame the PowerMock guys!
  public void shouldLoadSourceDirsFromFile() throws Exception {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "    <sourceDirs>\n" +
            "        <dir name=\"./src\"/>\n" +
            "    </sourceDirs>\n" +
            "</project>";

    StubFileSystemDelegate delegate = new StubFileSystemDelegate();
    delegate.addResult("./src", true);

    DefaultProjectFileParser fileParser = new DefaultProjectFileParser(delegate);

    ReportOptions ro = fileParser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));

    Assert.assertNotNull(ro);
    Assert.assertEquals(1, ro.getSourceDirs().size());
  }

  @Test
  // I hate that this method has to throw Exception, blame the PowerMock guys!
  public void shouldThrowExceptionIfSourceDirsDoesntExist() throws Exception {
    String fileContents =
        "<project>\n" +
            "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
            "    <sourceDirs>\n" +
            "        <dir name=\"./src\"/>\n" +
            "    </sourceDirs>\n" +
            "</project>";


    try {
      StubFileSystemDelegate delegate = new StubFileSystemDelegate();
      delegate.addResult("./src", false);

      DefaultProjectFileParser fileParser = new DefaultProjectFileParser(delegate);

      ReportOptions ro = fileParser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));
      Assert.fail("Should not be able to load a Project File if a source directory does not exist.");
    } catch (ProjectConfigurationException e) {
      // expected exception
    }
  }
}
