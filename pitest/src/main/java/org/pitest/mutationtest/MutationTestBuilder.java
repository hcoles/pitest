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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.pitest.classinfo.ClassName;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.coverage.domain.TestInfo;
import org.pitest.extension.Configuration;
import org.pitest.extension.TestUnit;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.Prelude;
import org.pitest.internal.ClassByteArraySource;
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
  private final MutationConfig        mutationConfig;
  private final CoverageDatabase      coverageDatabase;
  private final MutationFilterFactory filterFactory;
  private final ClassByteArraySource  source;
  private final Configuration         configuration;
  private final JavaAgent             javaAgent;
  private final File baseDir;

  public MutationTestBuilder(final File baseDir, final MutationConfig mutationConfig,
      final MutationFilterFactory filterFactory,
      final CoverageDatabase coverageDatabase, final ReportOptions data,
      final ClassByteArraySource source, final Configuration configuration,
      final JavaAgent javaAgent) {
    this.data = data;
    this.mutationConfig = mutationConfig;
    this.coverageDatabase = coverageDatabase;
    this.filterFactory = filterFactory;
    this.source = source;
    this.configuration = configuration;
    this.javaAgent = javaAgent;
    this.baseDir = baseDir;
  }

  public List<TestUnit> createMutationTestUnits(
      final Collection<ClassName> codeClasses) {
    final List<TestUnit> tus = new ArrayList<TestUnit>();

    for (final ClassName clazz : codeClasses) {

      final Collection<MutationDetails> mutationsForClasses = createMutations(clazz);

      if (this.data.getMutationUnitSize() > 0) {
        final FunctionalList<List<MutationDetails>> groupedMutations = FCollection
            .splitToLength(this.data.getMutationUnitSize(), mutationsForClasses);
        FCollection.mapTo(groupedMutations, mutationDetailsToTestUnit(), tus);
      } else {
        tus.add(createMutationTestUnit(mutationsForClasses));
      }
    }
    return tus;
  }

  private F<List<MutationDetails>, TestUnit> mutationDetailsToTestUnit() {
    return new F<List<MutationDetails>, TestUnit>() {
      public TestUnit apply(final List<MutationDetails> mutations) {
        return createMutationTestUnit(mutations);
      }
    };
  }

  private Collection<MutationDetails> createMutations(final ClassName clazz) {

    final MutationFilter filter = this.filterFactory.createFilter();
    final Mutater m = this.mutationConfig.createMutator(this.source);

    final Collection<MutationDetails> availableMutations = filter.filter(m
        .findMutations(clazz));

    assignTestsToMutations(availableMutations);

    return availableMutations;

  }

  private void assignTestsToMutations(
      final Collection<MutationDetails> availableMutations) {
    for (final MutationDetails mutation : availableMutations) {
      final Collection<TestInfo> testDetails = prioritizeTests(mutation);

      if (testDetails.isEmpty()) {
        LOG.fine("According to coverage no tests hit the mutation " + mutation);
      }

      mutation.addTestsInOrder(testDetails);
    }
  }

  private Collection<TestInfo> prioritizeTests(final MutationDetails mutation) {
    final Collection<TestInfo> testsForMutant = getTestsForMutant(mutation);
    final List<TestInfo> sortedTis = FCollection.map(testsForMutant,
        Prelude.id(TestInfo.class));
    Collections.sort(sortedTis, new TestInfoPriorisationComparator(
        new ClassName(mutation.getClazz()),
        TIME_WEIGHTING_FOR_DIRECT_UNIT_TESTS));
    return sortedTis;
  }

  private Collection<TestInfo> getTestsForMutant(final MutationDetails mutation) {
    if (!mutation.isInStaticInitializer()) {
      return this.coverageDatabase
          .getTestsForClassLine(mutation.getClassLine());
    } else {
      LOG.warning("Using untargetted tests");
      return this.coverageDatabase.getTestsForClass(mutation.getJVMClassName());
    }
  }

  private TestUnit createMutationTestUnit(
      final Collection<MutationDetails> mutationsForClasses) {

    final Set<ClassName> uniqueTestClasses = new HashSet<ClassName>();
    FCollection.flatMapTo(mutationsForClasses, mutationDetailsToTestClass(),
        uniqueTestClasses);

    return new MutationTestUnit(baseDir, mutationsForClasses, uniqueTestClasses,
        this.configuration, this.mutationConfig, this.javaAgent,
        new PercentAndConstantTimeoutStrategy(this.data.getTimeoutFactor(),
            this.data.getTimeoutConstant()), this.data.isVerbose(), this.data
            .getClassPath().getLocalClassPath());
  }

  private static F<MutationDetails, Iterable<ClassName>> mutationDetailsToTestClass() {
    return new F<MutationDetails, Iterable<ClassName>>() {
      public Iterable<ClassName> apply(final MutationDetails a) {
        return FCollection.map(a.getTestsInOrder(),
            TestInfo.toDefiningClassName());
      }
    };
  }

}
