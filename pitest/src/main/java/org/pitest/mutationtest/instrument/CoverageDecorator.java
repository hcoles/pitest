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
package org.pitest.mutationtest.instrument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.pitest.coverage.ClassStatistics;
import org.pitest.coverage.CoverageStatistics;
import org.pitest.coverage.InvokeEntry;
import org.pitest.coverage.InvokeQueue;
import org.pitest.extension.ResultCollector;
import org.pitest.extension.TestFilter;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.TestUnitDecorator;
import org.pitest.functional.Option;
import org.pitest.mutationtest.instrument.Statistics.ClassLine;

public class CoverageDecorator extends TestUnitDecorator {

  private final CoverageStatistics             invokeStatistics;
  private final InvokeQueue                    invokeQueue;
  private final Map<ClassLine, List<TestUnit>> lineMapping;
  private long                                 executionTime = 0;
  private final Collection<String>             classesForCoverage;

  protected CoverageDecorator(final Collection<String> classForCoverage,
      final InvokeQueue queue, final CoverageStatistics invokeStatistics,
      final Map<ClassLine, List<TestUnit>> lineMapping, final TestUnit child) {
    super(child);
    this.invokeStatistics = invokeStatistics;
    this.invokeQueue = queue;
    this.lineMapping = lineMapping;
    this.classesForCoverage = classForCoverage;
  }

  @Override
  public void execute(final ClassLoader loader, final ResultCollector rc) {
    System.out.println("Gathering stats for test " + child().getDescription());
    this.invokeStatistics.clearCoverageStats();

    final long t0 = System.currentTimeMillis();
    this.child().execute(loader, rc);
    this.executionTime = System.currentTimeMillis() - t0;

    readStatisticsQueue(this.invokeStatistics, this.invokeQueue);

    final Collection<ClassStatistics> css = this.invokeStatistics
        .getClassStatistics(this.classesForCoverage);

    for (final ClassStatistics cs : css) {

      for (final Integer line : cs.getUniqueVisitedLines()) {
        final ClassLine cl = new ClassLine(cs.getClassName(), line);
        List<TestUnit> coveringTests = this.lineMapping.get(cl);
        if (coveringTests == null) {
          coveringTests = new ArrayList<TestUnit>();
        }
        coveringTests.add(this.child());

        this.lineMapping.put(cl, coveringTests);

      }
    }

  }

  public Option<TestUnit> filter(final TestFilter filter) {
    final Option<TestUnit> modifiedChild = this.child().filter(filter);
    if (modifiedChild.hasSome()) {
      return Option.<TestUnit> some(new CoverageDecorator(
          this.classesForCoverage, this.invokeQueue, this.invokeStatistics,
          this.lineMapping, modifiedChild.value()));
    } else {
      return Option.none();
    }

  }

  private void readStatisticsQueue(final CoverageStatistics invokeStatistics,
      final InvokeQueue invokeQueue) {
    while (!invokeQueue.isEmpty()) {
      final InvokeEntry entry = invokeQueue.poll();
      invokeStatistics.visitLine(entry.getClassId(), entry.getLineNumber());

    }

  }

  public long getExecutionTime() {
    return this.executionTime;
  }

}
