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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;
import org.pitest.support.DirectoriesOnlyWalker;
import org.pitest.testapi.execute.Pitest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stefan Penndorf <stefan.penndorf@gmail.com>
 */
public class PitMojoIT {
  private static Logger  LOGGER     = LoggerFactory.getLogger(PitMojoIT.class);
  private static String  VERSION    = getVersion();

  @Rule
  public TestName        testName   = new TestName();

  @Rule
  public TemporaryFolder testFolder = new TemporaryFolder();

  private Verifier       verifier;
  private long           startTime;

  @Before
  public void beforeEachTest() {
    LOGGER.info("running test '{}'", testName.getMethodName());
    startTime = System.currentTimeMillis();
  }

  @After
  public void afterEachTest() {
    LOGGER.info("duration of test '{}' {}ms", testName.getMethodName(),
        System.currentTimeMillis() - startTime);
  }

  @Test
  public void shouldSetUserDirToArtefactWorkingDirectory() throws Exception {
    prepare("/pit-33-setUserDir");

    verifier.executeGoal("org.pitest:pitest-maven:mutationCoverage");
  }
  
  @Test(timeout=60000)
  public void shouldNotHangWhenLargeAmountsOfConsoleOutput() throws Exception {
    File testDir = prepare("/pit-process-hang");
    verifier.executeGoal("test");
    verifier.executeGoal("org.pitest:pitest-maven:mutationCoverage"); 
    // checkout output looks sane, but main point is that test completed
    assertThat(readResults(testDir))
    .contains(
        "<sourceFile>SomeCode.java</sourceFile>");
  }

  @Test
  public void shouldProduceConsistantCoverageData() throws Exception {
    File testDir = prepare("/pit-deterministic-coverage");
    verifier.executeGoal("test");
    verifier.executeGoal("org.pitest:pitest-maven:mutationCoverage");

    String firstRun = readCoverage(testDir);
    verifier.executeGoal("org.pitest:pitest-maven:mutationCoverage");

    String secondRun = readCoverage(testDir);
    assertEquals(firstRun, secondRun);
  }

  @Test
  public void shouldWorkWithTestNG() throws Exception {
    File testDir = prepare("/pit-testng");
    verifier.executeGoal("test");
    verifier.executeGoal("org.pitest:pitest-maven:mutationCoverage");

    String actual = readResults(testDir);
    assertThat(actual)
        .contains(
            "<mutation detected='true' status='KILLED'><sourceFile>Covered.java</sourceFile>");
    assertThat(actual)
        .contains(
            "<mutation detected='false' status='NO_COVERAGE'><sourceFile>Covered.java</sourceFile>");
    assertThat(actual).doesNotContain("status='RUN_ERROR'");
  }

  @Test
  public void shouldWorkWithTestNGAndJMockit() throws Exception {
    File testDir = prepare("/pit-testng-jmockit");
    verifier.executeGoal("test");
    verifier.executeGoal("org.pitest:pitest-maven:mutationCoverage");

    String actual = readResults(testDir);
    assertThat(actual)
        .contains(
            "<mutation detected='true' status='KILLED'><sourceFile>Covered.java</sourceFile>");
    assertThat(actual)
        .contains(
            "<mutation detected='false' status='NO_COVERAGE'><sourceFile>Covered.java</sourceFile>");
    assertThat(actual).doesNotContain("status='RUN_ERROR'");
  }

  @Test
  public void shouldExcludeSpecifiedJUnitCategories() throws Exception {
    File testDir = prepare("/pit-junit-categories");
    verifier.executeGoal("test");
    verifier.executeGoal("org.pitest:pitest-maven:mutationCoverage");

    String actual = readResults(testDir);
    String coverage = readCoverage(testDir);
    assertThat(coverage).doesNotContain("NotCovered");
    assertThat(coverage).contains("Covered");
    assertThat(actual)
        .contains(
            "<mutation detected='false' status='NO_COVERAGE'><sourceFile>NotCovered.java</sourceFile>");
    assertThat(actual)
        .doesNotContain(
            "<mutation detected='true' status='KILLED'><sourceFile>NotCovered.java</sourceFile>");
    assertThat(actual)
        .contains(
            "<mutation detected='true' status='KILLED'><sourceFile>Covered.java</sourceFile>");
  }

  @Test
  //@Ignore("test is flakey, possibly due to real non deterministic issue with powermock")
  public void shouldWorkWithPowerMock() throws Exception {
    assumeFalse(System.getProperty("java.version").equals("9"));
    File testDir = prepare("/pit-powermock");
    verifier.addCliOption("-DtimeoutConstant=10000");
    verifier.executeGoal("test");
    verifier.executeGoal("org.pitest:pitest-maven:mutationCoverage");

    String actual = readResults(testDir);
    assertThat(actual)
        .contains(
            "<mutation detected='true' status='KILLED'><sourceFile>PowerMockAgentCallFoo.java</sourceFile>");
    assertThat(actual)
        .contains(
            "<mutation detected='true' status='KILLED'><sourceFile>PowerMockCallsOwnMethod.java</sourceFile>");
    assertThat(actual)
        .contains(
            "<mutation detected='true' status='KILLED'><sourceFile>PowerMockCallFoo.java</sourceFile>");
    assertThat(actual).doesNotContain("status='RUN_ERROR'");
    assertThat(actual).doesNotContain("<mutation detected='false' status='NO_COVERAGE'><sourceFile>PowerMockCallsOwnMethod.java</sourceFile><mutatedClass>com.example.PowerMockCallsOwnMethod</mutatedClass><mutatedMethod>branchedCode</mutatedMethod>");
  }

  @Test
  public void shouldCorrectlyTargetTestsWhenMultipleBlocksIncludeALine()
      throws Exception {
    File testDir = prepare("/pit-158-coverage");
    verifier.executeGoal("test");
    verifier.executeGoal("org.pitest:pitest-maven:mutationCoverage");

    String actual = readResults(testDir);
    assertThat(actual)
        .contains(
            "<mutation detected='true' status='KILLED'><sourceFile>MyRequest.java</sourceFile>");
  }

  /*
   * Verifies that configuring report generation to be skipped does actually
   * prevent the site report from being generated.
   */
  @Test
  public void shouldSkipSiteReportGeneration() throws Exception {
    File testDir = prepareSiteTest("/pit-site-skip");
    File siteParentDir = buildFilePath(testDir, "target", "site");

    verifier.executeGoal("site");

    assertThat(siteParentDir).exists();
    assertThat(buildFilePath(siteParentDir, "pit-reports")).doesNotExist();
    assertThat(buildFilePath(siteParentDir, "index.html")).exists();
    assertThat(buildFilePath(siteParentDir, "project-reports.html"))
        .doesNotExist();
  }

  /*
   * Verifies that running PIT with timestampedReports set to false will
   * correctly copy the HTML report to the site reports directory.
   */
  @Test
  public void shouldGenerateSiteReportWithNonTimestampedHtmlReport()
      throws Exception {
    File testDir = prepareSiteTest("/pit-site-non-timestamped");

    verifier.executeGoal("site");
    verifyPitReportTest(testDir);
  }

  /*
   * Verifies that running PIT with timestampedReports set to true will copy the
   * contents of the latest timestamped report
   */
  @Test
  public void shouldGenerateSiteReportWithSingleTimestampedHtmlReport()
      throws Exception {
    File testDir = prepareSiteTest("/pit-site-timestamped", "201505212116");

    verifier.executeGoal("site");
    verifyPitReportTest(testDir);
  }

  /*
   * Verifies that, when multiple timestamped PIT reports have been generated,
   * only the latest report is copied to the site reports directory. This test
   * sets the earlier directory (201503292032) as the last modified. This tests
   * to make sure the last modified date is used instead of just using the
   * folder name.
   */
  @Test
  public void shouldCopyLatestTimestampedReportWhenMultipleTimestampedReportsExist()
      throws Exception {
    File testDir = prepareSiteTest("/pit-site-multiple-timestamped",
        "201503292032");

    verifier.executeGoal("site");
    verifyPitReportTest(testDir);
  }

  /*
   * Verifies that in the case where pit has generated reports with both
   * timestampedReports=true and timestampedReports=false, the latest report run
   * is copied and no timestamped report subdirectories are copied
   */
  @Test
  public void shouldCopyLatestTimestampedOrNonTimestampedReportWhenBothExist()
      throws Exception {
    File testDir = prepareSiteTest("/pit-site-combined", "");

    verifier.executeGoal("site");
    verifyPitReportTest(testDir);
  }

  /*
   * Verifies that the build fails when running the report goal without first
   * running the mutationCoverage goal
   */
  @Test
  public void shouldFailIfNoPITReportAvailable() throws Exception {
    prepare("/pit-site-reportonly");

    try {
      verifier.executeGoal("site");
      fail("should fail");
    } catch (VerificationException e) {
      assertThat(e.getMessage())
          .containsSequence(
              "[ERROR] Failed to execute goal org.apache.maven.plugins:maven-site-plugin:",
              ":site (default-site) on project pit-site-reportonly: Execution default-site of goal org.apache.maven.plugins:maven-site-plugin:",
              ":site failed: could not find reports directory",
              "pit-site-reportonly/target/pit-reports");
    }
  }

  /*
   * verifies that overriding defaults has the expected results
   */
  @Test
  public void shouldCorrectlyHandleOverrides() throws Exception {
    File testDir = prepareSiteTest("/pit-site-custom-config");
    File targetDir = buildFilePath(testDir, "target");
    File expectedSiteReportDir = buildFilePath(testDir, "target", "site",
        "foobar");

    FileUtils.moveDirectory(buildFilePath(targetDir, "pit-reports"),
        buildFilePath(targetDir, "new-report-location"));

    verifier.executeGoal("site");

    String projectReportsHtmlContents = FileUtils
        .readFileToString(buildFilePath(testDir, "target", "site",
            "project-reports.html"));
    assertTrue(
        "did not find expected anchor tag to pit site report",
        projectReportsHtmlContents
            .contains("<a href=\"foobar/index.html\" title=\"my-test-pit-report-name\">my-test-pit-report-name</a>"));
    assertTrue("expected site report directory [" + expectedSiteReportDir
        + "] does not exist but should exist", expectedSiteReportDir.exists());

    assertFalse(
        "expected default site report directory exists but should not exist since the report location parameter was overridden",
        buildFilePath(testDir, "target", "site", "pit-reports").exists());
  }

  @Test
  public void shouldReadExclusionsFromSurefireConfig() throws Exception {
    File testDir = prepare("/pit-surefire-excludes");
    verifier.addCliOption("-DskipTests");
    verifier.executeGoal("test");
    verifier.executeGoal("org.pitest:pitest-maven:mutationCoverage");

    String actual = readResults(testDir);
    assertThat(actual)
        .contains(
            "<mutation detected='false' status='NO_COVERAGE'><sourceFile>NotCovered.java</sourceFile>");
  }

  @Test
  public void shouldWorkWithGWTMockito() throws Exception {
    assumeFalse(System.getProperty("java.version").equals("9"));
    File testDir = prepare("/pit-183-gwtmockito");
    verifier.executeGoal("test");
    verifier.executeGoal("org.pitest:pitest-maven:mutationCoverage");

    String actual = readResults(testDir);
    assertThat(actual)
        .contains(
            "<mutation detected='true' status='KILLED'><sourceFile>MyWidget.java</sourceFile>");
    assertThat(actual)
        .contains(
            "<mutation detected='false' status='SURVIVED'><sourceFile>MyWidget.java</sourceFile>");
    assertThat(actual).doesNotContain("status='RUN_ERROR'");
  }

  @Test
  public void shouldWorkWithYatspec() throws Exception {
    File testDir = prepare("/pit-263-yatspec");
    verifier.executeGoal("test");
    verifier.executeGoal("org.pitest:pitest-maven:mutationCoverage");

    String actual = readResults(testDir);
    assertThat(actual)
            .contains(
                    "<mutation detected='true' status='KILLED'><sourceFile>SomeClass.java</sourceFile>");
    assertThat(actual).doesNotContain("status='NO_COVERAGE'");
    assertThat(actual).doesNotContain("status='RUN_ERROR'");
  }

  private static String readResults(File testDir) throws IOException {
    File mutationReport = new File(testDir.getAbsoluteFile() + File.separator
        + "target" + File.separator + "pit-reports" + File.separator
        + "mutations.xml");
    return FileUtils.readFileToString(mutationReport);
  }

  private static String readCoverage(File testDir) throws IOException {
    File coverage = new File(testDir.getAbsoluteFile() + File.separator
        + "target" + File.separator + "pit-reports" + File.separator
        + "linecoverage.xml");
    return FileUtils.readFileToString(coverage);
  }

  private File prepare(String testPath) throws IOException,
      VerificationException {
    String path = ResourceExtractor.extractResourcePath(getClass(), testPath,
        testFolder.getRoot(), true).getAbsolutePath();

    verifier = new Verifier(path);
    verifier.setAutoclean(false);
    verifier.setDebug(true);
    verifier.getCliOptions().add("-Dpit.version=" + VERSION);
    verifier.getCliOptions().add(
        "-Dthreads=" + (Runtime.getRuntime().availableProcessors()));

    return new File(testFolder.getRoot().getAbsolutePath() + testPath);
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

  /**
   * Creates a new {@link File} object building off an existing {@link File}
   * object and appending subfolders.
   * 
   * For example, if this function is called with these arguments:
   * <code>buildFile(new File("/foo/bar"), "subdir1", "subdir2", "file1.txt");</code>
   * The returned {@link File} object would represent the path:
   * /foo/bar/subdir1/subdir/file1.txt
   * 
   * @param base
   *          {@link File} representing the starting location
   * @param pathParts
   *          {@link String} varags containing the subfolders to append to the
   *          base, this argument should contain at least one value and none of
   *          its values should be blank or null.
   * 
   * @return {@link File}
   */
  private File buildFilePath(File base, String... pathParts) {
    StringBuilder path = new StringBuilder(base.getAbsolutePath());

    for (String part : pathParts) {
      path.append(File.separator).append(part);
    }

    return new File(path.toString());
  }

  /**
   * Sets up a test of the reporting mojo simulating multiple runs of mvn
   * install (as in the case where timestampedReports is set to true). First
   * calls {@link #prepareSiteTest(String)} then walks all directories starting
   * at target/pit-reports setting their last modified time to 0 (epoch time).
   * Finally, the directory specified in the {@code latestDir} parameter has its
   * last modified time set to {@link System#currentTimeMillis()}.
   * 
   * @param testPath
   *          {@link String} see {@link #prepareSiteTest(String)}
   * @param latestDir
   *          {@link String} containing the subdirectory of target/pit-reports
   *          that should be set as the latest, pass an empty string to indicate
   *          the target/pit-reports directory should be the latest
   * 
   * @return {@link File} representing the temporary folder that was set up for
   *         this execution of the test
   * @throws Exception
   */
  private File prepareSiteTest(String testPath, String latestDir)
      throws Exception {
    File testDir = prepareSiteTest(testPath);
    // location where the target directory would be if a mvn clean install was executed
    File testTargetDir = this.buildFilePath(testDir, "target", "pit-reports"); 
    DirectoriesOnlyWalker walker = new DirectoriesOnlyWalker();

    for (File f : walker.locateDirectories(testTargetDir)) {
      f.setLastModified(0L);
    }

    assertThat(
        buildFilePath(testTargetDir, latestDir).setLastModified(
            System.currentTimeMillis())).isTrue();

    return testDir;
  }

  /**
   * Sets up a test of the reporting mojo by simulating what a mvn clean install
   * would do. After this function is executed, the maven site can be generated
   * by executing the site goal.
   * 
   * The provided {@code testPath} must have this folder structure set up
   * underneath it: src/test/resources/pit-reports The contents of this
   * directory will be moved to the target directory simulating a mvn clean
   * install.
   * 
   * @param testPath
   *          {@link String} location of the test to set up, this path is
   *          relative to <code>${basedir}/src/test/resources</code>
   * 
   * @return {@link File} representing the temporary folder that was set up for
   *         this execution of the test
   * @throws Exception
   */
  private File prepareSiteTest(String testPath) throws Exception {
    File tempTestExecutionDir = prepare(testPath);
    File targetDir = this.buildFilePath(tempTestExecutionDir, "target",
        "pit-reports");

    FileUtils.copyDirectory(
        buildFilePath(tempTestExecutionDir, "src", "test", "resources",
            "pit-reports"), targetDir);

    return tempTestExecutionDir;
  }

  private void verifyPitReportTest(File testDir) throws Exception {
    File pitReportSiteDir = buildFilePath(testDir, "target", "site",
        "pit-reports");

    assertThat(pitReportSiteDir).exists();
    assertThat(this.buildFilePath(pitReportSiteDir, "marker_expected.txt"))
        .exists();

    String projectReportsHtmlContents = FileUtils
        .readFileToString(buildFilePath(testDir, "target", "site",
            "project-reports.html"));
    assertTrue(
        "did not find expected anchor tag to pit site report",
        projectReportsHtmlContents
            .contains("<a href=\"pit-reports/index.html\" title=\"PIT Test Report\">PIT Test Report</a>"));
  }

}
