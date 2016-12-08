package org.pitest.coverage.execute;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pitest.SystemTest;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassPath;
import org.pitest.coverage.BlockLocation;
import org.pitest.coverage.CoverageResult;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.MutableList;
import org.pitest.functional.SideEffect1;
import org.pitest.functional.predicate.Predicate;
import org.pitest.junit.JUnitCompatibleConfiguration;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MethodName;
import org.pitest.mutationtest.execute.DefaultPITClassloader;
import org.pitest.mutationtest.tooling.JarCreatingJarFinder;
import org.pitest.process.LaunchOptions;
import org.pitest.process.ProcessArgs;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.util.ExitCode;
import org.pitest.util.IsolationUtils;
import org.pitest.util.SocketFinder;

import com.example.coverage.execute.samples.exceptions.CoveredBeforeExceptionTestee;
import com.example.coverage.execute.samples.exceptions.TestThrowsExceptionFromLargeMethodTestee;
import com.example.coverage.execute.samples.exceptions.TestThrowsExceptionInFinallyBlock;
import com.example.coverage.execute.samples.exceptions.TestsClassWithException;
import com.example.coverage.execute.samples.exceptions.ThrowsExceptionFromLargeMethodTestee;
import com.example.coverage.execute.samples.exceptions.ThrowsExceptionInFinallyBlockTestee;
import com.example.coverage.execute.samples.exceptions.ThrowsExceptionTestee;
import com.example.coverage.execute.samples.simple.Testee;
import com.example.coverage.execute.samples.simple.Testee2;
import com.example.coverage.execute.samples.simple.TesteeWithComplexConstructorsTest;
import com.example.coverage.execute.samples.simple.TesteeWithMultipleLines;
import com.example.coverage.execute.samples.simple.Tests;
import com.example.coverage.execute.samples.simple.TestsForMultiBlockCoverage;

@Category(SystemTest.class)
public class CoverageProcessSystemTest {

  private final MethodName foo = MethodName.fromString("foo");

  @Test
  public void shouldRecordSomeCoverage() throws IOException,
  InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coverage = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertFalse(coverage.iterator().next().getCoverage().isEmpty());
  }

  // check all the specialised implementations broadly work
  @Test
  public void shouldCalculateCoverageForSingleBlockMethods()
      throws IOException, InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertCoverage(coveredClasses, "test1", 1);
  }

  // @Test
  // public void shouldCalculateCoverageFor2BlockMethods() throws IOException,
  // InterruptedException, ExecutionException {
  // final FunctionalList<CoverageResult> coveredClasses =
  // runCoverageForTest(TestsForMultiBlockCoverage.class);
  // assertCoverage(coveredClasses, "test2", 2);
  // }

  @Test
  public void shouldCalculateCoverageFor3BlockMethods() throws IOException,
  InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertCoverage(coveredClasses, "test3", 2);
  }

  @Test
  public void shouldCalculateCoverageForConstructors() throws IOException,
  InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TesteeWithComplexConstructorsTest.class);
    assertTrue(coversBlock(coveredClasses, "testHigh", 0));
    assertTrue(coversBlock(coveredClasses, "testHigh", 1));
    assertFalse(coversBlock(coveredClasses, "testHigh", 2));

    assertTrue(coversBlock(coveredClasses, "testLow", 0));
    assertTrue(coversBlock(coveredClasses, "testLow", 2));
    assertFalse(coversBlock(coveredClasses, "testLow", 1));
  }

  @Test
  public void shouldCalculateCoverageFor4BlockMethods() throws IOException,
  InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertCoverage(coveredClasses, "test4", 2);
  }

  @Test
  public void shouldCalculateCoverageFor5BlockMethods() throws IOException,
  InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertCoverage(coveredClasses, "test5", 2);
  }

  @Test
  public void shouldCalculateCoverageForBlockMethods() throws IOException,
  InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertCoverage(coveredClasses, "test6", 2);
  }

  @Test
  public void shouldCalculateCoverageFor7BlockMethods() throws IOException,
  InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertCoverage(coveredClasses, "test7", 2);
  }

  @Test
  public void shouldCalculateCoverageFor8BlockMethods() throws IOException,
  InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertCoverage(coveredClasses, "test8", 2);
  }

  @Test
  public void shouldCalculateCoverageFor9BlockMethods() throws IOException,
  InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertCoverage(coveredClasses, "test9", 2);
  }

  @Test
  public void shouldCalculateCoverageFor10BlockMethods() throws IOException,
  InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertCoverage(coveredClasses, "test10", 2);
  }

  @Test
  public void shouldCalculateCoverageFor11BlockMethods() throws IOException,
  InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertCoverage(coveredClasses, "test11", 2);
  }

  @Test
  public void shouldCalculateCoverageFor12BlockMethods() throws IOException,
  InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertCoverage(coveredClasses, "test12", 2);
  }

  @Test
  public void shouldCalculateCoverageFor13BlockMethods() throws IOException,
  InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertCoverage(coveredClasses, "test13", 2);
  }

  @Test
  public void shouldCalculateCoverageFor14BlockMethods() throws IOException,
  InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertCoverage(coveredClasses, "test14", 2);
  }

  @Test
  public void shouldCalculateCoverageFor15BlockMethods() throws IOException,
  InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertCoverage(coveredClasses, "test15", 2);
  }

  @Test
  public void shouldCalculateCoverageForLargeBlockMethods() throws IOException,
  InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertCoverage(coveredClasses, "testMany", 2);
  }

  @Test
  public void shouldCalculateCoverageForAllRelevantClasses()
      throws IOException, InterruptedException, ExecutionException {

    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(Tests.class);

    assertTrue(coveredClasses.contains(coverageFor(Testee2.class)));
    assertTrue(coveredClasses.contains(coverageFor(Testee.class)));
    assertTrue(coveredClasses
        .contains(coverageFor(TesteeWithMultipleLines.class)));
  }

  @Test
  public void shouldCalculateCoverageForSmallMethodThatThrowsException()
      throws IOException, InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestsClassWithException.class);
    assertTrue(coveredClasses
        .contains(coverageFor(CoveredBeforeExceptionTestee.class)));

    ClassName throwsException = ClassName
        .fromClass(ThrowsExceptionTestee.class);

    assertTrue(coveredClasses.contains(coverageFor(BlockLocation.blockLocation(
        Location.location(throwsException, this.foo, "()V"), 0))));

    assertTrue(coveredClasses.contains(coverageFor(BlockLocation.blockLocation(
        Location.location(throwsException,
            MethodName.fromString("throwsException"), "()V"), 0))));

  }

  @Test
  public void shouldCalculateCoverageForMethodThatThrowsExceptionWithFinallyBlock()
      throws IOException, InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestThrowsExceptionInFinallyBlock.class);

    ClassName clazz = ClassName
        .fromClass(ThrowsExceptionInFinallyBlockTestee.class);

    assertTrue(coveredClasses.contains(coverageFor(BlockLocation.blockLocation(
        Location.location(clazz, this.foo, "()V"), 0))));

    assertTrue(coveredClasses.contains(coverageFor(BlockLocation.blockLocation(
        Location.location(clazz, this.foo, "()V"), 1))));
  }

  @Test
  public void shouldCalculateCoverageForLargeMethodThatThrowsException()
      throws IOException, InterruptedException, ExecutionException {
    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(TestThrowsExceptionFromLargeMethodTestee.class);

    ClassName clazz = ClassName
        .fromClass(ThrowsExceptionFromLargeMethodTestee.class);

    assertTrue(coveredClasses.contains(coverageFor(BlockLocation.blockLocation(
        Location.location(clazz, this.foo, "()I"), 0))));

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

  @Test
  public void shouldFailWithExitCode() throws Exception {
    final SideEffect1<CoverageResult> noOpHandler = new SideEffect1<CoverageResult>() {
      @Override
      public void apply(final CoverageResult a) {
      }
    };

    final CoverageOptions sa = new CoverageOptions(coverOnlyTestees(),
        new JUnitCompatibleConfiguration(new TestGroupConfig(), Collections.<String>emptyList()), true, -1);
    final JarCreatingJarFinder agent = new JarCreatingJarFinder();
    final LaunchOptions lo = new LaunchOptions(agent);
    final SocketFinder sf = new SocketFinder();
    final CoverageProcess process = new CoverageProcess(ProcessArgs
        .withClassPath(classPathWithoutJUnit()).andLaunchOptions(lo), sa,
        sf.getNextAvailableServerSocket(),
        Arrays.asList(TestsForMultiBlockCoverage.class.getName()), noOpHandler);
    process.start();

    final ExitCode exitCode = process.waitToDie();
    assertEquals(ExitCode.JUNIT_ISSUE, exitCode);
  }

  private ClassPath classPathWithoutJUnit() {
    FunctionalList<File> cpWithoutJUnit = FCollection.filter(
        ClassPath.getClassPathElementsAsFiles(), new F<File, Boolean>() {
          @Override
          public Boolean apply(File file) {
            return !file.getName().contains("junit");
          }
        });

    return new ClassPath(cpWithoutJUnit);
  }

  private F<CoverageResult, Boolean> failingTest() {
    return new F<CoverageResult, Boolean>() {

      @Override
      public Boolean apply(final CoverageResult a) {
        return !a.isGreenTest();
      }

    };
  }

  private FunctionalList<CoverageResult> runCoverageForTest(final Class<?> test)
      throws IOException, InterruptedException, ExecutionException {

    final FunctionalList<CoverageResult> coveredClasses = new MutableList<CoverageResult>();

    runCoverageProcess(test, coveredClasses);
    return coveredClasses;
  }

  private void runCoverageProcess(final Class<?> test,
      final FunctionalList<CoverageResult> coveredClasses) throws IOException,
      InterruptedException {
    final SideEffect1<CoverageResult> handler = new SideEffect1<CoverageResult>() {

      @Override
      public void apply(final CoverageResult a) {
        coveredClasses.add(a);
      }

    };

    final CoverageOptions sa = new CoverageOptions(coverOnlyTestees(),
        new JUnitCompatibleConfiguration(new TestGroupConfig(), Collections.<String>emptyList()), true, -1);
    final JarCreatingJarFinder agent = new JarCreatingJarFinder();
    try {
      final LaunchOptions lo = new LaunchOptions(agent);
      final SocketFinder sf = new SocketFinder();
      final CoverageProcess process = new CoverageProcess(ProcessArgs
          .withClassPath(new ClassPath()).andLaunchOptions(lo), sa,
          sf.getNextAvailableServerSocket(), Arrays.asList(test.getName()),
          handler);
      process.start();

      final ExitCode exitCode = process.waitToDie();
      assertEquals(ExitCode.OK, exitCode);
    } finally {
      agent.close();
    }
  }

  private F<CoverageResult, Boolean> coverageFor(final Class<?> class1) {
    return new F<CoverageResult, Boolean>() {

      @Override
      public Boolean apply(final CoverageResult a) {
        return FCollection.contains(a.getCoverage(), resultFor(class1));
      }

      private F<BlockLocation, Boolean> resultFor(final Class<?> class1) {
        return new F<BlockLocation, Boolean>() {

          @Override
          public Boolean apply(final BlockLocation a) {

            return a.isFor(ClassName.fromClass(class1));
          }

        };
      }
    };
  }

  private F<CoverageResult, Boolean> coverageFor(final BlockLocation location) {
    return new F<CoverageResult, Boolean>() {
      @Override
      public Boolean apply(final CoverageResult a) {
        return a.getCoverage().contains(location);
      }
    };
  }

  private Predicate<String> coverOnlyTestees() {

    return new Predicate<String>() {

      @Override
      public Boolean apply(final String a) {
        return a.contains("Testee") && !a.endsWith("Test");
      }

    };
  }

  private F<CoverageResult, Boolean> coverage(final String testName,
      final int numberOfBlocks) {
    return new F<CoverageResult, Boolean>() {

      @Override
      public Boolean apply(final CoverageResult a) {
        return a.getTestUnitDescription().getName().startsWith(testName)
            && (a.getNumberOfCoveredBlocks() == numberOfBlocks);
      }

    };
  }

  private void assertCoverage(
      final FunctionalList<CoverageResult> coveredClasses,
      final String testName, final int numberOfBlocks) {
    assertTrue(coveredClasses.contains(coverage(testName, numberOfBlocks)));
  }

  private boolean coversBlock(
      final FunctionalList<CoverageResult> coveredClasses,
      final String testName, final int block) {
    return coveredClasses.contains(hitsBlock(testName, block));
  }

  private F<CoverageResult, Boolean> hitsBlock(final String testName,
      final int block) {
    return new F<CoverageResult, Boolean>() {
      @Override
      public Boolean apply(final CoverageResult a) {
        return a.getTestUnitDescription().getName().startsWith(testName)
            && (FCollection.contains(a.getCoverage(), hasBlock(block)));
      }

      private F<BlockLocation, Boolean> hasBlock(final int block) {
        return new F<BlockLocation, Boolean>() {
          @Override
          public Boolean apply(BlockLocation a) {
            System.out.println(a);
            return a.getBlock() == block;
          }

        };
      }

    };
  }

}
