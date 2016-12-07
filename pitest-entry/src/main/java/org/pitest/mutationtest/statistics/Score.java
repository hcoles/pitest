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

public final class Score {

  private final String                mutatorName;
  private final Iterable<StatusCount> counts;
  private final long                  totalMutations;
  private final long                  totalDetected;

  public Score(final String name, Iterable<StatusCount> counts,
      long totalMutations, long totalDetected) {
    this.mutatorName = name;
    this.counts = counts;
    this.totalMutations = totalMutations;
    this.totalDetected = totalDetected;
  }

  public void report(final PrintStream out) {
    out.println("> " + this.mutatorName);
    out.println(">> Generated " + this.totalMutations + " Killed "
        + this.totalDetected + " (" + this.getPercentageDetected() + "%)");
    int i = 0;
    StringBuilder sb = new StringBuilder();
    for (final StatusCount each : this.counts) {
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

  public long getTotalMutations() {
    return this.totalMutations;
  }

  public long getTotalDetectedMutations() {
    return totalDetected;
  }

  public int getPercentageDetected() {
    if (getTotalMutations() == 0) {
      return 100;
    }

    if (getTotalDetectedMutations() == 0) {
      return 0;
    }

    return Math.round((100f / getTotalMutations())
        * getTotalDetectedMutations());
  }

}
