/*
 * Copyright 2011 Henry Coles
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
package org.pitest.mutationtest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.pitest.Description;
import org.pitest.coverage.domain.TestInfo;
import org.pitest.extension.Configuration;
import org.pitest.extension.TestUnit;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Prelude;
import org.pitest.internal.IsolationUtils;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.instrument.MutationTestUnit;
import org.pitest.mutationtest.instrument.PercentAndConstantTimeoutStrategy;
import org.pitest.util.JavaAgent;
import org.pitest.util.Log;

public class MutationTestBuilder {

  private final static int     TIME_WEIGHTING_FOR_DIRECT_UNIT_TESTS = 1000;

  private final static Logger  LOG                                  = Log
                                                                        .getLogger();

  private final ReportOptions  data;
  private final JavaAgent      javaAgentFinder;
  private final MutationConfig mutationConfig;

  private final Configuration  initialConfig;

  public MutationTestBuilder(final MutationConfig mutationConfig,
      final Configuration initialConfig, final ReportOptions data,
      final JavaAgent javaAgentFinder) {
    this.data = data;
    this.javaAgentFinder = javaAgentFinder;
    this.mutationConfig = mutationConfig;
    this.initialConfig = initialConfig;
  }

  public List<TestUnit> createMutationTestUnits(
      final Collection<ClassGrouping> codeClasses,
      final Configuration pitConfig, final CoverageDatabase coverageDatabase) {
    final List<TestUnit> tus = new ArrayList<TestUnit>();

    for (final ClassGrouping classGroup : codeClasses) {

      final Collection<MutationDetails> mutationsForClasses = createMutations(
          coverageDatabase, this.mutationConfig, classGroup);

      tus.add(createMutationTestUnit(this.mutationConfig, mutationsForClasses,
          classGroup.getParent()));

    }
    return tus;
  }

  private Collection<MutationDetails> createMutations(
      final CoverageDatabase coverageDatabase,
      final MutationConfig mutationConfig, final ClassGrouping classesToMutate) {
    mutationConfig.createMutator(IsolationUtils.getContextClassLoader());

    final Mutater m = mutationConfig.createMutator(IsolationUtils
        .getContextClassLoader());

    final Collection<MutationDetails> availableMutations = m
        .findMutations(classesToMutate);

    assignTestsToMutations(availableMutations, coverageDatabase);

    return availableMutations;

  }

  private void assignTestsToMutations(
      final Collection<MutationDetails> availableMutations,
      final CoverageDatabase coverageDatabase) {
    for (final MutationDetails mutation : availableMutations) {
      final Collection<TestInfo> testDetails = prioritizeTests(mutation,
          coverageDatabase);

      mutation.addTestsInOrder(testDetails);
    }
  }

  private Collection<TestInfo> prioritizeTests(final MutationDetails mutation,
      final CoverageDatabase coveageDatabase) {
    final Collection<TestInfo> testsForMutant = getTestsForMutant(
        coveageDatabase, mutation);
    final List<TestInfo> sortedTis = FCollection.map(testsForMutant,
        Prelude.id(TestInfo.class));
    Collections.sort(sortedTis,
        new TestInfoPriorisationComparator(mutation.getClazz(),
            TIME_WEIGHTING_FOR_DIRECT_UNIT_TESTS));
    return sortedTis;
  }

  private Collection<TestInfo> getTestsForMutant(
      final CoverageDatabase coverageDatabase, final MutationDetails mutation) {

    if (!mutation.isInStaticInitializer()) {
      return coverageDatabase.getTestsForClassLine(mutation.getClassLine());
    } else {
      LOG.warning("Using untargetted tests");
      return coverageDatabase.getTestsForClass(mutation.getJVMClassName());
    }
  }

  private TestUnit createMutationTestUnit(final MutationConfig mutationConfig,
      final Collection<MutationDetails> mutationsForClasses,
      final String parentClassName) {

    final Description d = new Description(
        "mutation test of " + parentClassName, MutationCoverageReport.class,
        null);

    final Set<String> uniqueTestClasses = new HashSet<String>();
    FCollection.flatMapTo(mutationsForClasses, mutationDetailsToTestClass(),
        uniqueTestClasses);

    return new MutationTestUnit(mutationsForClasses, uniqueTestClasses,
        this.initialConfig, mutationConfig, d, this.javaAgentFinder,
        new PercentAndConstantTimeoutStrategy(this.data.getTimeoutFactor(),
            this.data.getTimeoutConstant()));
  }

  private F<MutationDetails, Iterable<String>> mutationDetailsToTestClass() {
    return new F<MutationDetails, Iterable<String>>() {

      public Iterable<String> apply(final MutationDetails a) {
        return FCollection.flatMap(a.getTestsInOrder(),
            TestInfo.toDefiningClassNames());
      }

    };
  }

}
