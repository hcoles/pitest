package org.pitest.coverage.execute;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pitest.SystemTest;
import org.pitest.classinfo.ClassName;
import org.pitest.coverage.ClassStatistics;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.MutableList;
import org.pitest.functional.SideEffect1;
import org.pitest.functional.predicate.Predicate;
import org.pitest.internal.ClassPath;
import org.pitest.internal.IsolationUtils;
import org.pitest.internal.classloader.DefaultPITClassloader;
import org.pitest.junit.JUnitCompatibleConfiguration;
import org.pitest.mutationtest.instrument.JarCreatingJarFinder;
import org.pitest.util.ProcessArgs;
import org.pitest.util.SocketFinder;

import com.example.coverage.execute.samples.exceptions.CoveredBeforeExceptionTestee;
import com.example.coverage.execute.samples.exceptions.TestThrowsExceptionFromLargeMethodTestee;
import com.example.coverage.execute.samples.exceptions.TestsClassWithException;
import com.example.coverage.execute.samples.exceptions.ThrowsExceptionFromLargeMethodTestee;
import com.example.coverage.execute.samples.exceptions.ThrowsExceptionTestee;
import com.example.coverage.execute.samples.simple.Testee;
import com.example.coverage.execute.samples.simple.Testee2;
import com.example.coverage.execute.samples.simple.TesteeWithMultipleLines;
import com.example.coverage.execute.samples.simple.Tests;
import com.example.coverage.execute.samples.simple.TestsForMultilineCoverage;

@Category(SystemTest.class)
public class CoverageProcessSystemTest {


  // check all the specialised implementations broadly work
  @Test
  public void shouldCalculateCoverageForSingleLineMethods() throws IOException,
      InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultilineCoverage.class);
    assertCoverage(coveredClasses, "test1", 1);
  }

  @Test
  public void shouldCalculateCoverageFor2LineMethods() throws IOException,
      InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultilineCoverage.class);
    assertCoverage(coveredClasses, "test2", 2);
  }

  @Test
  public void shouldCalculateCoverageFor3LineMethods() throws IOException,
      InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultilineCoverage.class);
    assertCoverage(coveredClasses, "test3", 3);
  }

  @Test
  public void shouldCalculateCoverageFor4LineMethods() throws IOException,
      InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultilineCoverage.class);
    assertCoverage(coveredClasses, "test4", 4);
  }

  @Test
  public void shouldCalculateCoverageFor5LineMethods() throws IOException,
      InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultilineCoverage.class);
    assertCoverage(coveredClasses, "test5", 5);
  }

  @Test
  public void shouldCalculateCoverageFor6LineMethods() throws IOException,
      InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultilineCoverage.class);
    assertCoverage(coveredClasses, "test6", 6);
  }

  @Test
  public void shouldCalculateCoverageFor7LineMethods() throws IOException,
      InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultilineCoverage.class);
    assertCoverage(coveredClasses, "test7", 7);
  }

  @Test
  public void shouldCalculateCoverageFor8LineMethods() throws IOException,
      InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultilineCoverage.class);
    assertCoverage(coveredClasses, "test8", 8);
  }

  @Test
  public void shouldCalculateCoverageFor9LineMethods() throws IOException,
      InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultilineCoverage.class);
    assertCoverage(coveredClasses, "test9", 9);
  }

  @Test
  public void shouldCalculateCoverageFor10LineMethods() throws IOException,
      InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultilineCoverage.class);
    assertCoverage(coveredClasses, "test10", 10);
  }

  @Test
  public void shouldCalculateCoverageFor11LineMethods() throws IOException,
      InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultilineCoverage.class);
    assertCoverage(coveredClasses, "test11", 11);
  }

  @Test
  public void shouldCalculateCoverageFor12LineMethods() throws IOException,
      InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultilineCoverage.class);
    assertCoverage(coveredClasses, "test12", 12);
  }

  @Test
  public void shouldCalculateCoverageFor13LineMethods() throws IOException,
      InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultilineCoverage.class);
    assertCoverage(coveredClasses, "test13", 13);
  }

  @Test
  public void shouldCalculateCoverageFor14LineMethods() throws IOException,
      InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultilineCoverage.class);
    assertCoverage(coveredClasses, "test14", 14);
  }

  @Test
  public void shouldCalculateCoverageFor15LineMethods() throws IOException,
      InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultilineCoverage.class);
    assertCoverage(coveredClasses, "test15", 15);
  }

  @Test
  public void shouldCalculateCoverageForLargeLineMethods() throws IOException,
      InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultilineCoverage.class);
    assertCoverage(coveredClasses, "test30", 30);
  }


  @Test
  public void shouldCalculateCoverageForAllRelevantClasses()
      throws IOException, InterruptedException, ExecutionException {

    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(Tests.class);

    printCoverage(coveredClasses);

    assertTrue(coveredClasses.contains(coverageFor(Testee2.class)));
    assertTrue(coveredClasses.contains(coverageFor(Testee.class)));
    assertTrue(coveredClasses
        .contains(coverageFor(TesteeWithMultipleLines.class)));
  }

  private void printCoverage(final FunctionalList<CoverageResult> coveredClasses) {
    for (final CoverageResult i : coveredClasses) {
      for (final ClassStatistics j : i.getCoverage()) {
        System.out.println(j);
      }
    }
  }

  @Test
  public void shouldCalculateCoverageForSmallMethodThatThrowsException()
      throws IOException, InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsClassWithException.class);
    assertTrue(coveredClasses
        .contains(coverageFor(CoveredBeforeExceptionTestee.class)));
    assertTrue(coveredClasses
        .contains(coverageFor(ThrowsExceptionTestee.class, Arrays.asList(5,6,10))));
  }
  
  @Test
  public void shouldCalculateCoverageForLargeMethodThatThrowsException()
      throws IOException, InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestThrowsExceptionFromLargeMethodTestee.class);
    assertTrue(coveredClasses
        .contains(coverageFor(ThrowsExceptionFromLargeMethodTestee.class, Arrays.asList(7,35,41))));
  }

  public static class TestInDifferentClassLoader {
    @Test
    public void testFoo() {
      final ClassLoader cl = new DefaultPITClassloader(new ClassPath(),
          IsolationUtils.bootClassLoader());
      final Testee testee = new Testee();
      final Runnable r = (Runnable) IsolationUtils.cloneForLoader(testee, cl);
      r.run();
    }

  }

  @Test
  public void shouldCalculateCoverageOfClassesRunInDifferentClassLoader()
      throws IOException, InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestInDifferentClassLoader.class);
    assertTrue(coveredClasses.contains(coverageFor(Testee2.class)));
    assertTrue(coveredClasses.contains(coverageFor(Testee.class)));
  }

  public static class ReliesOnNewLine {
    public static String parseNewLines() throws IOException {
      final StringWriter sw = new StringWriter();
      final PrintWriter pw = new PrintWriter(sw);
      pw.println("foo");
      pw.println("bar");

      final BufferedReader in = new BufferedReader(new StringReader(sw
          .getBuffer().toString()));
      return in.readLine();
    }
  }

  public static class ReliesOnNewLineTest {
    @Test
    public void testNewLine() throws IOException {
      assertEquals("foo", ReliesOnNewLine.parseNewLines());
    }
  }

  @Test
  public void shouldNotCorruptedTheSystemNewLineProperty() throws Exception {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(ReliesOnNewLineTest.class);
    assertFalse(coveredClasses.contains(failingTest()));
  }

  private F<CoverageResult, Boolean> failingTest() {
    return new F<CoverageResult, Boolean>() {

      public Boolean apply(final CoverageResult a) {
        return !a.isGreenTest();
      }

    };
  }

  private FunctionalList<CoverageResult> runCoverageForTest(final Class<?> test)
      throws IOException, InterruptedException, ExecutionException {

    final CoverageOptions sa = new CoverageOptions(coverOnlyTestees(),
        new JUnitCompatibleConfiguration(), true, -1);

    final FunctionalList<CoverageResult> coveredClasses = new MutableList<CoverageResult>();

    final SideEffect1<CoverageResult> handler = new SideEffect1<CoverageResult>() {

      public void apply(final CoverageResult a) {
        coveredClasses.add(a);
      }

    };

    final JarCreatingJarFinder agent = new JarCreatingJarFinder();
    final SocketFinder sf = new SocketFinder();
    final CoverageProcess process = new CoverageProcess(ProcessArgs
        .withClassPath(new ClassPath()).andJavaAgentFinder(agent), sa,
        sf.getNextAvailableServerSocket(), Arrays.asList(test.getName()),
        handler);
    process.start();
    process.waitToDie();
    agent.close();
    return coveredClasses;
  }

  private F<CoverageResult, Boolean> coverageFor(final Class<?> class1) {
    return new F<CoverageResult, Boolean>() {

      public Boolean apply(final CoverageResult a) {
        return FCollection.contains(a.getCoverage(), resultFor(class1));
      }

      private F<ClassStatistics, Boolean> resultFor(final Class<?> class1) {
        return new F<ClassStatistics, Boolean>() {

          public Boolean apply(final ClassStatistics a) {

            return a.getClassName().equals(ClassName.fromClass(class1))
                && a.wasVisited();
          }

        };
      }
    };
  }
  
  private F<CoverageResult, Boolean> coverageFor(final Class<?> class1, final Collection<Integer> lines) {
    return new F<CoverageResult, Boolean>() {

      public Boolean apply(final CoverageResult a) {
        return FCollection.contains(a.getCoverage(), resultFor(class1));
      }

      private F<ClassStatistics, Boolean> resultFor(final Class<?> class1) {
        return new F<ClassStatistics, Boolean>() {

          public Boolean apply(final ClassStatistics a) {
            Set<Integer> required = new HashSet<Integer>(lines);
            
            return a.getClassName().equals(ClassName.fromClass(class1))
                && a.getUniqueVisitedLines().containsAll(required);
          }

        };
      }
    };
  }


  private Predicate<String> coverOnlyTestees() {

    return new Predicate<String>() {

      public Boolean apply(final String a) {
        return a.contains("Testee");
      }

    };
  }

  private F<CoverageResult, Boolean> coverage(final String testName,
      final int numberOfLines) {
    return new F<CoverageResult, Boolean>() {

      public Boolean apply(final CoverageResult a) {
        return a.getTestUnitDescription().getName().startsWith(testName)
            && (a.getNumberOfCoveredLines() == (numberOfLines + 1));
      }

    };
  }
  
  private void assertCoverage(
      final FunctionalList<CoverageResult> coveredClasses,
      final String testName, final int numberOfLines) {
    assertTrue(coveredClasses.contains(coverage(testName, numberOfLines)));
  }

}
