package org.pitest.mutationtest.statistics;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.pitest.classinfo.ClassName;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.MutationResult;

class MutationStatisticsPrecursor {
  private final Map<String, ScorePrecursor> mutatorTotalMap  = new HashMap<>();
  private final Set<ClassName> mutatedClasses = new HashSet<>();
  private long                              numberOfTestsRun = 0;

  public void registerResults(final Collection<MutationResult> results) {
    results.forEach(register());
  }

  public void registerClass(ClassName mutatedClass) {
    mutatedClasses.add(mutatedClass);
  }

  public Set<ClassName> mutatedClasses() {
    return mutatedClasses;
  }

  private Consumer<MutationResult> register() {
    return mr -> {
      MutationStatisticsPrecursor.this.numberOfTestsRun = MutationStatisticsPrecursor.this.numberOfTestsRun
          + mr.getNumberOfTestsRun();
      final String key = mr.getDetails().getId().getMutator();
      ScorePrecursor total = MutationStatisticsPrecursor.this.mutatorTotalMap
          .get(key);
      if (total == null) {
        total = new ScorePrecursor(key);
        MutationStatisticsPrecursor.this.mutatorTotalMap.put(key, total);
      }
      total.registerResult(mr.getStatus());
    };
  }

  public MutationStatistics toStatistics() {
    final Iterable<Score> scores = getScores();
    final long totalMutations = FCollection.fold(addTotals(), 0L, scores);
    final long totalDetected = FCollection
        .fold(addDetectedTotals(), 0L, scores);
    final long totalWithCoverage = FCollection.fold(addCoveredTotals(), 0L, scores);
    return new MutationStatistics(scores, totalMutations, totalDetected, totalWithCoverage,
        this.numberOfTestsRun, mutatedClasses());
  }

  Iterable<Score> getScores() {
    return this.mutatorTotalMap.values().stream()
            .map(ScorePrecursor::toScore)
            .collect(Collectors.toList());
  }


  private static BiFunction<Long, Score, Long> addTotals() {
    return (a, b) -> a + b.getTotalMutations();
  }

  private static BiFunction<Long, Score, Long> addDetectedTotals() {
    return (a, b) -> a + b.getTotalDetectedMutations();
  }

  private static BiFunction<Long, Score, Long> addCoveredTotals() {
    return (a, b) -> a + b.getTotalWithCoverage();
  }


}