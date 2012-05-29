/*
 * Copyright 2012 Henry Coles
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

package org.pitest.coverage;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.pitest.Description;
import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassName;
import org.pitest.classinfo.CodeSource;
import org.pitest.coverage.domain.TestInfo;
import org.pitest.coverage.execute.CoverageResult;
import org.pitest.functional.F2;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.mutationtest.instrument.ClassLine;
import org.pitest.util.Log;
import org.pitest.util.MemoryEfficientHashMap;

public class CoverageData implements CoverageDatabase {

  private final static Logger                              LOG           = Log
                                                                             .getLogger();

  private final Map<Description, Long>                     times         = new MemoryEfficientHashMap<Description, Long>();
  private final Map<String, Map<ClassLine, Set<TestInfo>>> classCoverage = new MemoryEfficientHashMap<String, Map<ClassLine, Set<TestInfo>>>();
  private final CodeSource                                 code;

  private boolean                                          hasFailedTest = false;

  CoverageData(final CodeSource code) {
    this.code = code;
  }

  public Collection<TestInfo> getTestsForClassLine(final ClassLine classLine) {
    final Collection<TestInfo> result = getTestsForJVMClassName(
        classLine.getJVMClassName()).get(classLine);
    if (result == null) {
      return Collections.emptyList();
    } else {
      return result;
    }
  }

  public boolean allTestsGreen() {
    return !this.hasFailedTest;
  }

  public Collection<ClassInfo> getClassInfo(final Collection<String> classes) {
    return this.code.getClassInfo(classes);
  }

  public int getNumberOfCoveredLines(final Collection<String> mutatedClass) {
    return FCollection.fold(numberCoveredLines(), 0, mutatedClass);
  }

  public Collection<TestInfo> getTestsForClass(final String clazz) {
    final Map<ClassLine, Set<TestInfo>> map = getTestsForJVMClassName(clazz);

    final Set<TestInfo> tis = new HashSet<TestInfo>();
    for (final Set<TestInfo> each : map.values()) {
      tis.addAll(each);
    }
    return tis;

  }

  void calculateClassCoverage(final CoverageResult cr) {

    checkForFailedTest(cr);

    this.recordExecutionTime(cr.getTestUnitDescription(), cr.getExecutionTime());

    for (final ClassStatistics i : cr.getCoverage()) {
      Map<ClassLine, Set<TestInfo>> map = this.classCoverage.get(i
          .getClassName());
      if (map == null) {
        map = new MemoryEfficientHashMap<ClassLine, Set<TestInfo>>();
        this.classCoverage.put(i.getClassName(), map);
      }
      mapTestsToClassLines(cr.getTestUnitDescription(), i, map);
    }
  }

  private void checkForFailedTest(final CoverageResult cr) {
    if (!cr.isGreenTest()) {
      recordTestFailure();
      LOG.warning(cr.getTestUnitDescription()
          + " did not pass without mutation.");
    }
  }

  private void mapTestsToClassLines(final Description description,
      final ClassStatistics i, final Map<ClassLine, Set<TestInfo>> map) {

    for (final int line : i.getUniqueVisitedLines()) {
      final ClassLine key = new ClassLine(i.getClassName(), line);
      Set<TestInfo> testsForLine = map.get(key);
      if (testsForLine == null) {
        testsForLine = new TreeSet<TestInfo>(new TestInfoNameComparator()); // inject
        // comparator
        // here
        map.put(key, testsForLine);
      }
      testsForLine.add(this.descriptionToTestInfo(description));

    }
  }

  private TestInfo descriptionToTestInfo(final Description description) {
    final int time = this.times.get(description).intValue();

    final Option<ClassName> testee = this.code.findTestee(description
        .getFirstTestClass());

    return new TestInfo(description.getFirstTestClass(),
        description.getQualifiedName(), time, testee);

  }

  private F2<Integer, String, Integer> numberCoveredLines() {
    return new F2<Integer, String, Integer>() {

      public Integer apply(final Integer a, final String clazz) {
        return a + getNumberOfCoveredLines(clazz);
      }

    };
  }

  private int getNumberOfCoveredLines(final String clazz) {
    final Map<ClassLine, Set<TestInfo>> map = this.classCoverage.get(clazz
        .replace(".", "/"));
    if (map != null) {
      return map.size();
    } else {
      return 0;
    }

  }

  private Map<ClassLine, Set<TestInfo>> getTestsForJVMClassName(
      final String clazz) {
    // Use any test that provided some coverage of the class
    // This fails to consider tests that only accessed a static variable
    // of the class in question as this does not register as coverage.

    Map<ClassLine, Set<TestInfo>> map = this.classCoverage.get(clazz);
    if (map == null) {
      map = new MemoryEfficientHashMap<ClassLine, Set<TestInfo>>();
    }
    return map;
  }

  private void recordExecutionTime(final Description testUnitDescription,
      final long executionTime) {
    this.times.put(testUnitDescription, executionTime);
  }

  private void recordTestFailure() {
    this.hasFailedTest = true;
  }

}
