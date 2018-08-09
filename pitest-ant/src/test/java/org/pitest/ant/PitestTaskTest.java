/*
 * Copyright 2012 Nicolas Rusconi
 *
 * Licensed under the Apache License, Version 2.0 ("the "License"");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.pitest.ant;

import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Commandline.Argument;
import org.apache.tools.ant.types.Path;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pitest.mutationtest.commandline.MutationCoverageReport;

@RunWith(MockitoJUnitRunner.class)
public class PitestTaskTest {

  private PitestTask       pitestTask;
  @Mock
  private Java             java;
  @Mock
  private Argument         arg;
  @Mock
  private Project          project;

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Before
  public void setUp() {
    when(this.java.createArg()).thenReturn(this.arg);

    this.pitestTask = new PitestTask();
    this.pitestTask.setClasspath("bin/");
    this.pitestTask.setPitClasspath("foo/");
    this.pitestTask.setTargetClasses("com.*");
    this.pitestTask.setReportDir("report/");
    this.pitestTask.setSourceDir("src/");
    this.pitestTask.setProject(this.project);
  }

  @Test
  public void shouldPassAvoidCallsOptionToJavaTask() {
    this.pitestTask.setAvoidCallsTo("avoidCalls");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--avoidCallsTo=avoidCalls");
  }

  @Test
  public void shouldPassDependencyDistanceOptionToJavaTask() {
    this.pitestTask.setDependencyDistance("distance");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--dependencyDistance=distance");
  }

  @Test
  public void shouldPassExcludedClassesOptionToJavaTask() {
    this.pitestTask.setExcludedClasses("String");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--excludedClasses=String");
  }

  @Test
  public void shouldPassExcludedTestClassesOptionToJavaTask() {
    this.pitestTask.setExcludedTestClasses("String");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--excludedTestClasses=String");
  }

  @Test
  public void shouldPassExcludedMethodsOptionToJavaTask() {
    this.pitestTask.setExcludedMethods("toString");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--excludedMethods=toString");
  }

  @Test
  public void shouldPassJvmArgsOptionToJavaTask() {
    this.pitestTask.setJvmArgs("-Da=a");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--jvmArgs=-Da=a");
  }

  @Test
  public void shouldPassMaxMutationsPerClassOptionToJavaTask() {
    this.pitestTask.setMaxMutationsPerClass("10");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--maxMutationsPerClass=10");
  }

  @Test
  public void shouldNotPassMutateStaticInitsOptionToJavaTaskWhenNoValueSet() {
    this.pitestTask.execute(this.java);
    verify(this.arg, never()).setValue("--mutateStaticInits=true");
  }

  @Test
  public void shouldPassDetectInlinedCodeOptionToJavaTask() {
    this.pitestTask.setDetectInlinedCode("true");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--detectInlinedCode=true");
  }

  @Test
  public void shouldNotPassInlinedCodeOptionToJavaTaskWhenNoValueSet() {
    this.pitestTask.execute(this.java);
    verify(this.arg, never()).setValue("--detectInlinedCode=true");
  }

  @Test
  public void shouldPassMutateInlinedCodeOptionToJavaTaskWhenValueIsFalse() {
    this.pitestTask.setDetectInlinedCode("false");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--detectInlinedCode=false");
  }

  @Test
  public void shouldPassMutatorsOptionToJavaTask() {
    this.pitestTask.setMutators("a,b");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--mutators=a,b");
  }

  @Test
  public void shouldPassFeaturesOptionToJavaTask() {
    this.pitestTask.setFeatures("FOO,BAR(a[1] a[2])");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--features=FOO,BAR(a[1] a[2])");
  }

  @Test
  public void shouldPassOutputFormatsOptionToJavaTask() {
    this.pitestTask.setOutputFormats("XML");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--outputFormats=XML");
  }

  @Test
  public void shouldPassReportDirOptionToJavaTask() {
    this.pitestTask.setReportDir("report/");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--reportDir=report/");
  }

  @Test
  public void shouldPassTargetClassesOptionToJavaTask() {
    this.pitestTask.setTargetClasses("com.*");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--targetClasses=com.*");
  }

  @Test
  public void shouldPassTargetTestsOptionToJavaTask() {
    this.pitestTask.setTargetTests("Test*");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--targetTests=Test*");
  }

  @Test
  public void shouldPassThreadsOptionToJavaTask() {
    this.pitestTask.setThreads("4");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--threads=4");
  }

  @Test
  public void shouldPassTimeoutConstsOptionToJavaTask() {
    this.pitestTask.setTimeoutConst("100");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--timeoutConst=100");
  }

  @Test
  public void shouldPassTimeoutFactorOptionToJavaTask() {
    this.pitestTask.setTimeoutFactor("1.20");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--timeoutFactor=1.20");
  }

  @Test
  public void shouldPassVerboseFlagToJavaTaskWhenValueIsTrue() {
    this.pitestTask.setVerbose("true");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--verbose=true");
  }

  @Test
  public void shouldPassVerboseFlagToJavaTaskWhenValueIsFalse() {
    this.pitestTask.setVerbose("false");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--verbose=false");
  }

  @Test
  public void shouldPassIncludedGroupsOptionToJavaTask() {
    this.pitestTask.setIncludedGroups("foo");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--includedGroups=foo");
  }

  @Test
  public void shouldPassExcludedGroupsOptionToJavaTask() {
    this.pitestTask.setExcludedGroups("foo");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--excludedGroups=foo");
  }

  @Test
  public void shouldPassIncludedTestMethodsOptionToJavaTask() {
    this.pitestTask.setIncludedTestMethods("footest");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--includedTestMethods=footest");
  }

  @Test
  public void shouldPassMutableCodePathsToJavaTask() {
    this.pitestTask.setMutableCodePaths("foo");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--mutableCodePaths=foo");
  }

  @Test
  public void shouldOnlyPassTheSpecifiedOptions() throws Exception {
    this.pitestTask.setVerbose("true");
    this.pitestTask.execute(this.java);

    verify(this.arg).setValue("--verbose=true");
    verify(this.arg).setValue("--targetClasses=com.*");
    verify(this.arg).setValue("--reportDir=report/");
    verify(this.arg).setValue("--sourceDirs=src/");
    verify(this.arg).setValue("--includeLaunchClasspath=false");
    verify(this.arg).setValue(startsWith("--classPath="));
    verifyNoMoreInteractions(this.arg);
  }

  @Test
  public void shouldFailtOnError() throws Exception {
    this.pitestTask.execute(this.java);

    verify(this.java).setFailonerror(true);
  }

  @Test
  public void shouldForkWhenExecuted() throws Exception {
    this.pitestTask.execute(this.java);

    verify(this.java).setFork(true);
  }

  @Test
  public void shouldExecute() throws Exception {
    this.pitestTask.execute(this.java);

    verify(this.java).execute();
  }

  @Test
  public void shouldExecutePitMainClass() throws Exception {
    this.pitestTask.execute(this.java);

    verify(this.java).setClassname(
        MutationCoverageReport.class.getCanonicalName());
  }

  @Test
  public void shouldFailWhenNoClasspathSupplied() throws Exception {
    this.exception.expect(BuildException.class);
    this.exception.expectMessage("You must specify the classpath.");

    this.pitestTask = new PitestTask();
    this.pitestTask.execute(this.java);
  }

  @Test
  public void shouldFailWhenNoTargetClassesSupplied() throws Exception {
    this.exception.expect(BuildException.class);
    this.exception.expectMessage("You must specify the targetClasses.");

    this.pitestTask = new PitestTask();
    this.pitestTask.setClasspath("bin/");
    this.pitestTask.setPitClasspath("foo/");
    this.pitestTask.setProject(this.project);

    this.pitestTask.execute(this.java);
  }

  @Test
  public void shouldFailWhenNoReportDirSupplied() throws Exception {
    this.exception.expect(BuildException.class);
    this.exception.expectMessage("You must specify the reportDir.");

    this.pitestTask = new PitestTask();
    this.pitestTask.setClasspath("bin/");
    this.pitestTask.setPitClasspath("foo/");
    this.pitestTask.setProject(this.project);
    this.pitestTask.setTargetClasses("com.*");

    this.pitestTask.execute(this.java);
  }

  @Test
  public void shouldFailWhenNoSourceDirSupplied() throws Exception {
    this.exception.expect(BuildException.class);
    this.exception.expectMessage("You must specify the sourceDirs.");

    this.pitestTask = new PitestTask();
    this.pitestTask.setClasspath("bin/");
    this.pitestTask.setPitClasspath("foo/");
    this.pitestTask.setProject(this.project);
    this.pitestTask.setTargetClasses("com.*");
    this.pitestTask.setReportDir("report/");

    this.pitestTask.execute(this.java);
  }

  @Test
  public void shouldSetPitClasspathOnJavaTask() throws Exception {
    final String classpath = "bin/" + File.pathSeparator + "lib/util.jar";
    this.pitestTask.setPitClasspath(classpath);
    this.pitestTask.execute(this.java);

    verify(this.java).setClasspath(argThat(new PathMatcher(classpath)));
  }

  @Test
  public void shouldPassAnalysisClassPathToPit() throws Exception {
    this.pitestTask.setClasspath("Foo" + File.pathSeparator + "Bar");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--classPath=Foo,Bar");
  }

  @Test
  public void shouldPassTimestampedDirectoryFlagToJavaTaskWhenValueIsTrue()
      throws Exception {
    this.pitestTask.setTimestampedReports("true");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--timestampedReports=true");
  }

  @Test
  public void shouldPassTimestampedDirectoryFlagToJavaTaskWhenValueIsFalse()
      throws Exception {
    this.pitestTask.setTimestampedReports("false");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--timestampedReports=false");
  }

  @Test
  public void shouldSetPitClasspathAntReferenceOnJavaTask() throws Exception {
    final String classpath = "app.classpath";
    final Object reference = "antReference";
    when(this.project.getReference(classpath)).thenReturn(reference);

    this.pitestTask.setPitClasspath(classpath);
    this.pitestTask.execute(this.java);

    verify(this.java).setClasspath(
        argThat(new PathMatcher(reference.toString())));
  }


  @Test
  public void shouldPassClasspathAntReferenceToPit() throws Exception {
    final String classpath = "app.classpath";
    final Object reference = "antReference";
    when(this.project.getReference(classpath)).thenReturn(reference);

    this.pitestTask.setClasspath(classpath);
    this.pitestTask.execute(this.java);

    verify(this.arg).setValue("--classPath=" + reference.toString());
  }

  @Test
  public void shouldPassHistoryInputLocationToJavaTask() {
    this.pitestTask.setHistoryInputLocation("foo");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--historyInputLocation=foo");
  }

  @Test
  public void shouldPassHistoryOutputLocationToJavaTask() {
    this.pitestTask.setHistoryOutputLocation("foo");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--historyOutputLocation=foo");
  }

  @Test
  public void shouldIgnoreEmptyListOfExcludedClasses() {
    this.pitestTask.setExcludedClasses("");
    this.pitestTask.execute(this.java);
    verify(this.arg, never()).setValue("--excludedClasses=");
  }

  @Test
  public void shouldPassMutationThresholdToJavaTask() {
    this.pitestTask.setMutationThreshold("42");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--mutationThreshold=42");
  }

  @Test
  public void shouldPassMaxSurvivorsToJavaTask() {
    this.pitestTask.setMaxSurviving("42");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--maxSurviving=42");
  }

  @Test
  public void shouldPassCoverageThresholdToJavaTask() {
    this.pitestTask.setCoverageThreshold("42");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--coverageThreshold=42");
  }

  @Test
  public void shouldPassMutationEngineToJavaTask() {
    this.pitestTask.setMutationEngine("foo");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--mutationEngine=foo");
  }

  @Test
  public void shouldPassJVMToJavaTask() {
    this.pitestTask.setJVM("foo");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--jvmPath=foo");
  }

  @Test
  public void shouldPassClasspathJarFlagToJavaTask() {
    this.pitestTask.setUseClasspathJar("true");
    this.pitestTask.execute(this.java);
    verify(this.arg).setValue("--useClasspathJar=true");
  }
  
  private static class PathMatcher extends ArgumentMatcher<Path> {

    private final String[] expectedPaths;

    public PathMatcher(final String path) {
      this.expectedPaths = path.split(File.pathSeparator);
    }

    @Override
    public boolean matches(final Object argument) {
      final Path argPath = (Path) argument;
      final String[] paths = argPath.toString().split(File.pathSeparator);
      final boolean matches = paths.length == this.expectedPaths.length;
      if (matches) {
        for (final String expectedPathElement : this.expectedPaths) {
          if (isNotPresent(paths, expectedPathElement)) {
            return false;
          }
        }
      }
      return matches;
    }

    private boolean isNotPresent(final String[] paths,
        final String expectedPathElement) {
      final String element = normalizePath(expectedPathElement);
      for (final String pathElement : paths) {
        if (pathElement.endsWith(element)) {
          return false;
        }
      }
      return true;
    }

    private String normalizePath(final String expectedPathElement) {
      String element = expectedPathElement;
      element = element.replace("/", File.separator);
      element = element.replace("\\", File.separator);
      if (element.endsWith(File.separator)) {
        element = element.substring(0, element.length() - 1);
      }
      return element;
    }
  }
}
