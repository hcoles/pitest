package org.pitest.mutationtest.statistics;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.pitest.functional.FCollection;
import org.pitest.mutationtest.MutationResult;

class MutationStatisticsPrecursor {
  private final Map<String, ScorePrecursor> mutatorTotalMap  = new HashMap<>();
  private long                              numberOfTestsRun = 0;

  public void registerResults(final Collection<MutationResult> results) {
    results.forEach(register());
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
        this.numberOfTestsRun);
  }

  Iterable<Score> getScores() {
    return FCollection.map(this.mutatorTotalMap.values(), ScorePrecursor::toScore);
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