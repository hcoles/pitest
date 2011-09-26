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

import java.util.logging.Logger;

import org.pitest.coverage.CoverageReceiver;
import org.pitest.extension.ResultCollector;
import org.pitest.extension.TestFilter;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.TestUnitDecorator;
import org.pitest.functional.Option;
import org.pitest.mutationtest.ExitingResultCollector;
import org.pitest.util.Log;

public class CoverageDecorator extends TestUnitDecorator {

  private final static Logger    LOG = Log.getLogger();

  private final CoverageReceiver invokeQueue;
  private final int              index;

  protected CoverageDecorator(final CoverageReceiver queue,
      final TestUnit child, final int index) {
    super(child);
    this.invokeQueue = queue;
    this.index = index;

  }

  @Override
  public void execute(final ClassLoader loader, final ResultCollector rc) {
    LOG.fine("Gathering coverage for test " + child().getDescription());
    this.invokeQueue.recordTest(this.index);

    final long t0 = System.currentTimeMillis();
    final ExitingResultCollector wrappedCollector = new ExitingResultCollector(
        rc);
    this.child().execute(loader, wrappedCollector);
    final long executionTime = System.currentTimeMillis() - t0;
    this.invokeQueue.recordTestOutcome(!wrappedCollector.shouldExit(),
        executionTime);

  }

  public Option<TestUnit> filter(final TestFilter filter) {
    final Option<TestUnit> modifiedChild = this.child().filter(filter);
    if (modifiedChild.hasSome()) {
      return Option.<TestUnit> some(new CoverageDecorator(this.invokeQueue,
          modifiedChild.value(), this.index));
    } else {
      return Option.none();
    }

  }

}
