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

import org.pitest.coverage.ClassStatistics;
import org.pitest.extension.TestUnit;

public class Statistics {

  public final static class ClassLine {
    public final String clazz;
    public final int    lineNumber;

    public ClassLine(final String clazz, final int lineNumber) {
      this.clazz = clazz;
      this.lineNumber = lineNumber;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result
          + ((this.clazz == null) ? 0 : this.clazz.hashCode());
      result = prime * result + this.lineNumber;
      return result;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final ClassLine other = (ClassLine) obj;
      if (this.clazz == null) {
        if (other.clazz != null) {
          return false;
        }
      } else if (!this.clazz.equals(other.clazz)) {
        return false;
      }
      if (this.lineNumber != other.lineNumber) {
        return false;
      }
      return true;
    }

    @Override
    public String toString() {
      return "ClassLine [" + this.clazz + ":" + this.lineNumber + "]";
    }

  }

  private final Map<ClassLine, List<TestUnit>> lineNumberToCoveringTestsMap;

  private final Map<TestUnit, Long>            testUnitToExecutionTimeMap;
  private final Collection<ClassStatistics>    classStatistics;
  private final boolean                        testsRunGreen;

  public boolean hasCoverageData() {
    return this.lineNumberToCoveringTestsMap != null;
  }

  public Statistics(final boolean testRunGreen,
      final Map<TestUnit, Long> times,
      final Map<ClassLine, List<TestUnit>> stats,
      final Collection<ClassStatistics> classStatistics) {
    this.lineNumberToCoveringTestsMap = stats;
    this.testUnitToExecutionTimeMap = times;
    this.testsRunGreen = testRunGreen;
    this.classStatistics = classStatistics;
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

  public long getExecutionTime(final List<TestUnit> tus) {
    long t = 0;
    for (final TestUnit each : tus) {
      t = t + this.testUnitToExecutionTimeMap.get(each);
    }
    return t;
  }

  public boolean isGreenSuite() {
    return this.testsRunGreen;
  }

  public Collection<TestUnit> getAllTests() {
    return this.testUnitToExecutionTimeMap.keySet();
  }

  public Collection<ClassStatistics> getClassStatistics() {
    return this.classStatistics;
  }

  // public Boolean isCodeLine(final ClassLine classLine) {
  //
  // return FCollection.contains(this.classStatistics, isACodeLine(classLine));
  //
  // }
  //
  // private F<ClassStatistics, Boolean> isACodeLine(final ClassLine classLine)
  // {
  // return new F<ClassStatistics, Boolean>() {
  // public Boolean apply(final ClassStatistics a) {
  // return a.getClassName().equals(classLine.clazz)
  // && a.isCodeLine(classLine.lineNumber);
  // }
  //
  // };
  // }

  public int getNumberOfLinesWithCoverage() {
    return this.lineNumberToCoveringTestsMap.size();
  }

  // public int getNumberOfCodeLines() {
  // final F2<Integer, ClassStatistics, Integer> f = new F2<Integer,
  // ClassStatistics, Integer>() {
  // public Integer apply(final Integer a, final ClassStatistics b) {
  // return a + b.getCodeLines().size();
  // }
  // };
  //
  // return FCollection.fold(f, 0, this.classStatistics);
  // }

}
