package org.pitest.coverage.execute;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.pitest.Pitest;
import org.pitest.coverage.ClassStatistics;
import org.pitest.extension.TestFilter;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.NullDiscoveryListener;
import org.pitest.extension.common.UnGroupedStrategy;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.MutableList;
import org.pitest.functional.Option;
import org.pitest.functional.SideEffect1;
import org.pitest.functional.predicate.Predicate;
import org.pitest.internal.ClassPath;
import org.pitest.junit.JUnitCompatibleConfiguration;
import org.pitest.util.WrappingProcess;

public class CoverageProcessTest {

  public static class Testee {
    public void foo() {

    }

    public void bar() {

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

    final List<TestUnit> tus = Pitest.findTestUnitsForAllSuppliedClasses(
        new JUnitCompatibleConfiguration(), new NullDiscoveryListener(),
        new UnGroupedStrategy(), Option.<TestFilter> none(), Tests.class);

    final SlaveArguments sa = new SlaveArguments(System.getProperties(),
        coverOnlyTestees(), 8186, true);

    final FunctionalList<CoverageResult> coveredClasses = new MutableList<CoverageResult>();

    final SideEffect1<CoverageResult> handler = new SideEffect1<CoverageResult>() {

      public void apply(final CoverageResult a) {
        coveredClasses.add(a);
      }

    };

    final CoverageProcess process = new CoverageProcess(
        WrappingProcess.Args.withClassPath(new ClassPath()), sa, 8186, tus,
        handler);
    process.waitToDie();

    assertTrue(coveredClasses.contains(coverageFor(Testee2.class)));
    assertTrue(coveredClasses.contains(coverageFor(Testee.class)));

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
