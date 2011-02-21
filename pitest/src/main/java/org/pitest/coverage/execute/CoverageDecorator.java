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

import org.pitest.coverage.CoverageStatistics;
import org.pitest.coverage.InvokeQueue;
import org.pitest.extension.ResultCollector;
import org.pitest.extension.TestFilter;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.TestUnitDecorator;
import org.pitest.functional.Option;
import org.pitest.functional.SideEffect1;
import org.pitest.mutationtest.ExitingResultCollector;

public class CoverageDecorator extends TestUnitDecorator {

  private final CoverageStatistics          invokeStatistics;
  private final InvokeQueue                 invokeQueue;
  private final SideEffect1<CoverageResult> output;

  protected CoverageDecorator(final InvokeQueue queue,
      final CoverageStatistics invokeStatistics, final TestUnit child,
      final SideEffect1<CoverageResult> output) {
    super(child);
    this.invokeStatistics = invokeStatistics;
    this.invokeQueue = queue;
    this.output = output;

  }

  @Override
  public void execute(final ClassLoader loader, final ResultCollector rc) {
    System.out.println("Gathering coverage for test "
        + child().getDescription());
    this.invokeStatistics.clearCoverageStats();

    final CoverageReaderThread t = new CoverageReaderThread(this.invokeQueue,
        this.invokeStatistics);
    t.start();

    final long t0 = System.currentTimeMillis();
    final ExitingResultCollector wrappedCollector = new ExitingResultCollector(
        rc);
    this.child().execute(loader, wrappedCollector);
    final long executionTime = System.currentTimeMillis() - t0;

    try {
      t.waitToFinish();
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }

    // readStatisticsQueue();

    this.output.apply(new CoverageResult(this.getDescription(), executionTime,
        !wrappedCollector.shouldExit(), this.invokeStatistics
            .getClassStatistics()));

  }

  public Option<TestUnit> filter(final TestFilter filter) {
    final Option<TestUnit> modifiedChild = this.child().filter(filter);
    if (modifiedChild.hasSome()) {
      return Option.<TestUnit> some(new CoverageDecorator(this.invokeQueue,
          this.invokeStatistics, modifiedChild.value(), this.output));
    } else {
      return Option.none();
    }

  }

}
