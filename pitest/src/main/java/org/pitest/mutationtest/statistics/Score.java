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
package org.pitest.mutationtest.statistics;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.pitest.functional.F;
import org.pitest.functional.F2;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.DetectionStatus;

public class Score {

  private final String                            mutatorName;
  private final Map<DetectionStatus, StatusCount> counts;

  public Score(final String name) {
    this.mutatorName = name;
    this.counts = createMap();
  }

  private static Map<DetectionStatus, StatusCount> createMap() {
    final Map<DetectionStatus, StatusCount> map = new LinkedHashMap<DetectionStatus, StatusCount>();
    for (final DetectionStatus each : DetectionStatus.values()) {
      map.put(each, new StatusCount(each, 0L));
    }
    return map;
  }

  public void registerResult(final DetectionStatus result) {
    final StatusCount total = this.counts.get(result);
    total.increment();
  }

  public Iterable<StatusCount> getCounts() {
    return this.counts.values();
  }

  public long getTotalMutations() {
    return FCollection.fold(addTotals(), 0L, this.counts.values());
  }

  public long getTotalDetectedMutations() {
    return FCollection.fold(addTotals(), 0L,
        FCollection.filter(this.counts.values(), isDetected()));
  }

  public long getPercentageDetected() {
    if (getTotalMutations() == 0) {
      return 100;
    }

    if (getTotalDetectedMutations() == 0) {
      return 0;
    }

    return Math.round((100f / getTotalMutations())
        * getTotalDetectedMutations());
  }

  private static F<StatusCount, Boolean> isDetected() {
    return new F<StatusCount, Boolean>() {

      @Override
      public Boolean apply(final StatusCount a) {
        return a.getStatus().isDetected();
      }

    };
  }

  private F2<Long, StatusCount, Long> addTotals() {
    return new F2<Long, StatusCount, Long>() {

      @Override
      public Long apply(final Long a, final StatusCount b) {
        return a + b.getCount();
      }

    };
  }

  public void report(final PrintStream out) {
    out.println("> " + this.mutatorName);
    out.println(">> Generated " + this.getTotalMutations() + " Killed "
        + this.getTotalDetectedMutations() + " ("
        + this.getPercentageDetected() + "%)");
    int i = 0;
    StringBuilder sb = new StringBuilder();
    for (final StatusCount each : this.counts.values()) {
      sb.append(each + " ");
      i++;
      if ((i % 4) == 0) {
        out.println("> " + sb.toString());
        sb = new StringBuilder();
      }
    }
    out.println("> " + sb.toString());
  }

  public String getMutatorName() {
    return this.mutatorName;
  }

}
