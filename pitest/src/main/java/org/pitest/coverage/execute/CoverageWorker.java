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
package org.pitest.coverage.execute;

import static org.pitest.util.Unchecked.translateCheckedException;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.pitest.ConcreteConfiguration;
import org.pitest.DefaultStaticConfig;
import org.pitest.Pitest;
import org.pitest.containers.UnContainer;
import org.pitest.coverage.CodeCoverageStore;
import org.pitest.coverage.CoverageStatistics;
import org.pitest.coverage.InvokeQueue;
import org.pitest.extension.Container;
import org.pitest.extension.TestUnit;
import org.pitest.functional.SideEffect1;
import org.pitest.mutationtest.CheckTestHasFailedResultListener;

public class CoverageWorker implements Runnable {

  private final SlaveArguments params;
  private final Writer         output;

  public CoverageWorker(final SlaveArguments paramsFromParent, final Writer w) {
    this.params = paramsFromParent;
    this.output = w;
  }

  public void run() {

    final CoverageStatistics invokeStatistics = new CoverageStatistics();
    final InvokeQueue invokeQueue = new InvokeQueue();
    CodeCoverageStore.init(invokeQueue, invokeStatistics);

    final OutputToFile outputWriter = createOutput(this.output);
    final List<TestUnit> decoratedTests = decorateForCoverage(
        this.params.getTests(), invokeStatistics, invokeQueue, outputWriter);

    final Container c = new UnContainer();

    try {
      final CheckTestHasFailedResultListener listener = new CheckTestHasFailedResultListener();

      final ConcreteConfiguration conf = new ConcreteConfiguration();

      final DefaultStaticConfig staticConfig = new DefaultStaticConfig();
      staticConfig.addTestListener(listener);
      staticConfig.addTestListener(new ErrorListener());

      final Pitest pit = new Pitest(staticConfig, conf);
      pit.run(c, decoratedTests);
      outputWriter.writeToDisk();

    } catch (final Exception ex) {
      throw translateCheckedException(ex);
    }

  }

  private OutputToFile createOutput(final Writer w) {
    return new OutputToFile(w);
  }

  private List<TestUnit> decorateForCoverage(final List<TestUnit> plainTests,
      final CoverageStatistics stats, final InvokeQueue queue,
      final SideEffect1<CoverageResult> output) {
    final List<TestUnit> decorated = new ArrayList<TestUnit>(plainTests.size());
    for (final TestUnit each : plainTests) {
      decorated.add(new CoverageDecorator(queue, stats, each, output));
    }
    return decorated;
  }
}
