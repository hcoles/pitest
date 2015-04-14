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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.FileUtils;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Test;
import org.pitest.testapi.execute.Pitest;
import org.pitest.util.FileUtil;
import org.pitest.util.PitError;

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
  public void shouldWorkWithTestNGAndJMockit() throws Exception {
    final File testDir = prepare("/pit-testng-jmockit");
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
  
  /*
   * Verifies that configuring report generation to be skipped does actually prevent the site report from being generated.
   */
  @Test
  public void shouldSkipSiteReportGeneration() throws Exception {
    final File testDir = prepare("/pit-site-skip");
    final File siteParentDir = this.buildFile(testDir, "target", "site");
    
    this.verifier.executeGoals(Arrays.asList("clean", "test", "org.pitest:pitest-maven:mutationCoverage", "site"));
    
    assertThat(this.buildFile(siteParentDir, "pit-reports").exists()).isEqualTo(false);
    assertThat(this.buildFile(siteParentDir, "index.html").exists()).isEqualTo(true);
  }
  
  /*
   * Verifies that running PIT with timestampedReports set to false will correctly copy the HTML report to the site reports directory.
   */
  @Test
  public void shouldGenerateSiteReportWithNonTimestampedHtmlReport() throws Exception {
    final File testDir = prepare("/pit-site-non-timestamped");
    final File pitReportSiteDir = this.buildFile(testDir, "target", "site", "pit-reports");
    final String pitReportSiteIndexHtml;
    final String pitReportIndexHtml;
    
    this.verifier.executeGoals(Arrays.asList("clean", "test", "org.pitest:pitest-maven:mutationCoverage", "site"));
    
    assertThat(pitReportSiteDir.exists()).isEqualTo(true);

    pitReportSiteIndexHtml = FileUtil.readToString(new FileInputStream(this.buildFile(pitReportSiteDir, "index.html")));
    pitReportIndexHtml = FileUtil.readToString(new FileInputStream(this.buildFile(testDir, "target", "pit-reports", "index.html")));
    assertThat(pitReportSiteIndexHtml).isEqualTo(pitReportIndexHtml);
  }
  
  /*
   * Verifies that, when multiple timestamped PIT reports have been generated, only the latest report is copied to the site reports directory. 
   */
  @Test
  public void shouldCopyLatestTimestampedReport() throws Exception {
    final File testDir = prepare("/pit-site-multiple-timestamped");
    final File pitReportDir = this.buildFile(testDir, "target", "pit-reports");
    final File pitReportSiteDir = this.buildFile(testDir, "target", "site", "pit-reports");
    final String[] run1;
    
    this.verifier.setLogFileName("log1.txt");
    this.verifier.executeGoals(Arrays.asList("clean", "test", "org.pitest:pitest-maven:mutationCoverage", "site"));
    run1 = pitReportDir.list();
    assertThat(run1.length).isEqualTo(1);
    assertThat(this.buildFile(pitReportDir, run1[0], "first_marker.dat").createNewFile()).isEqualTo(true);
    
    //PIT timestamps reports to the minute which means it is possible to generate the same timestamped report twice, 
    //this code ensures that will not happen
    int loopCount = 0;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
    while(dateFormat.format(new Date()).equals(run1[0])){
    	if(loopCount > 65){
    		throw new PitError("integration test is stuck in an infinite loop");
    	}
    	
    	Thread.sleep(1000);
    	loopCount++;
    }
    
    this.verifier.setLogFileName("log2.txt");
    this.verifier.executeGoals(Arrays.asList("test", "org.pitest:pitest-maven:mutationCoverage", "site"));
    assertThat(pitReportDir.list().length).isEqualTo(2);
    
    assertThat(new File(pitReportSiteDir, "first_marker.dat").exists()).isEqualTo(false);
  }

  /*
   * Verifies that in the case where pit has generated reports with both timestampedReports=true and timestampedReports=false, 
   * the latest report run is copied and no timestamped report subdirectories are copied
   */
  @Test
  public void shouldCopyLatestTimestampedOrNonTimestampedReport() throws Exception {
	  final FilenameFilter timestampedDirFilter = new RegexFileFilter("^\\d+$");
	  final File testDir = prepare("/pit-site-combined");
	  final File pitReportDir = this.buildFile(testDir, "target", "pit-reports");
	  final File pitReportSiteDir = this.buildFile(testDir, "target", "site", "pit-reports");
	  final List originalCliOptions;
	  final File run1Dir;
	  final File run2Dir;
	  File run3Dir;
	  
	  originalCliOptions = new ArrayList(this.verifier.getCliOptions());
	  
	  //first run, create a timestamped report
	  this.verifier.setLogFileName("log1.txt");
	  this.verifier.getCliOptions().add("-DtimestampedReports=true");
	  this.verifier.executeGoals(Arrays.asList("clean", "test", "org.pitest:pitest-maven:mutationCoverage", "site"));
	  this.verifier.setCliOptions(new ArrayList(originalCliOptions));
	  run1Dir = pitReportDir.listFiles()[0];
	  new File(run1Dir, "first.dat").createNewFile();
	  
	  //second run, create a non-timestamped report
	  this.verifier.setLogFileName("log2.txt");
	  this.verifier.getCliOptions().add("-DtimestampedReports=false");
	  this.verifier.executeGoals(Arrays.asList("test", "org.pitest:pitest-maven:mutationCoverage", "site"));
	  this.verifier.setCliOptions(new ArrayList(originalCliOptions));
	  run2Dir = pitReportDir;
	  new File(run2Dir, "second.dat").createNewFile();
	  
	  //third run, create a timestamped report
	  run3Dir = null;
	  this.verifier.setLogFileName("log3.txt");
	  this.verifier.getCliOptions().add("-DtimestampedReports=true");
	  this.verifier.executeGoals(Arrays.asList("test", "org.pitest:pitest-maven:mutationCoverage"));
	  this.verifier.setCliOptions(new ArrayList(originalCliOptions));
	  for(File f : pitReportDir.listFiles(timestampedDirFilter)){
		  if(!f.equals(run1Dir)){
			  run3Dir = f;
			  break;
		  }
	  }
	  new File(run3Dir, "third.dat").createNewFile();
	  
	  //run the site lifecycle last so that the third.dat file has a chance to be created before the sire generation happens
	  this.verifier.setLogFileName("log4.txt");
	  this.verifier.executeGoal("site");
	  
	  //assert that the third run (a timestamped report) is the report in the site/pit-reports directory
	  assertThat(new File(pitReportSiteDir, "third.dat").exists()).isTrue();
	  
	  //assert that no timestamped report subdirectories were copied into the site/pit-reports directory
	  //comparing to an empty array is better than checking the array length because a failure in this assert 
	  //will list the files that were found instead of just the number of files that were found
	  assertThat(pitReportSiteDir.list(timestampedDirFilter)).isEqualTo(new String[0]);
  }

  @Test
  public void shouldReadExclusionsFromSurefireConfig() throws Exception {
    final File testDir = prepare("/pit-surefire-excludes");
    this.verifier.executeGoal("test");
    this.verifier.executeGoal("org.pitest:pitest-maven:mutationCoverage");
    final String actual = readResults(testDir);
    assertThat(actual).contains("<mutation detected='false' status='NO_COVERAGE'><sourceFile>NotCovered.java</sourceFile>");
  }
  
  @Test
  public void shouldWorkWithGWTMockito() throws Exception {
    final File testDir = prepare("/pit-183-gwtmockito");
    this.verifier.executeGoal("test");
    this.verifier.executeGoal("org.pitest:pitest-maven:mutationCoverage");
    final String actual = readResults(testDir);
    assertThat(actual).contains("<mutation detected='true' status='KILLED'><sourceFile>MyWidget.java</sourceFile>");
    assertThat(actual).contains("<mutation detected='false' status='SURVIVED'><sourceFile>MyWidget.java</sourceFile>");
    assertThat(actual).doesNotContain("status='RUN_ERROR'");
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
  
  private File buildFile(File base, String... pathParts) {
	  StringBuilder path = new StringBuilder(base.getAbsolutePath());
	  
	  for(String part : pathParts){
		  path.append(File.separator).append(part);
	  }
	  
	  return new File(path.toString());
  }

}
