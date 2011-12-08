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

import java.util.ArrayList;
import java.util.List;

import org.pitest.ConcreteConfiguration;
import org.pitest.DefaultStaticConfig;
import org.pitest.Pitest;
import org.pitest.containers.UnContainer;
import org.pitest.coverage.CoverageReceiver;
import org.pitest.extension.Container;
import org.pitest.extension.TestUnit;
import org.pitest.mutationtest.CheckTestHasFailedResultListener;

public class CoverageWorker implements Runnable {

  private final CoveragePipe   pipe;
  private final List<TestUnit> tests;

  public CoverageWorker(final CoveragePipe pipe, final List<TestUnit> tests) {
    this.pipe = pipe;
    this.tests = tests;
  }

  public void run() {

    try {

      final List<TestUnit> decoratedTests = decorateForCoverage(this.tests,
          this.pipe);

      final Container c = new UnContainer();

      final CheckTestHasFailedResultListener listener = new CheckTestHasFailedResultListener();

      final ConcreteConfiguration conf = new ConcreteConfiguration();

      final DefaultStaticConfig staticConfig = new DefaultStaticConfig();
      staticConfig.addTestListener(listener);
      staticConfig.addTestListener(new ErrorListener());

      final Pitest pit = new Pitest(staticConfig, conf);
      pit.run(c, decoratedTests);

    } catch (final Exception ex) {
      throw translateCheckedException(ex);
    }

  }

  private List<TestUnit> decorateForCoverage(final List<TestUnit> plainTests,
      final CoverageReceiver queue) {
    final List<TestUnit> decorated = new ArrayList<TestUnit>(plainTests.size());
    for (final TestUnit each : plainTests) {
      decorated.add(new CoverageDecorator(queue, each));
    }
    return decorated;
  }
}
