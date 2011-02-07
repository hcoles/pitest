package org.pitest.coverage.execute;

import static org.pitest.functional.Prelude.print;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.pitest.Pitest;
import org.pitest.extension.TestFilter;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.NullDiscoveryListener;
import org.pitest.extension.common.UnGroupedStrategy;
import org.pitest.functional.Option;
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
      final Testee2 testee = new Testee2();
      testee.foo();
    }
  }

  @Test
  public void testStuff() throws IOException, InterruptedException {

    final List<TestUnit> tus = Pitest.findTestUnitsForAllSuppliedClasses(
        new JUnitCompatibleConfiguration(), new NullDiscoveryListener(),
        new UnGroupedStrategy(), Option.<TestFilter> none(), Tests.class);

    final SlaveArguments sa = new SlaveArguments(tus, System.getProperties());

    final CoverageProcess process = new CoverageProcess(
        WrappingProcess.Args.withClassPath(new ClassPath()), sa);
    process.waitToDie();

    process.results().forEach(print(CoverageResult.class));

  }

}
