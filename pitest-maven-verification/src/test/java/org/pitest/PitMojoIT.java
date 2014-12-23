/*
 * Copyright 2011 Henry Coles and Stefan Penndorf
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.pitest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.FileUtils;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Test;
import org.pitest.testapi.execute.Pitest;
import org.pitest.util.FileUtil;

/**
 * @author Stefan Penndorf <stefan.penndorf@gmail.com>
 */
public class PitMojoIT {

  private final String version = getVersion();

  private Verifier     verifier;

  @Test
  public void shouldSetUserDirToArtefactWorkingDirectory() throws Exception {
    prepare("/pit-33-setUserDir");
    this.verifier.executeGoal("test");
    this.verifier.executeGoal("org.pitest:pitest-maven:mutationCoverage");
  }

  @Test
  public void shouldProduceConsistantCoverageData() throws Exception {
    final File testDir = prepare("/pit-deterministic-coverage");
    this.verifier.executeGoal("test");
    this.verifier.executeGoal("org.pitest:pitest-maven:mutationCoverage");
    final String firstRun = readCoverage(testDir);
    this.verifier.executeGoal("org.pitest:pitest-maven:mutationCoverage");
    final String secondRun = readCoverage(testDir);
    assertEquals(firstRun, secondRun);
  }
  
  @Test
  public void shouldWorkWithTestNG() throws Exception {
    final File testDir = prepare("/pit-testng");
    this.verifier.executeGoal("test");
    this.verifier.executeGoal("org.pitest:pitest-maven:mutationCoverage");
    final String actual = readResults(testDir);
    assertThat(actual).contains("<mutation detected='true' status='KILLED'><sourceFile>Covered.java</sourceFile>");
    assertThat(actual).contains("<mutation detected='false' status='NO_COVERAGE'><sourceFile>Covered.java</sourceFile>");
    assertThat(actual).doesNotContain("status='RUN_ERROR'");
  }
  
  @Test
  public void shouldExcludeSpecifiedJUnitCategories() throws Exception {
    final File testDir = prepare("/pit-junit-categories");
    this.verifier.executeGoal("test");
    this.verifier.executeGoal("org.pitest:pitest-maven:mutationCoverage");
    final String actual = readResults(testDir);
    final String coverage = readCoverage(testDir);
    assertThat(coverage).doesNotContain("NotCovered");
    assertThat(coverage).contains("Covered");
    assertThat(actual).contains("<mutation detected='false' status='NO_COVERAGE'><sourceFile>NotCovered.java</sourceFile>");
    assertThat(actual).doesNotContain("<mutation detected='true' status='KILLED'><sourceFile>NotCovered.java</sourceFile>");
    assertThat(actual).contains("<mutation detected='true' status='KILLED'><sourceFile>Covered.java</sourceFile>");
  }

  @Test
  public void shouldWorkWithPowerMock() throws Exception {
    final File testDir = prepare("/pit-powermock");
    this.verifier.executeGoal("test");
    this.verifier.executeGoal("org.pitest:pitest-maven:mutationCoverage");
    final String actual = readResults(testDir);
    assertThat(actual).contains("<mutation detected='true' status='KILLED'><sourceFile>PowerMockAgentCallFoo.java</sourceFile>");
    assertThat(actual).contains("<mutation detected='true' status='KILLED'><sourceFile>PowerMockCallsOwnMethod.java</sourceFile>");
    assertThat(actual).contains("<mutation detected='true' status='KILLED'><sourceFile>PowerMockCallFoo.java</sourceFile>");
    assertThat(actual).doesNotContain("status='RUN_ERROR'");
  }

  @Test
  public void shouldCorrectlyTargetTestsWhenMultipleBlocksIncludeALine() throws Exception{
    final File testDir = prepare("/pit-158-coverage");
    this.verifier.executeGoal("test");
    this.verifier.executeGoal("org.pitest:pitest-maven:mutationCoverage");
    final String actual = readResults(testDir);
    assertThat(actual).contains("<mutation detected='true' status='KILLED'><sourceFile>MyRequest.java</sourceFile>");
  }
  
  private String readResults(File testDir) throws FileNotFoundException,
      IOException {
    File coverage = new File(testDir.getAbsoluteFile() + File.separator
        + "target" + File.separator + "pit-reports" + File.separator
        + "mutations.xml");
    return FileUtil.readToString(new FileInputStream(coverage));
  }

  private String readCoverage(final File testDir) throws IOException,
      FileNotFoundException {
    File coverage = new File(testDir.getAbsoluteFile() + File.separator
        + "target" + File.separator + "pit-reports" + File.separator
        + "linecoverage.xml");
    return FileUtil.readToString(new FileInputStream(coverage));
  }

  @SuppressWarnings("unchecked")
  private File prepare(final String testPath) throws IOException,
      VerificationException {
    final String tempDirPath = System.getProperty("maven.test.tmpdir",
        System.getProperty("java.io.tmpdir"));
    final File tempDir = new File(tempDirPath, getClass().getSimpleName());
    final File testDir = new File(tempDir, testPath);
    FileUtils.deleteDirectory(testDir);

    final String path = ResourceExtractor.extractResourcePath(getClass(),
        testPath, tempDir, true).getAbsolutePath();

    this.verifier = new Verifier(path);
    this.verifier.setAutoclean(false);
    this.verifier.setDebug(true);
    this.verifier.getCliOptions().add("-Dpit.version=" + this.version);

    return testDir;
  }

  private static String getVersion() {
    String path = "/version.prop";
    InputStream stream = Pitest.class.getResourceAsStream(path);
    Properties props = new Properties();
    try {
      props.load(stream);
      stream.close();
      return (String) props.get("version");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
