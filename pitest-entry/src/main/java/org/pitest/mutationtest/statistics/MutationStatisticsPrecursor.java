package org.pitest.mutationtest.statistics;

import org.pitest.functional.FCollection;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;

class MutationStatisticsPrecursor {

  private final Map<String, ScorePrecursor> mutatorTotalMap = new HashMap<>();
  private long numberOfTestsRun = 0;

  public void registerResults(final Collection<MutationResult> results) {

    final Map<String, Map<Integer, Integer>> methodCountPerSourceLine = results.stream()
      .map(MutationResult::getDetails)
      .collect(
        groupingBy(
          MutationDetails::getFilename,
          groupingBy(
            MutationDetails::getLineNumber,
            collectingAndThen(
              mapping(MutationDetails::getMethod, toSet()),
              Set::size))));

    results.stream()
      .filter(result ->
        methodCountPerSourceLine
          .get(result.getDetails().getFilename())
          .get(result.getDetails().getLineNumber()) == 1)
      .forEach(register());

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
    return new MutationStatistics(scores, totalMutations, totalDetected, totalWithCoverage, this.numberOfTestsRun);
  }

  Iterable<Score> getScores() {
    return FCollection.map(this.mutatorTotalMap.values(), toScore());
  }

  private static Function<ScorePrecursor, Score> toScore() {
    return a -> a.toScore();
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
