package org.pitest.ant;

import static org.mockito.Matchers.argThat;
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
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pitest.mutationtest.MutationCoverageReport;

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
    this.pitestTask.setTargetClasses("com.*");
    this.pitestTask.setReportDir("report/");
    this.pitestTask.setSourceDir("src/");
    this.pitestTask.setProject(this.project);
  }

  @Test
  public void shouldPassAllOptionsToTheJavaTask() throws Exception {
    this.pitestTask.setAvoidCallsTo("avoidCalls");
    this.pitestTask.setDependencyDistance("distance");
    this.pitestTask.setExcludedClasses("String");
    this.pitestTask.setExcludedMethods("toString");
    this.pitestTask.setIncludeJarFiles("includeJars");
    this.pitestTask.setInScopeClasses("MyClass");
    this.pitestTask.setJvmArgs("-Da=a");
    this.pitestTask.setMaxMutationsPerClass("10");
    this.pitestTask.setMutateStaticInits("true");
    this.pitestTask.setMutators("a,b");
    this.pitestTask.setOutputFormats("XML");
    this.pitestTask.setReportDir("report/");
    this.pitestTask.setTargetClasses("com.*");
    this.pitestTask.setTargetTests("Test*");
    this.pitestTask.setThreads("4");
    this.pitestTask.setTimeoutConst("100");
    this.pitestTask.setTimeoutFactor("1.20");
    this.pitestTask.setVerbose("true");

    this.pitestTask.execute(this.java);

    verify(this.arg).setValue("--avoidCallsTo=avoidCalls");
    verify(this.arg).setValue("--dependencyDistance=distance");
    verify(this.arg).setValue("--excludedClasses=String");
    verify(this.arg).setValue("--excludedMethods=toString");
    verify(this.arg).setValue("--includeJarFiles=includeJars");
    verify(this.arg).setValue("--inScopeClasses=MyClass");
    verify(this.arg).setValue("--jvmArgs=-Da=a");
    verify(this.arg).setValue("--maxMutationsPerClass=10");
    verify(this.arg).setValue("--mutateStaticInits=true");
    verify(this.arg).setValue("--mutators=a,b");
    verify(this.arg).setValue("--outputFormats=XML");
    verify(this.arg).setValue("--reportDir=report/");
    verify(this.arg).setValue("--targetClasses=com.*");
    verify(this.arg).setValue("--targetTests=Test*");
    verify(this.arg).setValue("--threads=4");
    verify(this.arg).setValue("--timeoutConst=100");
    verify(this.arg).setValue("--timeoutFactor=1.20");
    verify(this.arg).setValue("--verbose=true");
  }

  @Test
  public void shouldOnlyPassTheSpecifiedOptions() throws Exception {
    this.pitestTask.setVerbose("true");

    this.pitestTask.execute(this.java);

    verify(this.arg).setValue("--verbose=true");
    verify(this.arg).setValue("--targetClasses=com.*");
    verify(this.arg).setValue("--reportDir=report/");
    verify(this.arg).setValue("--sourceDir=src/");
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
    this.pitestTask.setProject(this.project);

    this.pitestTask.execute(this.java);
  }

  @Test
  public void shouldFailWhenNoReportDirSupplied() throws Exception {
    this.exception.expect(BuildException.class);
    this.exception.expectMessage("You must specify the reportDir.");

    this.pitestTask = new PitestTask();
    this.pitestTask.setClasspath("bin/");
    this.pitestTask.setProject(this.project);
    this.pitestTask.setTargetClasses("com.*");

    this.pitestTask.execute(this.java);
  }

  @Test
  public void shouldFailWhenNoSourceDirSupplied() throws Exception {
    this.exception.expect(BuildException.class);
    this.exception.expectMessage("You must specify the sourceDir.");

    this.pitestTask = new PitestTask();
    this.pitestTask.setClasspath("bin/");
    this.pitestTask.setProject(this.project);
    this.pitestTask.setTargetClasses("com.*");
    this.pitestTask.setReportDir("report/");

    this.pitestTask.execute(this.java);
  }

  @Test
  @Ignore("broken!")
  public void shouldSetClasspathOnJavaTask() throws Exception {
    final String classpath = "bin/;lib/util.jar";
    this.pitestTask.setClasspath(classpath);
    this.pitestTask.execute(this.java);

    verify(this.java).setClasspath(argThat(new PathMatcher(classpath)));
  }

  @Test
  public void shouldSetClasspathAntReferenceOnJavaTask() throws Exception {
    final String classpath = "app.classpath";
    final Object reference = "antReference";
    when(this.project.getReference(classpath)).thenReturn(reference);

    this.pitestTask.setClasspath(classpath);
    this.pitestTask.execute(this.java);

    verify(this.java).setClasspath(
        argThat(new PathMatcher(reference.toString())));
  }

  private static class PathMatcher extends ArgumentMatcher<Path> {

    private static final String PATH_SEPARATOR = ";";
    private final String[]      expectedPaths;

    public PathMatcher(final String path) {
      this.expectedPaths = path.split(PATH_SEPARATOR);
    }

    @Override
    public boolean matches(final Object argument) {
      final Path argPath = (Path) argument;
      final String[] paths = argPath.toString().split(PATH_SEPARATOR);
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
