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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.pitest.coverage.TestInfo;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.engine.MutationDetails;

public class MutationStatusMap {

  private final Map<MutationDetails, MutationStatusTestPair> mutationMap = new HashMap<>();

  public void setStatusForMutation(final MutationDetails mutation,
      final DetectionStatus status) {
    this.setStatusForMutations(Collections.singleton(mutation), status);
  }

  public void setStatusForMutation(final MutationDetails mutation,
      final MutationStatusTestPair status) {
    this.mutationMap.put(mutation, status);
  }

  public void setStatusForMutations(
      final Collection<MutationDetails> mutations, final DetectionStatus status) {
      mutations.forEach(mutationDetails -> {
        List<String> coveringTests = mutationDetails.getTestsInOrder().stream().map(TestInfo::getName).collect(Collectors.toList());
        MutationStatusTestPair pair = MutationStatusTestPair.notAnalysed(0, status, coveringTests);
        mutationMap.put(mutationDetails, pair);
      });
  }

  public List<MutationResult> createMutationResults() {
    return this.mutationMap.entrySet().stream()
            .map(detailsToMutationResults())
            .collect(Collectors.toList());

  }

  public boolean hasUnrunMutations() {
    return !getUnrunMutations().isEmpty();
  }

  public Collection<MutationDetails> getUnrunMutations() {
    return this.mutationMap.entrySet().stream()
        .filter(hasStatus(DetectionStatus.NOT_STARTED))
        .map(toMutationDetails())
        .collect(Collectors.toList());
  }

  public Collection<MutationDetails> getUnfinishedRuns() {
    return this.mutationMap.entrySet().stream()
        .filter(hasStatus(DetectionStatus.STARTED))
        .map(toMutationDetails())
        .collect(Collectors.toList());
  }

  public Set<MutationDetails> allMutations() {
    return this.mutationMap.keySet();
  }

  private static Function<Entry<MutationDetails, MutationStatusTestPair>, MutationResult> detailsToMutationResults() {
    return a -> new MutationResult(a.getKey(), a.getValue());
  }

  private static Function<Entry<MutationDetails, MutationStatusTestPair>, MutationDetails> toMutationDetails() {
    return Entry::getKey;
  }

  private static Predicate<Entry<MutationDetails, MutationStatusTestPair>> hasStatus(
      final DetectionStatus status) {
    return a -> a.getValue().getStatus().equals(status);
  }

  public void markUncoveredMutations() {
    setStatusForMutations(
        FCollection.filter(this.mutationMap.keySet(), hasNoCoverage()),
        DetectionStatus.NO_COVERAGE);

  }

  private static Predicate<MutationDetails> hasNoCoverage() {
    return a -> a.getTestsInOrder().isEmpty();
  }

}
