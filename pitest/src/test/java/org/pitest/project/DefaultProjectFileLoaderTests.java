package org.pitest.project;

import junit.framework.Assert;
import org.junit.Test;
import org.pitest.mutationtest.ReportOptions;
import org.pitest.mutationtest.instrument.PercentAndConstantTimeoutStrategy;

import java.io.ByteArrayInputStream;

/**
 * Suite of tests that check that the {@see DefaultProjectFileParser} parses the project file according to the rules.
 *
 * @author Aidan Morgan
 */
public class DefaultProjectFileLoaderTests {

  @Test
  public void loadProjectFile_invalidRootElement() throws ProjectFileParserException, ProjectConfigurationException {
    String fileContents = "<pitest>\n" +
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
  public void loadProjectFile_reportDirSpecified() throws ProjectFileParserException, ProjectConfigurationException {
    String fileContents = "<project>\n" +
        "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
        "</project>";

    ProjectFileParser parser = ProjectFileParserFactory.createParser();
    ReportOptions ro = parser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));

    Assert.assertNotNull(ro);

    Assert.assertEquals("./reports", ro.getReportDir());
  }

  @Test
  public void loadProjectFile_noReportDirSpecified() throws ProjectFileParserException {
    String fileContents = "<project>\n" +
        "</project>";

    try {
      ProjectFileParser parser = ProjectFileParserFactory.createParser();
      ReportOptions ro = parser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));

      Assert.fail("Should not be able to parse a project file that does not have a reportDir property set.");
    } catch (ProjectConfigurationException e) {
      // expected
    }
  }

  @Test
  public void loadProjectFile_verbosePropertyIsTrue() throws ProjectConfigurationException, ProjectFileParserException {
    String fileContents = "<project>\n" +
        "    <property name=\"verbose\" value=\"true\"/>\n" +
        "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
        "</project>";

    ProjectFileParser parser = ProjectFileParserFactory.createParser();
    ReportOptions ro = parser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));

    Assert.assertNotNull(ro);

    Assert.assertTrue(ro.isVerbose());
  }

  @Test
  public void loadProjectFile_defaultVerboseValueIsFalse() throws ProjectConfigurationException, ProjectFileParserException {
    String fileContents = "<project>\n" +
        "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
        "</project>";

    ProjectFileParser parser = ProjectFileParserFactory.createParser();
    ReportOptions ro = parser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));

    Assert.assertNotNull(ro);

    Assert.assertFalse(ro.isVerbose());
  }

  @Test
  public void loadProjectFile_timeoutFactorSpecified() throws ProjectConfigurationException, ProjectFileParserException {
    String fileContents = "<project>\n" +
        "    <property name=\"timeoutFactor\" value=\"1.75\"/>\n" +
        "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
        "</project>";

    ProjectFileParser parser = ProjectFileParserFactory.createParser();
    ReportOptions ro = parser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));

    Assert.assertNotNull(ro);

    Assert.assertEquals(1.75, ro.getTimeoutFactor(), 0.001);
  }

  @Test
  public void loadProjectFile_defaultTimeoutFactor() throws ProjectConfigurationException, ProjectFileParserException {
    String fileContents = "<project>\n" +
        "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
        "</project>";

    ProjectFileParser parser = ProjectFileParserFactory.createParser();
    ReportOptions ro = parser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));

    Assert.assertNotNull(ro);

    Assert.assertEquals(PercentAndConstantTimeoutStrategy.DEFAULT_FACTOR, ro.getTimeoutFactor(), 0.001);
  }

  @Test
  public void loadProjectFile_timeoutConstantSpecified() throws ProjectConfigurationException, ProjectFileParserException {
    String fileContents = "<project>\n" +
        "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
        "    <property name=\"timeoutConst\" value=\"1500\"/>\n" +
        "</project>";

    ProjectFileParser parser = ProjectFileParserFactory.createParser();
    ReportOptions ro = parser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));

    Assert.assertNotNull(ro);

    Assert.assertEquals(1500, ro.getTimeoutConstant());
  }

  @Test
  public void loadProjectFile_timeoutConstantDefaultValue() throws ProjectConfigurationException, ProjectFileParserException {
    String fileContents = "<project>\n" +
        "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
        "</project>";

    ProjectFileParser parser = ProjectFileParserFactory.createParser();
    ReportOptions ro = parser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));

    Assert.assertNotNull(ro);

    Assert.assertEquals(PercentAndConstantTimeoutStrategy.DEFAULT_CONSTANT, ro.getTimeoutConstant());
  }

  @Test
  public void loadProjectFile_noTargetTestsSpecified() throws ProjectConfigurationException, ProjectFileParserException {
    String fileContents = "<project>\n" +
        "\t<property name=\"reportDir\" value=\"./reports\"/>\n" +
        "\t<targetTests/>\n" +
        "</project>";

    ProjectFileParser parser = ProjectFileParserFactory.createParser();
    ReportOptions ro = parser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));

    Assert.assertNotNull(ro);

    Assert.assertEquals(0, ro.getTargetTests().size());
  }

  @Test
  public void loadProjectFile_oneTargetTestsSpecified() throws ProjectConfigurationException, ProjectFileParserException {
    String fileContents ="<project>\n" +
        "\t<property name=\"reportDir\" value=\"./reports\"/>\n" +
        "\t<targetTests>\n" +
        "\t\t<filter name=\"org.pitest.*\"/>\n" +
        "\t</targetTests>\n" +
        "</project>";

    ProjectFileParser parser = ProjectFileParserFactory.createParser();
    ReportOptions ro = parser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));

    Assert.assertNotNull(ro);

    Assert.assertEquals(1, ro.getTargetTests().size());
  }  @Test

  public void loadProjectFile_noTargetClassesSpecified() throws ProjectConfigurationException, ProjectFileParserException {
    String fileContents = "<project>\n" +
        "\t<property name=\"reportDir\" value=\"./reports\"/>\n" +
        "\t<targetClasses/>\n" +
        "</project>";

    ProjectFileParser parser = ProjectFileParserFactory.createParser();
    ReportOptions ro = parser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));

    Assert.assertNotNull(ro);

    Assert.assertEquals(0, ro.getTargetClasses().size());
  }

  @Test
  public void loadProjectFile_oneTargetClassSpecified() throws ProjectConfigurationException, ProjectFileParserException {
    String fileContents ="<project>\n" +
        "\t<property name=\"reportDir\" value=\"./reports\"/>\n" +
        "\t<targetClasses>\n" +
        "\t\t<filter name=\"org.pitest.*\"/>\n" +
        "\t</targetClasses>\n" +
        "</project>";

    ProjectFileParser parser = ProjectFileParserFactory.createParser();
    ReportOptions ro = parser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));

    Assert.assertNotNull(ro);

    Assert.assertEquals(1, ro.getTargetClasses().size());
  }
}
