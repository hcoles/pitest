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

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.CodeSource;
import org.pitest.functional.F;
import org.pitest.functional.F2;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.testapi.Description;
import org.pitest.util.Log;

public class CoverageData implements CoverageDatabase {

  private final static Logger                                 LOG           = Log
                                                                                .getLogger();

  private final Map<ClassName, Map<ClassLine, Set<TestInfo>>> classCoverage = new LinkedHashMap<ClassName, Map<ClassLine, Set<TestInfo>>>();
  private final CodeSource                                    code;

  private boolean                                             hasFailedTest = false;

  public CoverageData(final CodeSource code) {
    this.code = code;
  }

  public Collection<TestInfo> getTestsForClassLine(final ClassLine classLine) {
    final Collection<TestInfo> result = getTestsForClassName(
        classLine.getClassName()).get(classLine);
    if (result == null) {
      return Collections.emptyList();
    } else {
      return result;
    }
  }

  public boolean allTestsGreen() {
    return !this.hasFailedTest;
  }

  public Collection<ClassInfo> getClassInfo(final Collection<ClassName> classes) {
    return this.code.getClassInfo(classes);
  }

  public int getNumberOfCoveredLines(final Collection<ClassName> mutatedClass) {
    return FCollection.fold(numberCoveredLines(), 0, mutatedClass);
  }

  public Collection<TestInfo> getTestsForClass(final ClassName clazz) {
    final Map<ClassLine, Set<TestInfo>> map = getTestsForClassName(clazz);

    final Set<TestInfo> tis = new LinkedHashSet<TestInfo>(map.values().size());
    for (final Set<TestInfo> each : map.values()) {
      tis.addAll(each);
    }
    return tis;

  }

  public void calculateClassCoverage(final CoverageResult cr) {

    checkForFailedTest(cr);
    final TestInfo ti = this.createTestInfo(cr.getTestUnitDescription(),
        cr.getExecutionTime(), cr.getNumberOfCoveredLines());

    for (final ClassStatistics i : cr.getCoverage()) {
      final Map<ClassLine, Set<TestInfo>> map = getCoverageMapForClass(i
          .getClassName());
      mapTestsToClassLines(ti, i, map);
    }
  }

  private void checkForFailedTest(final CoverageResult cr) {
    if (!cr.isGreenTest()) {
      recordTestFailure();
      LOG.warning(cr.getTestUnitDescription()
          + " did not pass without mutation.");
    }
  }

  private void mapTestsToClassLines(final TestInfo test,
      final ClassStatistics stats, final Map<ClassLine, Set<TestInfo>> map) {

    for (final int line : stats.getUniqueVisitedLines()) {
      final ClassLine key = new ClassLine(stats.getClassName(), line);
      Set<TestInfo> testsForLine = map.get(key);
      if (testsForLine == null) {
        testsForLine = new TreeSet<TestInfo>(new TestInfoNameComparator()); // inject
        // comparator
        // here
        map.put(key, testsForLine);
      }
      testsForLine.add(test);

    }
  }

  private TestInfo createTestInfo(final Description description,
      final int executionTime, final int linesCovered) {
    final Option<ClassName> testee = this.code.findTestee(description
        .getFirstTestClass());
    return new TestInfo(description.getFirstTestClass(),
        description.getQualifiedName(), executionTime, testee, linesCovered);
  }

  private F2<Integer, ClassName, Integer> numberCoveredLines() {
    return new F2<Integer, ClassName, Integer>() {

      public Integer apply(final Integer a, final ClassName clazz) {
        return a + getNumberOfCoveredLines(clazz);
      }

    };
  }

  private int getNumberOfCoveredLines(final ClassName clazz) {
    final Map<ClassLine, Set<TestInfo>> map = this.classCoverage.get(clazz);
    if (map != null) {
      return map.size();
    } else {
      return 0;
    }

  }

  private Map<ClassLine, Set<TestInfo>> getTestsForClassName(
      final ClassName clazz) {
    // Use any test that provided some coverage of the class
    // This fails to consider tests that only accessed a static variable
    // of the class in question as this does not register as coverage.

    Map<ClassLine, Set<TestInfo>> map = this.classCoverage.get(clazz);
    if (map == null) {
      map = new LinkedHashMap<ClassLine, Set<TestInfo>>(0);
    }
    return map;
  }

  private void recordTestFailure() {
    this.hasFailedTest = true;
  }

  private Map<ClassLine, Set<TestInfo>> getCoverageMapForClass(
      final ClassName className) {
    Map<ClassLine, Set<TestInfo>> map = this.classCoverage.get(className);
    if (map == null) {
      map = new LinkedHashMap<ClassLine, Set<TestInfo>>(0);
      this.classCoverage.put(className, map);
    }
    return map;
  }

  public BigInteger getCoverageIdForClass(final ClassName clazz) {
    final Map<ClassLine, Set<TestInfo>> coverage = this.classCoverage
        .get(clazz);
    if (coverage == null) {
      return BigInteger.ZERO;
    }

    return generateCoverageNumber(coverage);
  }

  public List<LineCoverage> createLineCoverage() {
    return FCollection.flatMap(this.classCoverage.values(), toLineCoverage());
  }

  private F<Map<ClassLine, Set<TestInfo>>, Collection<LineCoverage>> toLineCoverage() {
    return new F<Map<ClassLine, Set<TestInfo>>, Collection<LineCoverage>>() {
      public Collection<LineCoverage> apply(
          final Map<ClassLine, Set<TestInfo>> a) {
        return FCollection.map(a.entrySet(), entryToLineCoverage());
      }
    };
  }

  private F<Entry<ClassLine, Set<TestInfo>>, LineCoverage> entryToLineCoverage() {
    return new F<Entry<ClassLine, Set<TestInfo>>, LineCoverage>() {
      public LineCoverage apply(final Entry<ClassLine, Set<TestInfo>> a) {
        return new LineCoverage(a.getKey(), FCollection.map(a.getValue(),
            TestInfo.toName()));
      }

    };
  }

  private BigInteger generateCoverageNumber(
      final Map<ClassLine, Set<TestInfo>> coverage) {
    BigInteger coverageNumber = BigInteger.ZERO;
    final Set<ClassName> testClasses = new HashSet<ClassName>();
    FCollection.flatMapTo(coverage.values(), testsToClassName(), testClasses);

    for (final ClassInfo each : this.code.getClassInfo(testClasses)) {
      coverageNumber = coverageNumber.add(each.getDeepHash());
    }

    return coverageNumber;
  }

  private F<Set<TestInfo>, Iterable<ClassName>> testsToClassName() {
    return new F<Set<TestInfo>, Iterable<ClassName>>() {
      public Iterable<ClassName> apply(final Set<TestInfo> a) {
        return FCollection.map(a, TestInfo.toDefiningClassName());
      }
    };
  }

  public Collection<ClassInfo> getClassesForFile(final String sourceFile, String packageName) {
    return FCollection.filter(this.code.getCode(), matchesSourceAndPackage(sourceFile, packageName));
  }

  private static F<ClassInfo, Boolean> matchesSourceAndPackage(final String sourceFile,
    final String packageName) {
    return new F<ClassInfo, Boolean>() {
      public Boolean apply(final ClassInfo a) {
        return a.getSourceFileName().equals(sourceFile) &&
            a.getName().getPackage().asJavaName().equals(packageName);
      }
    };
  }

  public CoverageSummary createSummary() {
    return new CoverageSummary(numberOfLines(), coveredLines());
  }

  private int numberOfLines() {
    return FCollection.fold(numberLines(), 0,
        this.code.getClassInfo(this.classCoverage.keySet()));
  }

  private int coveredLines() {
    return FCollection.fold(numberCoveredLines(), 0,
        this.classCoverage.keySet());
  }

  private F2<Integer, ClassInfo, Integer> numberLines() {
    return new F2<Integer, ClassInfo, Integer>() {

      public Integer apply(final Integer a, final ClassInfo clazz) {
        return a + clazz.getNumberOfCodeLines();
      }

    };
  }
}
