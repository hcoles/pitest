package org.pitest.mutationtest.statistics;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.pitest.functional.FCollection;
import org.pitest.mutationtest.DetectionStatus;

class ScorePrecursor {

  private final String                            mutatorName;
  private final Map<DetectionStatus, StatusCount> counts;

  ScorePrecursor(final String name) {
    this.mutatorName = name;
    this.counts = createMap();
  }

  void registerResult(final DetectionStatus result) {
    final StatusCount total = this.counts.get(result);
    total.increment();
  }

  Iterable<StatusCount> getCounts() {
    return this.counts.values();
  }

  private long getTotalMutations() {
    return FCollection.fold(addTotals(), 0L, this.counts.values());
  }

  private long getTotalDetectedMutations() {
    return FCollection.fold(addTotals(), 0L,
        FCollection.filter(this.counts.values(), isDetected()));
  }

  private static Predicate<StatusCount> isDetected() {
    return a -> a.getStatus().isDetected();
  }

  private BiFunction<Long, StatusCount, Long> addTotals() {
    return (a, b) -> a + b.getCount();
  }

  private static Map<DetectionStatus, StatusCount> createMap() {
    final Map<DetectionStatus, StatusCount> map = new LinkedHashMap<>();
    for (final DetectionStatus each : DetectionStatus.values()) {
      map.put(each, new StatusCount(each, 0L));
    }
    return map;
  }

  Score toScore() {
    return new Score(this.mutatorName, this.getCounts(), this.getTotalMutations(),
        this.getTotalDetectedMutations());
  }
}

