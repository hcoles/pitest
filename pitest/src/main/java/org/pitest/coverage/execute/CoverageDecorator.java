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

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.logging.Logger;

import org.pitest.coverage.CoverageReceiver;
import org.pitest.extension.common.TestUnitDecorator;
import org.pitest.testapi.ResultCollector;
import org.pitest.testapi.TestUnit;
import org.pitest.testapi.execute.ExitingResultCollector;
import org.pitest.util.Log;

public class CoverageDecorator extends TestUnitDecorator {

  private static final Logger    LOG     = Log.getLogger();

  private final CoverageReceiver invokeQueue;
  private final ThreadMXBean     threads = ManagementFactory.getThreadMXBean();

  protected CoverageDecorator(final CoverageReceiver queue, final TestUnit child) {
    super(child);
    this.invokeQueue = queue;
  }

  @Override
  public void execute(final ResultCollector rc) {
    LOG.fine("Gathering coverage for test " + child().getDescription());
    this.invokeQueue.newTest();

    final int threadsBeforeTest = this.threads.getThreadCount();

    final long t0 = System.currentTimeMillis();
    final ExitingResultCollector wrappedCollector = new ExitingResultCollector(
        rc);
    this.child().execute(wrappedCollector);

    final int executionTime = (int) (System.currentTimeMillis() - t0);

    final int threadsAfterTest = this.threads.getThreadCount();
    if (threadsAfterTest > threadsBeforeTest) {
      LOG.warning("More threads at end of test (" + threadsAfterTest + ") "
          + child().getDescription().getName() + " than start. ("
          + threadsBeforeTest + ")");
    }

    this.invokeQueue.recordTestOutcome(child().getDescription(),
        !wrappedCollector.shouldExit(), executionTime);

  }

}
