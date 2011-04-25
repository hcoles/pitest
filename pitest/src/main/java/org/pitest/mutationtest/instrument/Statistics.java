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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.pitest.extension.TestUnit;

public class Statistics {

  private final Map<ClassLine, List<TestUnit>> lineNumberToCoveringTestsMap;

  private final Map<TestUnit, Long>            testUnitToExecutionTimeMap;
  private final boolean                        testsRunGreen;

  public Statistics(final boolean testRunGreen,
      final Map<TestUnit, Long> times,
      final Map<ClassLine, List<TestUnit>> stats) {
    this.lineNumberToCoveringTestsMap = stats;
    this.testUnitToExecutionTimeMap = times;
    this.testsRunGreen = testRunGreen;
    orderTestLists();
  }

  private void orderTestLists() {
    if (this.lineNumberToCoveringTestsMap != null) {
      final Comparator<TestUnit> comp = new Comparator<TestUnit>() {
        public int compare(final TestUnit arg0, final TestUnit arg1) {
          final long t0 = Statistics.this.testUnitToExecutionTimeMap.get(arg0);
          final long t1 = Statistics.this.testUnitToExecutionTimeMap.get(arg1);
          if (t0 < t1) {
            return -1;
          }
          if (t0 > t1) {
            return 1;
          }
          return 0;
        }

      };
      for (final Entry<ClassLine, List<TestUnit>> each : this.lineNumberToCoveringTestsMap
          .entrySet()) {
        Collections.sort(each.getValue(), comp);
      }
    }
  }

  public List<TestUnit> getTestForLineNumber(final ClassLine line) {

    if (this.lineNumberToCoveringTestsMap.get(line) != null) {
      return this.lineNumberToCoveringTestsMap.get(line);
    } else {
      return Collections.emptyList();
    }
  }

  public long getExecutionTime(final TestUnit tu) {
    return this.testUnitToExecutionTimeMap.get(tu);
  }

  public boolean isGreenSuite() {
    return this.testsRunGreen;
  }

  public Collection<TestUnit> getAllTests() {
    return this.testUnitToExecutionTimeMap.keySet();
  }

  public int getNumberOfLinesWithCoverage() {
    return this.lineNumberToCoveringTestsMap.size();
  }

  @Override
  public String toString() {
    return "Statistics [lineNumberToCoveringTestsMap="
        + this.lineNumberToCoveringTestsMap + ", testUnitToExecutionTimeMap="
        + this.testUnitToExecutionTimeMap + ", testsRunGreen="
        + this.testsRunGreen + "]";
  }

}
