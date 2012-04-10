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

import org.junit.Test;
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
import org.pitest.util.SocketFinder;
import org.pitest.util.ProcessArgs;

public class CoverageProcessTest {

  public static class Testee implements Runnable {
    public void foo() {
    }

    public void bar() {

    }

    public void run() {
      new Testee2().bar();
    }

  }

  public static class Testee2 {
    public void foo() {

    }

    public void bar() {

    }
  }

  public static class Tests {
    @Test
    public void testFoo() {
      final Testee testee = new Testee();
      testee.foo();

    }

    @Test
    public void testFoo2() {
      final Testee2 testee2 = new Testee2();
      testee2.foo();

    }
  }

  @Test
  public void shouldCalculateCoverageForAllRelevantClasses()
      throws IOException, InterruptedException {

    final FunctionalList<CoverageResult> coveredClasses = runCoverageForTest(Tests.class);

    assertTrue(coveredClasses.contains(coverageFor(Testee2.class)));
    assertTrue(coveredClasses.contains(coverageFor(Testee.class)));

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
      throws IOException, InterruptedException {
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
      throws IOException, InterruptedException {

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
        .withClassPath(new ClassPath()).andJavaAgentFinder(agent), sa, sf.getNextAvailableServerSocket(),
        Arrays.asList(test.getName()), handler);
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

            return a.getClassName().equals(class1.getName().replace(".", "/"))
                && a.wasVisited();
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

}
