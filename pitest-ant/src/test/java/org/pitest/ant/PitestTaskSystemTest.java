package org.pitest.ant;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;

import java.io.File;
import java.io.FileInputStream;

import org.apache.tools.ant.BuildFileTest;
import org.pitest.util.FileUtil;

public class PitestTaskSystemTest extends BuildFileTest {

  @Override
  public void setUp() {
    configureProject("testBuilds/testBuild.xml");
    executeTarget("clean");
  }

  public void testDetectsMixOfKilledSurvivingAndUncoveredMutants()
      throws Exception {
    executeTarget("mutationCoverage");
    FileInputStream fis = new FileInputStream(findOutput());
    String actual = FileUtil.readToString(fis);
    assertXpathExists("//mutation[@status='KILLED']", actual);
    assertXpathExists("//mutation[@status='NO_COVERAGE']", actual);
    assertXpathExists("//mutation[@status='SURVIVED']", actual);

  }

  private File findOutput() {
    String root = "target/tempOut/".replace('/', File.separatorChar);
    File dir = new File(root);
    String subDir = dir.list()[0];
    return new File(root + subDir + File.separatorChar + "mutations.xml");
  }

}
