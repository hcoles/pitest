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

import static org.pitest.functional.FCollection.flatMap;
import static org.pitest.functional.FCollection.map;
import static org.pitest.functional.Prelude.not;
import static org.pitest.util.Functions.stringToClass;
import static org.pitest.util.TestInfo.isATest;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.pitest.ConcreteConfiguration;
import org.pitest.DefaultStaticConfig;
import org.pitest.Pitest;
import org.pitest.containers.UnContainer;
import org.pitest.extension.Container;
import org.pitest.extension.TestListener;
import org.pitest.extension.common.ConsoleResultListener;
import org.pitest.functional.FCollection;
import org.pitest.internal.ClassPath;
import org.pitest.junit.JUnitCompatibleConfiguration;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.report.MutationTestSummaryData.MutationTestType;
import org.pitest.util.Functions;
import org.pitest.util.JavaAgent;
import org.pitest.util.Log;

public class TestCentricReport extends MutationCoverageReport {
  private final static Logger LOG = Log.getLogger();

  public TestCentricReport(final ReportOptions data,
      final JavaAgent javaAgentJarFinder,
      final ListenerFactory listenerFactory, final boolean nonLocalClassPath) {
    super(data, javaAgentJarFinder, listenerFactory, nonLocalClassPath);
  }

  @Override
  public void runReport() {

    final long t0 = System.currentTimeMillis();
    final Collection<Class<?>> targets = findClassesForCoverage(getClassPath()
        .getLocalDirectoryComponent());
    LOG.info("Found " + targets.size() + " targets");
    final Collection<Class<?>> tests = FCollection.filter(targets, isATest());

    final Collection<Class<?>> classesWithATest = extractTesteesFromTests(tests);

    final Collection<Class<?>> classesWithoutATest = FCollection.filter(
        targets, not(isATest()));
    classesWithoutATest.removeAll(classesWithATest);

    LOG.warning("Found " + tests.size() + " tests");
    LOG.warning("Matched " + classesWithATest.size() + " classes to a test");

    final DefaultStaticConfig staticConfig = new DefaultStaticConfig();
    final TestListener mutationReportListener = this.listenerFactory
        .getListener(this.data, t0);

    staticConfig.addTestListener(mutationReportListener);
    staticConfig.addTestListener(new ConsoleResultListener());

    reportFailureForClassesWithoutTests(
        map(classesWithoutATest, Functions.classToName()),
        mutationReportListener);

    final ConcreteConfiguration initialConfig = new ConcreteConfiguration(
        new JUnitCompatibleConfiguration());

    final MutationEngine engine = DefaultMutationConfigFactory.createEngine(
        this.data.isMutateStaticInitializers(),
        this.data.getLoggingClasses(),
        this.data.getMutators().toArray(
            new Mutator[this.data.getMutators().size()]));
    final MutationConfig mutationConfig = new MutationConfig(engine,
        MutationTestType.TEST_CENTRIC, 0, Collections.<String> emptyList());

    initialConfig.setMutationTestFinder(new MutationTestFinder(mutationConfig,
        new FindInnerAndMemberClassesStrategy(), this.javaAgentFinder));

    final Pitest pit = new Pitest(staticConfig, initialConfig);
    final Container c = new UnContainer();
    pit.run(c, tests);

    LOG.info("All Done");

  }

  protected Collection<Class<?>> findClassesForCoverage(final ClassPath cp) {
    final Collection<Class<?>> classes = flatMap(
        cp.findClasses(this.data.getTargetClassesFilter()), stringToClass());
    final Set<Class<?>> set = new HashSet<Class<?>>(classes.size());
    set.addAll(classes);
    return set;
  }
}
