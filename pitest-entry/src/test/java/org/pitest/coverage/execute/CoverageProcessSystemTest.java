package org.pitest.coverage.execute;

import com.example.coverage.execute.samples.exceptions.CoveredBeforeExceptionTestee;
import com.example.coverage.execute.samples.exceptions.TestThrowsExceptionFromLargeMethodTestee;
import com.example.coverage.execute.samples.exceptions.TestThrowsExceptionInFinallyBlock;
import com.example.coverage.execute.samples.exceptions.TestsClassWithException;
import com.example.coverage.execute.samples.exceptions.ThrowsExceptionFromLargeMethodTestee;
import com.example.coverage.execute.samples.exceptions.ThrowsExceptionInFinallyBlockTestee;
import com.example.coverage.execute.samples.exceptions.ThrowsExceptionTestee;
import com.example.coverage.execute.samples.executionindiscovery.AnExecutingTest;
import com.example.coverage.execute.samples.simple.ParentChildInitializationTest;
import com.example.coverage.execute.samples.simple.Testee;
import com.example.coverage.execute.samples.simple.Testee2;
import com.example.coverage.execute.samples.simple.TesteeWithComplexConstructorsTest;
import com.example.coverage.execute.samples.simple.TesteeWithMultipleLines;
import com.example.coverage.execute.samples.simple.Tests;
import com.example.coverage.execute.samples.simple.TestsForMultiBlockCoverage;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pitest.SystemTest;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassPath;
import org.pitest.coverage.BlockLocation;
import org.pitest.coverage.CoverageResult;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.config.TestPluginArguments;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.tooling.JarCreatingJarFinder;
import org.pitest.process.LaunchOptions;
import org.pitest.process.ProcessArgs;
import org.pitest.util.ExitCode;
import org.pitest.util.SocketFinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.pitest.util.Verbosity.VERBOSE;

@Category(SystemTest.class)
public class CoverageProcessSystemTest {

  @Test
  public void shouldRecordSomeCoverage() throws Exception {
    final List<CoverageResult> coverage = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertFalse(coverage.iterator().next().getCoverage().isEmpty());
  }

  // check all the specialised implementations broadly work
  @Test
  public void shouldCalculateCoverageForSingleBlockMethods()
      throws IOException, InterruptedException {
    final List<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertCoverage(coveredClasses, "test1", 1);
  }

  @Test
  public void shouldCalculateCoverageFor3BlockMethods() throws Exception {
    final List<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertCoverage(coveredClasses, "test3", 2);
  }

  @Test
  public void shouldCalculateCoverageForConstructors() throws Exception {
    final List<CoverageResult> coveredClasses = runCoverageForTest(TesteeWithComplexConstructorsTest.class);
    assertTrue(coversBlock(coveredClasses, "testHigh", 0));
    assertTrue(coversBlock(coveredClasses, "testHigh", 1));
    assertTrue(coversBlock(coveredClasses, "testHigh", 2));

    assertTrue(coversBlock(coveredClasses, "testLow", 0));
    assertTrue(coversBlock(coveredClasses, "testLow", 1));
    assertTrue(coversBlock(coveredClasses, "testLow", 4));
    assertFalse(coversBlock(coveredClasses, "testLow", 2));
  }

  @Test
  public void shouldCalculateCoverageFor4BlockMethods() throws Exception {
    final List<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertCoverage(coveredClasses, "test4", 2);
  }

  @Test
  public void shouldCalculateCoverageFor5BlockMethods() throws Exception {
    final List<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertCoverage(coveredClasses, "test5", 2);
  }

  @Test
  public void shouldCalculateCoverageForBlockMethods() throws Exception {
    final List<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertCoverage(coveredClasses, "test6", 2);
  }

  @Test
  public void shouldCalculateCoverageFor7BlockMethods() throws Exception {
    final List<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertCoverage(coveredClasses, "test7", 2);
  }

  @Test
  public void shouldCalculateCoverageFor8BlockMethods() throws Exception {
    final List<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertCoverage(coveredClasses, "test8", 2);
  }

  @Test
  public void shouldCalculateCoverageFor9BlockMethods() throws Exception {
    final List<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertCoverage(coveredClasses, "test9", 2);
  }

  @Test
  public void shouldCalculateCoverageFor10BlockMethods() throws IOException,
  InterruptedException, ExecutionException {
    final List<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertCoverage(coveredClasses, "test10", 2);
  }

  @Test
  public void shouldCalculateCoverageFor11BlockMethods() throws Exception {
    final List<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertCoverage(coveredClasses, "test11", 2);
  }

  @Test
  public void shouldCalculateCoverageFor12BlockMethods() throws Exception {
    final List<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertCoverage(coveredClasses, "test12", 2);
  }

  @Test
  public void shouldCalculateCoverageFor13BlockMethods() throws Exception {
    final List<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertCoverage(coveredClasses, "test13", 2);
  }

  @Test
  public void shouldCalculateCoverageFor14BlockMethods() throws Exception {
    final List<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertCoverage(coveredClasses, "test14", 2);
  }

  @Test
  public void shouldCalculateCoverageFor15BlockMethods() throws Exception {
    final List<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertCoverage(coveredClasses, "test15", 2);
  }

  @Test
  public void shouldCalculateCoverageForLargeBlockMethods() throws Exception {
    final List<CoverageResult> coveredClasses = runCoverageForTest(TestsForMultiBlockCoverage.class);
    assertCoverage(coveredClasses, "testMany", 2);
  }

  @Test
  public void shouldCalculateCoverageForAllRelevantClasses()
      throws Exception {

    final List<CoverageResult> coveredClasses = runCoverageForTest(Tests.class);

    assertThat(coveredClasses).anyMatch(coverageFor(Testee2.class));
    assertThat(coveredClasses).anyMatch(coverageFor(Testee.class));
    assertThat(coveredClasses).anyMatch(coverageFor(TesteeWithMultipleLines.class));
  }

  @Test
  public void shouldCalculateCoverageForSmallMethodThatThrowsException()
      throws Exception {
    final List<CoverageResult> coveredClasses = runCoverageForTest(TestsClassWithException.class);
    assertThat(coveredClasses).anyMatch(coverageFor(CoveredBeforeExceptionTestee.class));

    final ClassName throwsException = ClassName
        .fromClass(ThrowsExceptionTestee.class);

    assertThat(coveredClasses).anyMatch(coverageFor(BlockLocation.blockLocation(
        Location.location(throwsException, "foo", "()V"), 0)));

        assertThat(coveredClasses).anyMatch(coverageFor(BlockLocation.blockLocation(
        Location.location(throwsException,
            "throwsException", "()V"), 0)));

  }

  @Test
  public void shouldCalculateCoverageForMethodThatThrowsExceptionWithFinallyBlock()
      throws Exception {
    final List<CoverageResult> coveredClasses = runCoverageForTest(TestThrowsExceptionInFinallyBlock.class);

    final ClassName clazz = ClassName
        .fromClass(ThrowsExceptionInFinallyBlockTestee.class);

    assertThat(coveredClasses).anyMatch(coverageFor(BlockLocation.blockLocation(
        Location.location(clazz, "foo", "()V"), 0)));

        assertThat(coveredClasses).anyMatch(coverageFor(BlockLocation.blockLocation(
        Location.location(clazz, "foo", "()V"), 4)));
  }

  @Test
  public void shouldCalculateCoverageForLargeMethodThatThrowsException()
      throws Exception {
    final List<CoverageResult> coveredClasses = runCoverageForTest(TestThrowsExceptionFromLargeMethodTestee.class);

    final ClassName clazz = ClassName
        .fromClass(ThrowsExceptionFromLargeMethodTestee.class);

    assertThat(coveredClasses).anyMatch(coverageFor(BlockLocation.blockLocation(
        Location.location(clazz, "foo", "()I"), 0)));

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
  public void shouldNotCorruptTheSystemNewLineProperty() throws Exception {
    final List<CoverageResult> coveredClasses = runCoverageForTest(ReliesOnNewLineTest.class);
    assertThat(coveredClasses).noneMatch(failingTest());
  }

  @Test
  public void handlesParentChildInitializationOrderIssues() throws Exception {
    final List<CoverageResult> coveredClasses = runCoverageForTest(ParentChildInitializationTest.class);
    assertThat(coveredClasses)
            .anyMatch(coverageFor(ClassName.fromString("com.example.coverage.execute.samples.simple.TesteeChild")));
  }

  @Test
  public void gathersCoverageWhenTestsExecutedDuringDiscovery() throws Exception {
    final List<CoverageResult> coveredClasses = runCoverageForTest(AnExecutingTest.class);
    assertThat(coveredClasses)
            .anyMatch(coverageFor(ClassName.fromString("com.example.coverage.execute.samples.executionindiscovery.ATesteeClass")));
  }

  private Predicate<CoverageResult> failingTest() {
    return a -> !a.isGreenTest();
  }

  private List<CoverageResult> runCoverageForTest(final Class<?> test)
      throws IOException, InterruptedException {

    final List<CoverageResult> coveredClasses = new ArrayList<>();

    runCoverageProcess(test, coveredClasses);
    return coveredClasses;
  }

  private void runCoverageProcess(final Class<?> test,
      final List<CoverageResult> coveredClasses) throws IOException,
      InterruptedException {
    final Consumer<CoverageResult> handler = a -> coveredClasses.add(a);

    final CoverageOptions sa = new CoverageOptions(coverOnlyTestees(), excludeTests(), TestPluginArguments.defaults(), VERBOSE);

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

  private Predicate<CoverageResult> coverageFor(final Class<?> clazz) {
    return coverageFor(ClassName.fromClass(clazz));
  }

  private Predicate<CoverageResult> coverageFor(ClassName clazz) {
    return new Predicate<CoverageResult>() {

      @Override
      public boolean test(final CoverageResult a) {
        return FCollection.contains(a.getCoverage(), resultFor(clazz));
      }

      private Predicate<BlockLocation> resultFor(ClassName clazz) {
        return a -> a.isFor(clazz);
      }
    };
  }

  private Predicate<CoverageResult> coverageFor(final BlockLocation location) {
    return a -> a.getCoverage().contains(location);
  }

  private List<String> coverOnlyTestees() {
    return Arrays.asList("*Testee*");
  }

  private List<String> excludeTests() {
    return Arrays.asList("*Test");
  }

  private Predicate<CoverageResult> coverage(final String testName,
      final int numberOfBlocks) {
    return a -> a.getTestUnitDescription().getName().startsWith(testName)
        && (a.getNumberOfCoveredBlocks() == numberOfBlocks);
  }

  private void assertCoverage(
      final List<CoverageResult> coveredClasses,
      final String testName, final int numberOfBlocks) {
    assertThat(coveredClasses).anyMatch(coverage(testName, numberOfBlocks));
  }

  private boolean coversBlock(
      final List<CoverageResult> coveredClasses,
      final String testName, final int block) {
    return coveredClasses.stream().anyMatch(hitsBlock(testName, block));
  }

  private Predicate<CoverageResult> hitsBlock(final String testName,
      final int block) {
    return new Predicate<CoverageResult>() {
      @Override
      public boolean test(final CoverageResult a) {
        return a.getTestUnitDescription().getName().startsWith(testName)
            && (FCollection.contains(a.getCoverage(), hasBlock(block)));
      }

      private Predicate<BlockLocation> hasBlock(final int block) {
        return a -> {
          System.out.println(a);
          return a.getBlock() == block;
        };
      }

    };
  }

}
