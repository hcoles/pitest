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

import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.CodeSource;
import org.pitest.functional.FCollection;
import org.pitest.testapi.Description;
import org.pitest.util.Log;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CoverageData implements CoverageDatabase {

  private static final Logger LOG = Log.getLogger();

  private final Map<BlockLocation, Set<TestInfo>> blockCoverage = new LinkedHashMap<>();
  private final LegacyClassCoverage legacyClassCoverage;

  private final CodeSource code;

  private final List<Description> failingTestDescriptions = new ArrayList<>();

  public CoverageData(final CodeSource code, final LineMap lm) {
    this.code = code;
    this.legacyClassCoverage = new LegacyClassCoverage(code, lm);
  }

  public void calculateClassCoverage(final CoverageResult cr) {

    checkForFailedTest(cr);
    final TestInfo ti = this.createTestInfo(cr.getTestUnitDescription(),
            cr.getExecutionTime(), cr.getNumberOfCoveredBlocks());

    legacyClassCoverage.addTestToClasses(ti,cr.getCoverage());

    for (final BlockLocation each : cr.getCoverage()) {
        addTestsToBlockMap(ti, each);
    }
  }

  // populates class with class level data only, without block level data
  public void loadBlockDataOnly(Collection<BlockLocation> coverageData) {
    legacyClassCoverage.loadBlockDataOnly(coverageData);
  }


  @Override
  public Collection<TestInfo> getTestsForBlockLocation(BlockLocation location) {
    return this.blockCoverage.getOrDefault(location, Collections.emptySet());
  }

  @Override
  public Collection<TestInfo> getTestsForClassLine(final ClassLine classLine) {
    return legacyClassCoverage.getTestsForClassLine(classLine);
  }

  public boolean allTestsGreen() {
    return this.failingTestDescriptions.isEmpty();
  }

  public int getCountFailedTests() {
    return this.failingTestDescriptions.size();
  }

  public List<Description> getFailingTestDescriptions() {
    return failingTestDescriptions;
  }

  @Override
  public Collection<ClassInfo> getClassInfo(final Collection<ClassName> classes) {
    return this.code.getClassInfo(classes);
  }

  @Override
  public int getNumberOfCoveredLines(final Collection<ClassName> mutatedClass) {
    return legacyClassCoverage.getNumberOfCoveredLines(mutatedClass);
  }

  @Override
  public Collection<TestInfo> getTestsForClass(final ClassName clazz) {
    return legacyClassCoverage.getTestsForClass(clazz);
  }

  private void addTestsToBlockMap(final TestInfo ti, BlockLocation each) {
    Set<TestInfo> tests = this.blockCoverage.get(each);
    if (tests == null) {
      tests = new TreeSet<>(new TestInfoNameComparator());
      this.blockCoverage.put(each, tests);
    }
    tests.add(ti);
  }

  @Override
  public BigInteger getCoverageIdForClass(final ClassName clazz) {
    final Collection<TestInfo> coverage = getTestsForClass(clazz);
    if (coverage.isEmpty()) {
      return BigInteger.ZERO;
    }

    return generateCoverageNumber(coverage);
  }

  public List<BlockCoverage> createCoverage() {
    return FCollection.map(this.blockCoverage.entrySet(), toBlockCoverage());
  }

  private static Function<Entry<BlockLocation, Set<TestInfo>>, BlockCoverage> toBlockCoverage() {
    return a -> new BlockCoverage(a.getKey(), FCollection.map(a.getValue(),
        TestInfo.toName()));
  }

  @Override
  public Collection<ClassInfo> getClassesForFile(final String sourceFile,
      String packageName) {
    return legacyClassCoverage.getClassesForFile(sourceFile, packageName);
  }

  @Override
  public CoverageSummary createSummary() {
    return new CoverageSummary(numberOfLines(), coveredLines());
  }

  private BigInteger generateCoverageNumber(Collection<TestInfo> coverage) {
    BigInteger coverageNumber = BigInteger.ZERO;
    Set<ClassName> testClasses = coverage.stream()
            .map(TestInfo.toDefiningClassName())
            .collect(Collectors.toSet());

    for (final ClassInfo each : this.code.getClassInfo(testClasses)) {
      coverageNumber = coverageNumber.add(each.getDeepHash());
    }

    return coverageNumber;
  }

  private Collection<ClassName> allClasses() {
    return this.code.getCodeUnderTestNames();
  }

  private int numberOfLines() {
    return FCollection.fold(numberLines(), 0,
        this.code.getClassInfo(allClasses()));
  }

  private int coveredLines() {
    return getNumberOfCoveredLines(allClasses());
  }

  private BiFunction<Integer, ClassInfo, Integer> numberLines() {
    return (a, clazz) -> a + clazz.getNumberOfCodeLines();
  }

  private void checkForFailedTest(final CoverageResult cr) {
    if (!cr.isGreenTest()) {
      recordTestFailure(cr.getTestUnitDescription());
      LOG.severe(cr.getTestUnitDescription()
          + " did not pass without mutation.");
    }
  }

  private TestInfo createTestInfo(final Description description,
      final int executionTime, final int linesCovered) {

    if (description.getFirstTestClass() == null) {
      System.out.println(description);
    }

    final Optional<ClassName> testee = this.code.findTestee(description
        .getFirstTestClass());
    return new TestInfo(description.getFirstTestClass(),
        description.getQualifiedName(), executionTime, testee, linesCovered);
  }

  private void recordTestFailure(final Description testDescription) {
    this.failingTestDescriptions.add(testDescription);
  }

}
