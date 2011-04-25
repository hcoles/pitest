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
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.pitest.ConcreteConfiguration;
import org.pitest.DefaultStaticConfig;
import org.pitest.PitError;
import org.pitest.Pitest;
import org.pitest.containers.BaseThreadPoolContainer;
import org.pitest.containers.UnContainer;
import org.pitest.extension.ClassLoaderFactory;
import org.pitest.extension.Container;
import org.pitest.extension.TestListener;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.ConsoleResultListener;
import org.pitest.extension.common.SuppressMutationTestFinding;
import org.pitest.junit.JUnitCompatibleConfiguration;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.report.MutationTestSummaryData.MutationTestType;
import org.pitest.util.JavaAgent;
import org.pitest.util.Log;

public class CodeCentricReport extends MutationCoverageReport {

  private final static Logger LOG = Log.getLogger();

  public CodeCentricReport(final ReportOptions data,
      final JavaAgent javaAgentFinder, final ListenerFactory listenerFactory,
      final boolean nonLocalClassPath) {
    super(data, javaAgentFinder, listenerFactory, nonLocalClassPath);
  }

  @Override
  public void runReport() throws IOException {

    final long t0 = System.currentTimeMillis();

    final ConcreteConfiguration initialConfig = new ConcreteConfiguration(
        new JUnitCompatibleConfiguration());
    initialConfig.setMutationTestFinder(new SuppressMutationTestFinding());
    final CoverageDatabase coverageDatabase = new DefaultCoverageDatabase(
        initialConfig, this.getClassPath(), this.javaAgentFinder, this.data);

    if (!coverageDatabase.initialise()) {
      throw new PitError(
          "All tests did not pass without mutation when calculating coverage.");

    }

    final Map<ClassGrouping, List<String>> codeToTests = coverageDatabase
        .mapCodeToTests();

    final DefaultStaticConfig staticConfig = new DefaultStaticConfig();
    final TestListener mutationReportListener = this.listenerFactory
        .getListener(coverageDatabase, this.data, t0);

    staticConfig.addTestListener(mutationReportListener);
    staticConfig.addTestListener(new ConsoleResultListener());

    reportFailureForClassesWithoutTests(
        coverageDatabase.getParentClassesWithoutATest(), mutationReportListener);

    final MutationEngine engine = DefaultMutationConfigFactory.createEngine(
        this.data.isMutateStaticInitializers(),
        this.data.getLoggingClasses(),
        this.data.getMutators().toArray(
            new Mutator[this.data.getMutators().size()]));

    final MutationConfig mutationConfig = new MutationConfig(engine,
        MutationTestType.CODE_CENTRIC, 0, this.data.getJvmArgs());
    final MutationTestBuilder builder = new MutationTestBuilder(mutationConfig,
        new JUnitCompatibleConfiguration(), this.data, this.javaAgentFinder);

    final List<TestUnit> tus = builder.createMutationTestUnits(codeToTests,
        initialConfig, coverageDatabase);

    LOG.info("Created  " + tus.size() + " mutation test units");

    final Pitest pit = new Pitest(staticConfig, initialConfig);
    pit.run(createContainer(), tus);

    LOG.info("Completed in " + timeSpan(t0) + ".  Tested " + codeToTests.size()
        + " classes.");

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
    final ClassLoader loader = Thread.currentThread().getContextClassLoader();
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
