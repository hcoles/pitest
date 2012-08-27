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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.pitest.classinfo.ClassName;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.coverage.domain.TestInfo;
import org.pitest.functional.FCollection;
import org.pitest.functional.Prelude;
import org.pitest.internal.ClassByteArraySource;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.filter.MutationFilter;
import org.pitest.mutationtest.filter.MutationFilterFactory;
import org.pitest.util.Log;

public class MutationSource {

  private final static int            TIME_WEIGHTING_FOR_DIRECT_UNIT_TESTS = 1000;

  private final static Logger         LOG                                  = Log
                                                                               .getLogger();

  private final MutationConfig        mutationConfig;
  private final CoverageDatabase      coverageDatabase;
  private final MutationFilterFactory filterFactory;
  private final ClassByteArraySource  source;

  public MutationSource(final MutationConfig mutationConfig,
      final MutationFilterFactory filterFactory,
      final CoverageDatabase coverageDatabase, final ClassByteArraySource source) {
    this.mutationConfig = mutationConfig;
    this.coverageDatabase = coverageDatabase;
    this.filterFactory = filterFactory;
    this.source = source;
  }

  public Collection<MutationDetails> createMutations(final ClassName clazz) {

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
      return this.coverageDatabase.getTestsForClass(mutation.getClassName());
    }
  }

}
