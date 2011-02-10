package org.pitest.mutationtest;

import static org.pitest.functional.Prelude.printWith;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pitest.ConcreteConfiguration;
import org.pitest.Pitest;
import org.pitest.coverage.execute.CoverageProcess;
import org.pitest.coverage.execute.CoverageResult;
import org.pitest.coverage.execute.SlaveArguments;
import org.pitest.extension.TestFilter;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.NullDiscoveryListener;
import org.pitest.extension.common.UnGroupedStrategy;
import org.pitest.functional.FunctionalCollection;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.Option;
import org.pitest.functional.predicate.Predicate;
import org.pitest.internal.ClassPath;
import org.pitest.junit.JUnitCompatibleConfiguration;
import org.pitest.util.Functions;
import org.pitest.util.JavaAgent;
import org.pitest.util.WrappingProcess;

public class DefaultCoverageDatabase implements CoverageDatabase {

  private final JavaAgent     javaAgentFinder;
  private final ClassPath     classPath;
  private final ReportOptions data;

  public DefaultCoverageDatabase(final ClassPath classPath,
      final JavaAgent javaAgentFinder, final ReportOptions data) {
    this.classPath = classPath;
    this.data = data;
    this.javaAgentFinder = javaAgentFinder;
  }

  public Map<ClassGrouping, List<String>> mapCodeToTests(
      final FunctionalCollection<Class<?>> tests,
      final Map<String, ClassGrouping> groupedByOuterClass) throws IOException {

    try {
      final FunctionalList<CoverageResult> coverage = gatherCoverageData(tests);

      final List<String> testsAsStrings = tests.map(Functions.classToName());

      // don't actually need to map code to tests
      // as long as we pass in the coverage data
      // can map each test to the entire list
      // and it should never run the uncovered ones
      final Map<ClassGrouping, List<String>> codeToTests = new HashMap<ClassGrouping, List<String>>();
      for (final ClassGrouping each : groupedByOuterClass.values()) {
        codeToTests.put(each, testsAsStrings);
      }

    } catch (final InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // TODO Auto-generated method stub
    return null;
  }

  private FunctionalList<CoverageResult> gatherCoverageData(
      final Collection<Class<?>> tests) throws IOException,
      InterruptedException {
    final ConcreteConfiguration initialConfig = new ConcreteConfiguration(
        new JUnitCompatibleConfiguration());

    final List<TestUnit> tus = Pitest.findTestUnitsForAllSuppliedClasses(
        initialConfig, new NullDiscoveryListener(), new UnGroupedStrategy(),
        Option.<TestFilter> none(), tests.toArray(new Class<?>[tests.size()]));

    final SlaveArguments sa = new SlaveArguments(tus, System.getProperties(),
        convertToJVMClassFilter(this.data.getTargetClassesFilter()));
    final CoverageProcess process = new CoverageProcess(WrappingProcess.Args
        .withClassPath(this.classPath).andJVMArgs(this.data.getJvmArgs())
        .andJavaAgentFinder(this.javaAgentFinder)
        .andStderr(printWith("SLAVE : ")), sa);
    process.waitToDie();
    return process.results();

  }

  private Predicate<String> convertToJVMClassFilter(
      final Predicate<String> child) {
    return new Predicate<String>() {
      public Boolean apply(final String a) {
        return child.apply(a.replace("/", "."));
      }

    };
  }

}
