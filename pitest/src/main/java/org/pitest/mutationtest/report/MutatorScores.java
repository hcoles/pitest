package org.pitest.mutationtest.report;

import java.util.Collection;
import java.util.Map;

import org.pitest.functional.F2;
import org.pitest.functional.FCollection;
import org.pitest.functional.SideEffect1;
import org.pitest.mutationtest.results.MutationResult;
import org.pitest.util.MemoryEfficientHashMap;

public class MutatorScores {

  public static class Total {
    private final String name;
    private int          count;
    private int          detected;

    Total(final String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return this.name + " : Generated " + this.count + " / "
          + (this.count - this.detected) + " survived.";
    }

  }

  private final Map<String, Total> mutatorTotalMap = new MemoryEfficientHashMap<String, Total>();

  public void registerResults(final Collection<MutationResult> results) {
    FCollection.forEach(results, register());
  }

  public Collection<Total> getTotals() {
    return this.mutatorTotalMap.values();
  }

  private SideEffect1<MutationResult> register() {
    return new SideEffect1<MutationResult>() {

      public void apply(final MutationResult mr) {
        final String key = mr.details.getId().getMutator();
        Total total = MutatorScores.this.mutatorTotalMap.get(key);
        if (total == null) {
          total = new Total(key);
          MutatorScores.this.mutatorTotalMap.put(key, total);
        }
        total.count += 1;
        total.detected += mr.status.isDetected() ? 1 : 0;
      }

    };
  }

  public long getTotalMutations() {
    return FCollection.fold(addTotals(), 0, this.mutatorTotalMap.values());
  }

  public long getTotalDetectedMutations() {
    return FCollection.fold(addDetected(), 0, this.mutatorTotalMap.values());
  }

  private F2<Integer, Total, Integer> addDetected() {
    return new F2<Integer, Total, Integer>() {
      public Integer apply(final Integer a, final Total b) {
        return a + b.detected;
      }

    };
  }

  private F2<Integer, Total, Integer> addTotals() {
    return new F2<Integer, Total, Integer>() {
      public Integer apply(final Integer a, final Total b) {
        return a + b.count;
      }

    };
  }

}
