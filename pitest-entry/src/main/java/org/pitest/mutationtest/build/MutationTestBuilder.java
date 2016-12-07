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
package org.pitest.mutationtest.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pitest.classinfo.ClassName;
import org.pitest.coverage.TestInfo;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationAnalyser;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.engine.MutationDetails;

public class MutationTestBuilder {

  private final MutationSource   mutationSource;
  private final MutationAnalyser analyser;
  private final WorkerFactory    workerFactory;
  private final MutationGrouper  grouper;

  public MutationTestBuilder(final WorkerFactory workerFactory,
      final MutationAnalyser analyser, final MutationSource mutationSource,
      final MutationGrouper grouper) {

    this.mutationSource = mutationSource;
    this.analyser = analyser;
    this.workerFactory = workerFactory;
    this.grouper = grouper;
  }

  public List<MutationAnalysisUnit> createMutationTestUnits(
      final Collection<ClassName> codeClasses) {
    final List<MutationAnalysisUnit> tus = new ArrayList<MutationAnalysisUnit>();

    final List<MutationDetails> mutations = FCollection.flatMap(codeClasses,
        classToMutations());

    Collections.sort(mutations, comparator());

    final Collection<MutationResult> analysedMutations = this.analyser
        .analyse(mutations);

    final Collection<MutationDetails> needAnalysis = FCollection.filter(
        analysedMutations, statusNotKnown()).map(resultToDetails());

    final List<MutationResult> analysed = FCollection.filter(analysedMutations,
        Prelude.not(statusNotKnown()));

    if (!analysed.isEmpty()) {
      tus.add(makePreAnalysedUnit(analysed));
    }

    if (!needAnalysis.isEmpty()) {
      for (final Collection<MutationDetails> ms : this.grouper.groupMutations(
          codeClasses, needAnalysis)) {
        tus.add(makeUnanalysedUnit(ms));
      }
    }

    Collections.sort(tus, new AnalysisPriorityComparator());
    return tus;
  }

  private Comparator<MutationDetails> comparator() {
    return new Comparator<MutationDetails>() {

      @Override
      public int compare(final MutationDetails arg0, final MutationDetails arg1) {
        return arg0.getId().compareTo(arg1.getId());
      }

    };
  }

  private F<ClassName, Iterable<MutationDetails>> classToMutations() {
    return new F<ClassName, Iterable<MutationDetails>>() {
      @Override
      public Iterable<MutationDetails> apply(final ClassName a) {
        return MutationTestBuilder.this.mutationSource.createMutations(a);
      }

    };
  }

  private MutationAnalysisUnit makePreAnalysedUnit(
      final List<MutationResult> analysed) {
    return new KnownStatusMutationTestUnit(analysed);
  }

  private MutationAnalysisUnit makeUnanalysedUnit(
      final Collection<MutationDetails> needAnalysis) {
    final Set<ClassName> uniqueTestClasses = new HashSet<ClassName>();
    FCollection.flatMapTo(needAnalysis, mutationDetailsToTestClass(),
        uniqueTestClasses);

    return new MutationTestUnit(needAnalysis, uniqueTestClasses,
        this.workerFactory);
  }

  private static F<MutationResult, MutationDetails> resultToDetails() {
    return new F<MutationResult, MutationDetails>() {
      @Override
      public MutationDetails apply(final MutationResult a) {
        return a.getDetails();
      }
    };
  }

  private static F<MutationResult, Boolean> statusNotKnown() {
    return new F<MutationResult, Boolean>() {
      @Override
      public Boolean apply(final MutationResult a) {
        return a.getStatus() == DetectionStatus.NOT_STARTED;
      }
    };
  }

  private static F<MutationDetails, Iterable<ClassName>> mutationDetailsToTestClass() {
    return new F<MutationDetails, Iterable<ClassName>>() {
      @Override
      public Iterable<ClassName> apply(final MutationDetails a) {
        return FCollection.map(a.getTestsInOrder(),
            TestInfo.toDefiningClassName());
      }
    };
  }

}
