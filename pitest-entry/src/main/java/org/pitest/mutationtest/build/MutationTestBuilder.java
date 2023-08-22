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

import org.pitest.classinfo.ClassName;
import org.pitest.functional.FCollection;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationAnalyser;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

public class MutationTestBuilder {

  private final MutationSource   mutationSource;
  private final MutationAnalyser analyser;
  private final WorkerFactory    workerFactory;
  private final MutationGrouper  grouper;

  public MutationTestBuilder(final WorkerFactory workerFactory,
                             final MutationAnalyser analyser,
                             final MutationSource mutationSource,
                             final MutationGrouper grouper) {

    this.mutationSource = mutationSource;
    this.analyser = analyser;
    this.workerFactory = workerFactory;
    this.grouper = grouper;
  }

  public List<MutationAnalysisUnit> createMutationTestUnits(
      final Collection<ClassName> codeClasses) {
    final List<MutationAnalysisUnit> tus = new ArrayList<>();

    final List<MutationDetails> mutations = codeClasses.stream()
                    .flatMap(c -> mutationSource.createMutations(c).stream())
                            .collect(Collectors.toList());

    mutations.sort(comparing(MutationDetails::getId));

    final Collection<MutationResult> analysedMutations = this.analyser
        .analyse(mutations);

    final Collection<MutationDetails> needAnalysis = analysedMutations.stream()
        .filter(statusNotKnown())
        .map(MutationResult::getDetails)
        .collect(Collectors.toList());

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

    tus.sort(new AnalysisPriorityComparator());
    return tus;
  }


  private MutationAnalysisUnit makePreAnalysedUnit(
      final List<MutationResult> analysed) {
    return new KnownStatusMutationTestUnit(analysed);
  }

  private MutationAnalysisUnit makeUnanalysedUnit(
      final Collection<MutationDetails> needAnalysis) {
    return new MutationTestUnit(needAnalysis, this.workerFactory);
  }

  private static Predicate<MutationResult> statusNotKnown() {
    return a -> a.getStatus() == DetectionStatus.NOT_STARTED;
  }

}
