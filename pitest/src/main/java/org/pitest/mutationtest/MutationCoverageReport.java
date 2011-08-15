/*
 * Copyright 2010 Henry Coles
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
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.mutationtest;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.pitest.ConcreteConfiguration;
import org.pitest.DefaultStaticConfig;
import org.pitest.ExtendedTestResult;
import org.pitest.Pitest;
import org.pitest.TestResult;
import org.pitest.containers.BaseThreadPoolContainer;
import org.pitest.containers.UnContainer;
import org.pitest.extension.ClassLoaderFactory;
import org.pitest.extension.Container;
import org.pitest.extension.TestListener;
import org.pitest.extension.TestUnit;
import org.pitest.functional.FCollection;
import org.pitest.functional.Prelude;
import org.pitest.functional.SideEffect1;
import org.pitest.help.Help;
import org.pitest.help.PitHelpError;
import org.pitest.internal.ClassPath;
import org.pitest.internal.IsolationUtils;
import org.pitest.junit.JUnitCompatibleConfiguration;
import org.pitest.mutationtest.commandline.OptionsParser;
import org.pitest.mutationtest.commandline.ParseResult;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.filter.LimitNumberOfMutationPerClassFilter;
import org.pitest.mutationtest.filter.MutationFilterFactory;
import org.pitest.mutationtest.filter.UnfilteredMutationFilter;
import org.pitest.mutationtest.instrument.JarCreatingJarFinder;
import org.pitest.mutationtest.instrument.UnRunnableMutationTestMetaData;
import org.pitest.util.JavaAgent;
import org.pitest.util.Log;
import org.pitest.util.TestInfo;
import org.pitest.util.Unchecked;

public class MutationCoverageReport implements Runnable {

  private static final Logger     LOG = Log.getLogger();
  protected final ReportOptions   data;
  protected final ListenerFactory listenerFactory;
  protected final JavaAgent       javaAgentFinder;
  protected final boolean         nonLocalClassPath;

  public MutationCoverageReport(final ReportOptions data,
      final JavaAgent javaAgentFinder, final ListenerFactory listenerFactory,
      final boolean nonLocalClassPath) {
    this.javaAgentFinder = javaAgentFinder;
    this.nonLocalClassPath = nonLocalClassPath;
    this.listenerFactory = listenerFactory;
    this.data = data;
  }

  public final void run() {
    try {
      this.runReport();

    } catch (final IOException ex) {
      throw Unchecked.translateCheckedException(ex);
    }
  }

  public static void main(final String args[]) {

    final OptionsParser parser = new OptionsParser();
    final ParseResult pr = parser.parse(args);

    if (!pr.isOk()) {
      parser.printHelp();
      System.out.println(">>>> " + pr.getErrorMessage().value());
    } else {
      final ReportOptions data = pr.getOptions();
      setClassesInScopeToEqualTargetClassesIfNoValueSupplied(data);
      runReport(data);
    }

  }

  private static void runReport(final ReportOptions data) {
    final JarCreatingJarFinder agent = new JarCreatingJarFinder();
    try {
      final MutationCoverageReport instance = new MutationCoverageReport(data,
          agent, new HtmlReportFactory(), false);

      instance.run();
    } finally {
      agent.close();
    }
  }

  private static void setClassesInScopeToEqualTargetClassesIfNoValueSupplied(
      final ReportOptions data) {
    if (!data.hasValueForClassesInScope()) {
      data.setClassesInScope(data.getTargetClasses());
    }
  }

  protected void reportFailureForClassesWithoutTests(
      final Collection<String> classesWithOutATest,
      final TestListener mutationReportListener) {
    final SideEffect1<String> reportFailure = new SideEffect1<String>() {
      public void apply(final String a) {
        final TestResult tr = new ExtendedTestResult(null, null,
            new UnRunnableMutationTestMetaData("Could not find any tests for "
                + a));
        mutationReportListener.onTestFailure(tr);
      }

    };
    FCollection.forEach(classesWithOutATest, reportFailure);
  }

  protected ClassPath getClassPath() {
    return this.data.getClassPath(this.nonLocalClassPath).getOrElse(
        new ClassPath());
  }

  private void runReport() throws IOException {

    TestInfo.checkJUnitVersion();

    Log.setVerbose(this.data.isVerbose());

    LOG.fine("System class path is " + System.getProperty("java.class.path"));

    final long t0 = System.currentTimeMillis();

    final ConcreteConfiguration initialConfig = new ConcreteConfiguration(
        new JUnitCompatibleConfiguration());

    final CoverageDatabase coverageDatabase = new DefaultCoverageDatabase(
        initialConfig, this.getClassPath(), this.javaAgentFinder, this.data);

    if (!coverageDatabase.initialise()) {
      throw new PitHelpError(Help.FAILING_TESTS);
    }

    final Collection<ClassGrouping> codeClasses = coverageDatabase
        .getGroupedClasses();

    final DefaultStaticConfig staticConfig = new DefaultStaticConfig();
    final TestListener mutationReportListener = this.listenerFactory
        .getListener(coverageDatabase, this.data, t0);

    staticConfig.addTestListener(mutationReportListener);
    // staticConfig.addTestListener(ConsoleTestListener.);

    reportFailureForClassesWithoutTests(
        coverageDatabase.getParentClassesWithoutATest(), mutationReportListener);

    final MutationEngine engine = DefaultMutationConfigFactory.createEngine(
        this.data.isMutateStaticInitializers(),
        Prelude.or(this.data.getExcludedMethods()),
        this.data.getLoggingClasses(),
        this.data.getMutators().toArray(
            new Mutator[this.data.getMutators().size()]));

    final MutationConfig mutationConfig = new MutationConfig(engine,
        this.data.getJvmArgs());
    final MutationTestBuilder builder = new MutationTestBuilder(mutationConfig,
        limitMutationsPerClass(), new JUnitCompatibleConfiguration(),
        this.data, this.javaAgentFinder);

    final List<TestUnit> tus = builder.createMutationTestUnits(codeClasses,
        initialConfig, coverageDatabase);

    LOG.info("Created  " + tus.size() + " mutation test units");
    checkMutationsFounds(tus);

    final Pitest pit = new Pitest(staticConfig, initialConfig);
    pit.run(createContainer(), tus);

    LOG.info("Completed in " + timeSpan(t0) + ".  Tested " + codeClasses.size()
        + " classes.");

  }

  private void checkMutationsFounds(final List<TestUnit> tus) {
    if (tus.isEmpty()) {
      throw new PitHelpError(Help.NO_MUTATIONS_FOUND);
    }
  }

  private MutationFilterFactory limitMutationsPerClass() {
    if (this.data.getMaxMutationsPerClass() <= 0) {
      return UnfilteredMutationFilter.factory();
    } else {
      return LimitNumberOfMutationPerClassFilter.factory(this.data
          .getMaxMutationsPerClass());
    }

  }

  private Container createContainer() {
    if (this.data.getNumberOfThreads() > 1) {
      return new BaseThreadPoolContainer(this.data.getNumberOfThreads(),
          classLoaderFactory(), Executors.defaultThreadFactory()) {

      };
    } else {
      return new UnContainer();
    }
  }

  private ClassLoaderFactory classLoaderFactory() {
    final ClassLoader loader = IsolationUtils.getContextClassLoader();
    return new ClassLoaderFactory() {

      public ClassLoader get() {
        return loader;
      }

    };
  }

  private String timeSpan(final long t0) {
    return "" + ((System.currentTimeMillis() - t0) / 1000) + " seconds";
  }

}
