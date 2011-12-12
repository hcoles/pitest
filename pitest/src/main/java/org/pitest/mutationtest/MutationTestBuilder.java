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
import org.pitest.classinfo.ClassName;
import org.pitest.coverage.domain.TestInfo;
import org.pitest.extension.Configuration;
import org.pitest.extension.TestUnit;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Prelude;
import org.pitest.internal.IsolationUtils;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.filter.MutationFilter;
import org.pitest.mutationtest.filter.MutationFilterFactory;
import org.pitest.mutationtest.instrument.MutationTestUnit;
import org.pitest.mutationtest.instrument.PercentAndConstantTimeoutStrategy;
import org.pitest.util.JavaAgent;
import org.pitest.util.Log;

public class MutationTestBuilder {

  private final static int            TIME_WEIGHTING_FOR_DIRECT_UNIT_TESTS = 1000;

  private final static Logger         LOG                                  = Log
  .getLogger();

  private final ReportOptions         data;
  private final JavaAgent             javaAgentFinder;
  private final MutationConfig        mutationConfig;
  private final Configuration         initialConfig;
  private final MutationFilterFactory filterFactory;

  public MutationTestBuilder(final MutationConfig mutationConfig,
      final MutationFilterFactory filterFactory,
      final Configuration initialConfig, final ReportOptions data,
      final JavaAgent javaAgentFinder) {
    this.data = data;
    this.javaAgentFinder = javaAgentFinder;
    this.mutationConfig = mutationConfig;
    this.initialConfig = initialConfig;
    this.filterFactory = filterFactory;
  }

  public List<TestUnit> createMutationTestUnits(
      final Collection<ClassGrouping> codeClasses,
      final Configuration pitConfig, final CoverageDatabase coverageDatabase) {
    final List<TestUnit> tus = new ArrayList<TestUnit>();

    for (final ClassGrouping classGroup : codeClasses) {

      final Collection<MutationDetails> mutationsForClasses = createMutations(
          coverageDatabase, this.mutationConfig, classGroup,
          this.filterFactory.createFilter());

      tus.add(createMutationTestUnit(this.mutationConfig, mutationsForClasses));

    }
    return tus;
  }

  private Collection<MutationDetails> createMutations(
      final CoverageDatabase coverageDatabase,
      final MutationConfig mutationConfig, final ClassGrouping classesToMutate,
      final MutationFilter filter) {
    mutationConfig.createMutator(IsolationUtils.getContextClassLoader());

    final Mutater m = mutationConfig.createMutator(IsolationUtils
        .getContextClassLoader());

    final Collection<MutationDetails> availableMutations = filter.filter(m
        .findMutations(classesToMutate));

    assignTestsToMutations(availableMutations, coverageDatabase);

    return availableMutations;

  }

  private void assignTestsToMutations(
      final Collection<MutationDetails> availableMutations,
      final CoverageDatabase coverageDatabase) {
    for (final MutationDetails mutation : availableMutations) {
      final Collection<TestInfo> testDetails = prioritizeTests(mutation,
          coverageDatabase);

      if (testDetails.isEmpty()) {
        LOG.fine("According to coverage no tests hit the mutation " + mutation);
      }

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
      final Collection<MutationDetails> mutationsForClasses) {

    final Description d = new Description( "mutation test", (String)null);

    final Set<ClassName> uniqueTestClasses = new HashSet<ClassName>();
    FCollection.flatMapTo(mutationsForClasses, mutationDetailsToTestClass(),
        uniqueTestClasses);

    return new MutationTestUnit(mutationsForClasses, uniqueTestClasses,
        this.initialConfig, mutationConfig, d, this.javaAgentFinder,
        new PercentAndConstantTimeoutStrategy(this.data.getTimeoutFactor(),
            this.data.getTimeoutConstant()), this.data.isVerbose());
  }

  private F<MutationDetails, Iterable<ClassName>> mutationDetailsToTestClass() {
    return new F<MutationDetails, Iterable<ClassName>>() {

      public Iterable<ClassName> apply(final MutationDetails a) {
        return FCollection.map(a.getTestsInOrder(),
            TestInfo.toDefiningClassName());
      }

    };
  }

}
