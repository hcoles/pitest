package org.pitest.project;

import junit.framework.Assert;
import org.junit.Test;
import org.pitest.mutationtest.ReportOptions;
import org.pitest.mutationtest.instrument.PercentAndConstantTimeoutStrategy;

import java.io.ByteArrayInputStream;

/**
 * @author Aidan Morgan
 */
public class DefaultProjectFileLoaderTests {

  @Test
  public void loadProjectFile_verbosePropertyIsTrue() {
    String fileContents = "<project>\n" +
        "    <property name=\"verbose\" value=\"true\"/>\n" +
        "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
        "</project>";

    try {
      ProjectFileParser parser = ProjectFileParserFactory.createParser();
      ReportOptions ro = parser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));

      Assert.assertNotNull(ro);

      Assert.assertTrue(ro.isVerbose());
    } catch (ProjectFileParserException e) {
      Assert.fail("ProjectFileParserException thrown: " + e.getMessage());
    } catch (ProjectConfigurationException e) {
      Assert.fail("ProjectConfigurationException thrown: " + e.getMessage());
    }
  }

  @Test
  public void loadProjectFile_defaultVerboseValueIsFalse() {
    String fileContents = "<project>\n" +
        "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
        "</project>";

    try {
      ProjectFileParser parser = ProjectFileParserFactory.createParser();
      ReportOptions ro = parser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));

      Assert.assertNotNull(ro);

      Assert.assertFalse(ro.isVerbose());
    } catch (ProjectFileParserException e) {
      Assert.fail("ProjectFileParserException thrown: " + e.getMessage());
    } catch (ProjectConfigurationException e) {
      Assert.fail("ProjectConfigurationException thrown: " + e.getMessage());
    }
  }

  @Test
  public void loadProjectFile_timeoutFactorSpecified() {
    String fileContents = "<project>\n" +
        "    <property name=\"timeoutFactor\" value=\"1.75\"/>\n" +
        "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
        "</project>";

    try {
      ProjectFileParser parser = ProjectFileParserFactory.createParser();
      ReportOptions ro = parser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));

      Assert.assertNotNull(ro);

      Assert.assertEquals(1.75, ro.getTimeoutFactor(), 0.001);
    } catch (ProjectFileParserException e) {
      Assert.fail("ProjectFileParserException thrown: " + e.getMessage());
    } catch (ProjectConfigurationException e) {
      Assert.fail("ProjectConfigurationException thrown: " + e.getMessage());
    }
  }

  @Test
  public void loadProjectFile_defaultTimeoutFactor() {
    String fileContents = "<project>\n" +
        "    <property name=\"reportDir\" value=\"./reports\"/>\n" +
        "</project>";

    try {
      ProjectFileParser parser = ProjectFileParserFactory.createParser();
      ReportOptions ro = parser.loadProjectFile(new ByteArrayInputStream(fileContents.getBytes()));

      Assert.assertNotNull(ro);

      Assert.assertEquals(PercentAndConstantTimeoutStrategy.DEFAULT_FACTOR, ro.getTimeoutFactor(), 0.001);
    } catch (ProjectFileParserException e) {
      Assert.fail("ProjectFileParserException thrown: " + e.getMessage());
    } catch (ProjectConfigurationException e) {
      Assert.fail("ProjectConfigurationException thrown: " + e.getMessage());
    }
  }
}
