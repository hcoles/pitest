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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;
import org.pitest.testapi.execute.Pitest;
import org.pitest.util.FileUtil;
import org.pitest.util.PitError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

/**
 * @author Stefan Penndorf <stefan.penndorf@gmail.com>
 */
public class PitMojoIT {
  private static Logger LOGGER = LoggerFactory.getLogger(PitMojoIT.class);
  private static String VERSION = getVersion();

  @Rule
  public TestName testName = new TestName();

  @Rule
  public TemporaryFolder testFolder = new TemporaryFolder();

  private Verifier     verifier;
  private long startTime;

  @Before
  public void beforeEachTest(){
    LOGGER.info("running test '{}'", testName.getMethodName() );
    startTime = System.currentTimeMillis();
  }

  @After
  public void afterEachTest(){
    LOGGER.info("duration of test '{}' {}ms", testName.getMethodName(),System.currentTimeMillis()-startTime );
  }

  @Test
  public void shouldSetUserDirToArtefactWorkingDirectory() throws Exception {
    prepare("/pit-33-setUserDir");

    verifier.executeGoal("org.pitest:pitest-maven:mutationCoverage");
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
    assertThat(actual).contains("<mutation detected='true' status='KILLED'><sourceFile>Covered.java</sourceFile>");
    assertThat(actual).contains("<mutation detected='false' status='NO_COVERAGE'><sourceFile>Covered.java</sourceFile>");
    assertThat(actual).doesNotContain("status='RUN_ERROR'");
  }
  @Test
  public void shouldWorkWithTestNGAndJMockit() throws Exception {
    File testDir = prepare("/pit-testng-jmockit");
    verifier.executeGoal("test");
    verifier.executeGoal("org.pitest:pitest-maven:mutationCoverage");

    String actual = readResults(testDir);
    assertThat(actual).contains("<mutation detected='true' status='KILLED'><sourceFile>Covered.java</sourceFile>");
    assertThat(actual).contains("<mutation detected='false' status='NO_COVERAGE'><sourceFile>Covered.java</sourceFile>");
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
    assertThat(actual).contains("<mutation detected='false' status='NO_COVERAGE'><sourceFile>NotCovered.java</sourceFile>");
    assertThat(actual).doesNotContain("<mutation detected='true' status='KILLED'><sourceFile>NotCovered.java</sourceFile>");
    assertThat(actual).contains("<mutation detected='true' status='KILLED'><sourceFile>Covered.java</sourceFile>");
  }

  @Test
  public void shouldWorkWithPowerMock() throws Exception {
    File testDir = prepare("/pit-powermock");
    verifier.executeGoal("test");
    verifier.executeGoal("org.pitest:pitest-maven:mutationCoverage");

    String actual = readResults(testDir);
    assertThat(actual).contains("<mutation detected='true' status='KILLED'><sourceFile>PowerMockAgentCallFoo.java</sourceFile>");
    assertThat(actual).contains("<mutation detected='true' status='KILLED'><sourceFile>PowerMockCallsOwnMethod.java</sourceFile>");
    assertThat(actual).contains("<mutation detected='true' status='KILLED'><sourceFile>PowerMockCallFoo.java</sourceFile>");
    assertThat(actual).doesNotContain("status='RUN_ERROR'");
  }

  @Test
  public void shouldCorrectlyTargetTestsWhenMultipleBlocksIncludeALine() throws Exception{
    File testDir = prepare("/pit-158-coverage");
    verifier.executeGoal("test");
    verifier.executeGoal("org.pitest:pitest-maven:mutationCoverage");

    String actual = readResults(testDir);
    assertThat(actual).contains("<mutation detected='true' status='KILLED'><sourceFile>MyRequest.java</sourceFile>");
  }

  /*
   * Verifies that configuring report generation to be skipped does actually prevent the site report from being generated.
   */
  @Test
  public void shouldSkipSiteReportGeneration() throws Exception {
    File testDir = prepare("/pit-site-skip");
    File siteParentDir = buildFile(testDir, "target", "site");

    verifier.executeGoals(Arrays.asList("clean", "test", "org.pitest:pitest-maven:mutationCoverage", "site"));

    assertThat(buildFile(siteParentDir, "pit-reports").exists()).isEqualTo(false);
    assertThat(buildFile(siteParentDir, "index.html").exists()).isEqualTo(true);
  }

  /*
   * Verifies that running PIT with timestampedReports set to false will correctly copy the HTML report to the site reports directory.
   */
  @Test
  public void shouldGenerateSiteReportWithNonTimestampedHtmlReport() throws Exception {
    File testDir = prepare("/pit-site-non-timestamped");
    File pitReportSiteDir = buildFile(testDir, "target", "site", "pit-reports");
    File siteProjectReportsIndex = buildFile(testDir, "target", "site", "project-reports.html");

    verifier.executeGoals(Arrays.asList("clean", "test", "org.pitest:pitest-maven:mutationCoverage", "site"));

    assertThat(pitReportSiteDir.exists()).isEqualTo(true);

    String pitReportSiteIndexHtml = FileUtil.readToString(new FileInputStream(buildFile(pitReportSiteDir, "index.html")));
    String pitReportIndexHtml = FileUtil.readToString(new FileInputStream(buildFile(testDir, "target", "pit-reports", "index.html")));
    assertThat(pitReportSiteIndexHtml).isEqualTo(pitReportIndexHtml);

    //assert that the expected report name/description was written to the site project report's index.html file
	String projectReportsHtml = FileUtil.readToString(new FileInputStream(siteProjectReportsIndex));
	assertTrue("did not find expected anchor tag to pit site report", projectReportsHtml.contains("<a href=\"pit-reports/index.html\" title=\"PIT Test Report\">PIT Test Report</a>"));
  }

  /*
   * Verifies that, when multiple timestamped PIT reports have been generated, only the latest report is copied to the site reports directory.
   */
  @Test
  public void shouldCopyLatestTimestampedReport() throws Exception {
    File testDir = prepare("/pit-site-multiple-timestamped");
    File pitReportDir = buildFile(testDir, "target", "pit-reports");
    File pitReportSiteDir = buildFile(testDir, "target", "site", "pit-reports");

    verifier.setLogFileName("log1.txt");
    verifier.executeGoals(Arrays.asList("clean", "test", "org.pitest:pitest-maven:mutationCoverage", "site"));
    String[] run1 = pitReportDir.list();
    assertThat(run1.length).isEqualTo(1);
    assertTrue("first marker file not created", buildFile(pitReportDir, run1[0], "first_marker.dat").createNewFile());

    waitUntilNextMinute(run1[0]);

    verifier.setLogFileName("log2-pit.txt");
    verifier.executeGoals(Arrays.asList("test", "org.pitest:pitest-maven:mutationCoverage"));

    boolean secondMarkerCreated = false;
    //create a marker file to ensure the latest pit report is copied over
    String[] run2 = pitReportDir.list();
    assertThat(run2.length).isEqualTo(2);
    for (String s : run2) {
      if (!s.equals(run1[0])) {
    		assertThat(buildFile(pitReportDir, s, "second_marker.dat").createNewFile()).isEqualTo(true);
    		secondMarkerCreated = true;
    		break;
    	}
    }
    assertTrue("second marker file not created", secondMarkerCreated);

    verifier.setLogFileName("log2-site.txt");
    verifier.executeGoal("site");

    assertThat(new File(pitReportSiteDir, "first_marker.dat").exists()).isEqualTo(false);
    assertThat(new File(pitReportSiteDir, "second_marker.dat").exists()).isEqualTo(true);
  }

  /*
   * Verifies that in the case where pit has generated reports with both timestampedReports=true and timestampedReports=false,
   * the latest report run is copied and no timestamped report subdirectories are copied
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  public void shouldCopyLatestTimestampedOrNonTimestampedReport() throws Exception {
	  FilenameFilter timestampedDirFilter = new RegexFileFilter("^\\d+$");
	  File testDir = prepare("/pit-site-combined");
	  File pitReportDir = buildFile(testDir, "target", "pit-reports");
	  File pitReportSiteDir = buildFile(testDir, "target", "site", "pit-reports");
	  boolean thirdMarkerCreated = false;

	  List originalCliOptions = new ArrayList(verifier.getCliOptions());

	  //first run -- create a timestamped report
	  verifier.setLogFileName("log1.txt");
	  verifier.getCliOptions().add("-DtimestampedReports=true");
	  verifier.executeGoals(Arrays.asList("clean", "test", "org.pitest:pitest-maven:mutationCoverage", "site"));
	  verifier.setCliOptions(new ArrayList(originalCliOptions));

	  //first run -- create the "first.dat" marker file in the new timestamped reports directory
	  File run1Dir = pitReportDir.listFiles()[0];
	  new File(run1Dir, "first.dat").createNewFile();

	  //second run -- create a non-timestamped report
	  verifier.setLogFileName("log2.txt");
	  verifier.getCliOptions().add("-DtimestampedReports=false");
	  verifier.executeGoals(Arrays.asList("test", "org.pitest:pitest-maven:mutationCoverage", "site"));
	  verifier.setCliOptions(new ArrayList(originalCliOptions));

	  //second run -- create the "second.dat" marker file in the target/pit-reports directory (since the second run is a non-timestamped report)
	  new File(pitReportDir, "second.dat").createNewFile();

    //third run -- create a timestamped report
	  waitUntilNextMinute(run1Dir.getName());
	  verifier.setLogFileName("log3-pit.txt");
	  verifier.getCliOptions().add("-DtimestampedReports=true");
	  verifier.executeGoals(Arrays.asList("test", "org.pitest:pitest-maven:mutationCoverage"));
	  verifier.setCliOptions(new ArrayList(originalCliOptions));

	  //third run -- create the "third.dat" marker file in the new timestamped reports directory
	  for (File f : pitReportDir.listFiles(timestampedDirFilter)) {
		  if (!f.equals(run1Dir)) {
			  new File(f, "third.dat").createNewFile();
			  thirdMarkerCreated = true;
			  break;
		  }
	  }
	  assertTrue("third marker file not created", thirdMarkerCreated);

	  //run the site lifecycle last so that the third.dat file has a chance to be created before the site generation happens
	  verifier.setLogFileName("log3-site.txt");
	  verifier.executeGoal("site");

	  //assert that the third run (a timestamped report) is the report in the site/pit-reports directory
	  assertTrue("did not find expected marker file third.dat in site directory", new File(pitReportSiteDir, "third.dat").exists());

	  //assert that no timestamped report subdirectories were copied into the site/pit-reports directory
	  //comparing to an empty array is better than checking the array length because a failure in this assert
	  //will list the files that were found instead of just the number of files that were found
	  assertThat(pitReportSiteDir.list(timestampedDirFilter)).isEqualTo(new String[0]);
  }

  /*
   * Verifies that the build fails when running the report goal without first running the mutationCoverage goal
   */
  @Test
  public void shouldFailIfNoReportAvailable() throws Exception {
	  prepare("/pit-site-reportonly");

	  try{
		  verifier.executeGoal("site");
          fail("should fail");
	  }catch(VerificationException e){
		  assertThat(e.getMessage()).containsSequence("[ERROR] Failed to execute goal org.apache.maven.plugins:maven-site-plugin:", ":site (default-site) on project pit-site-reportonly: Execution default-site of goal org.apache.maven.plugins:maven-site-plugin:", ":site failed: could not find reports directory", "pit-site-reportonly/target/pit-reports");
	  }
  }

  /*
   * verifies that overriding defaults has the expected results
   */
  @Test
  public void shouldCorrectlyHandleOverrides() throws Exception {
    File testDir = prepare("/pit-site-custom-config");
    File siteProjectReportsIndex = buildFile(testDir, "target", "site", "project-reports.html");
    File expectedSiteReportDir = buildFile(testDir, "target", "site", "foobar");
    File defaultSiteReportDir = buildFile(testDir, "target", "site", "pit-reports");

    verifier.executeGoals(Arrays.asList("test", "org.pitest:pitest-maven:mutationCoverage", "site"));

    String projectReportsHtml = FileUtils.readFileToString(siteProjectReportsIndex);
    assertTrue("did not find expected anchor tag to pit site report", projectReportsHtml.contains("<a href=\"foobar/index.html\" title=\"my-test-pit-report-name\">my-test-pit-report-name</a>"));
    assertTrue("expected site report directory [" + expectedSiteReportDir + "] does not exist but should exist", expectedSiteReportDir.exists());
    assertFalse("expected default site report directory [" + defaultSiteReportDir + "] exists but should not exist since the report location parameter was overridden", defaultSiteReportDir.exists());
  }

  @Test
  public void shouldReadExclusionsFromSurefireConfig() throws Exception {
    File testDir = prepare("/pit-surefire-excludes");
    verifier.executeGoal("test");
    verifier.executeGoal("org.pitest:pitest-maven:mutationCoverage");

    String actual = readResults(testDir);
    assertThat(actual).contains("<mutation detected='false' status='NO_COVERAGE'><sourceFile>NotCovered.java</sourceFile>");
  }

  @Test
  public void shouldWorkWithGWTMockito() throws Exception {
    File testDir = prepare("/pit-183-gwtmockito");
    verifier.executeGoal("test");
    verifier.executeGoal("org.pitest:pitest-maven:mutationCoverage");

    String actual = readResults(testDir);
    assertThat(actual).contains("<mutation detected='true' status='KILLED'><sourceFile>MyWidget.java</sourceFile>");
    assertThat(actual).contains("<mutation detected='false' status='SURVIVED'><sourceFile>MyWidget.java</sourceFile>");
    assertThat(actual).doesNotContain("status='RUN_ERROR'");
  }

  private static String readResults(File testDir) throws IOException {
    File mutationReport = new File(testDir.getAbsoluteFile() + File.separator
            + "target" + File.separator
            + "pit-reports" + File.separator
            + "mutations.xml");
    return FileUtils.readFileToString(mutationReport);
  }

  private static String readCoverage(File testDir) throws IOException{
    File coverage = new File(testDir.getAbsoluteFile() + File.separator
            + "target" + File.separator
            + "pit-reports" + File.separator
            + "linecoverage.xml");
    return FileUtils.readFileToString(coverage);
  }

  @SuppressWarnings("unchecked")
  private File prepare(String testPath) throws IOException, VerificationException {
    String path = ResourceExtractor.extractResourcePath(getClass(), testPath, testFolder.getRoot(), true).getAbsolutePath();

    verifier = new Verifier(path);
    verifier.setAutoclean(false);
    verifier.setDebug(true);
    verifier.getCliOptions().add("-Dpit.version=" + VERSION);

    // just for debugging, this hangs in case of error
    // because the exception tries to read the log file and fails (of course)
//    verifier.setLogFileName("../../../../../dev/stdout");

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

  private File buildFile(File base, String... pathParts) {
	  StringBuilder path = new StringBuilder(base.getAbsolutePath());

	  for (String part : pathParts) {
		  path.append(File.separator).append(part);
	  }

	  return new File(path.toString());
  }

  /**
   * PIT timestamps reports to the minute which means it is possible to generate the same timestamped report twice.
   * This function ensures that will not happen by waiting until the minute after the specified date time.
   *
   * @param startDateTime date time {@link String} in the format "yyyyMMddHHmm", this function will wait until a minute after this date time
   * @throws Exception if this function waits more than 65 seconds or if there is an {@link InterruptedException} during the Thread.sleep
   */
  private void waitUntilNextMinute(String startDateTime) throws Exception {
	//
    //this code ensures that will not happen
    int loopCount = 0;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
    while(dateFormat.format(new Date()).equals(startDateTime)){
    	if(loopCount > 65){
    		throw new PitError("integration test is stuck in an infinite loop");
    	}

    	Thread.sleep(1000);
    	loopCount++;
    }
  }
}
