package org.pitest.mutationtest.statistics;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.pitest.functional.F;
import org.pitest.functional.F2;
import org.pitest.functional.FCollection;
import org.pitest.functional.SideEffect1;
import org.pitest.mutationtest.MutationResult;

class MutationStatisticsPrecursor {
  private final Map<String, ScorePrecursor> mutatorTotalMap  = new HashMap<String, ScorePrecursor>();
  private long                              numberOfTestsRun = 0;

  public void registerResults(final Collection<MutationResult> results) {
    FCollection.forEach(results, register());
  }

  private SideEffect1<MutationResult> register() {
    return new SideEffect1<MutationResult>() {

      @Override
      public void apply(final MutationResult mr) {
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
      }

    };
  }

  public MutationStatistics toStatistics() {
    final Iterable<Score> scores = getScores();
    final long totalMutations = FCollection.fold(addTotals(), 0L, scores);
    final long totalDetected = FCollection
        .fold(addDetectedTotals(), 0L, scores);
    return new MutationStatistics(scores, totalMutations, totalDetected,
        this.numberOfTestsRun);
  }

  Iterable<Score> getScores() {
    return FCollection.map(this.mutatorTotalMap.values(), toScore());
  }

  private static F<ScorePrecursor, Score> toScore() {
    return new F<ScorePrecursor, Score>() {
      @Override
      public Score apply(ScorePrecursor a) {
        return a.toScore();
      }

    };
  }

  private static F2<Long, Score, Long> addTotals() {
    return new F2<Long, Score, Long>() {

      @Override
      public Long apply(final Long a, final Score b) {
        return a + b.getTotalMutations();
      }

    };
  }

  private static F2<Long, Score, Long> addDetectedTotals() {
    return new F2<Long, Score, Long>() {

      @Override
      public Long apply(final Long a, final Score b) {
        return a + b.getTotalDetectedMutations();
      }

    };
  }
}