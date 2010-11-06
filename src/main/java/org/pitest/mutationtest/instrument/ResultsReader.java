/*
 * Copyright 2010 Henry Coles
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
package org.pitest.mutationtest.instrument;

import java.util.Map;

import org.pitest.functional.Option;
import org.pitest.functional.SideEffect1;
import org.pitest.internal.IsolationUtils;
import org.pitest.mutationtest.MutationDetails;

public class ResultsReader implements SideEffect1<String> {

  public static class MutationResult {

    public MutationResult(final MutationDetails md) {
      this.details = md;
    }

    public final MutationDetails details;
    public int                   numberOfTestsHittingMutatedLine;
    public boolean               detected;
  }

  private int                                lastRunMutation;
  private StringBuffer                       lineBuffer = new StringBuffer();
  private Option<Statistics>                 stats;
  // store as map rather than list to allow possibility of order mutation
  // results
  private final Map<Integer, MutationResult> mutations;

  public ResultsReader(final int lastRunMutation,
      final Map<Integer, MutationResult> mutations,
      final Option<Statistics> stats) {
    this.lastRunMutation = lastRunMutation;
    this.stats = stats;
    this.mutations = mutations;
  }

  public void apply(final String a) {
    final int lineEnd = a.indexOf("\n");
    if (lineEnd == -1) {
      this.lineBuffer.append(a);
    } else {
      this.lineBuffer.append(a.substring(0, lineEnd));
      process();
      apply(a.substring(lineEnd + 1, a.length()));
    }

  }

  @SuppressWarnings("unchecked")
  private void process() {
    final String line = this.lineBuffer.toString();
    System.out.println("Result from file " + line);
    this.lineBuffer = new StringBuffer();
    if (line.startsWith("STATS=")) {
      this.stats = (Option<Statistics>) IsolationUtils.fromTransportString(line
          .substring(6, line.length()));
    } else {
      final String[] parts = line.split(",");
      if (parts[0].equals("DESC=")) {
        receiveMutationDescription(parts);
      } else {
        receiveMutationResults(parts);
      }

    }

  }

  private void receiveMutationDescription(final String[] parts) {
    final int mutation = extractMutationIndex(parts);
    final MutationDetails details = new MutationDetails(parts[3], parts[4],
        parts[5], parts[6]);
    this.mutations.put(mutation, new MutationResult(details));

  }

  private int extractMutationIndex(final String[] parts) {
    return Integer.parseInt(parts[1]);
  }

  private void receiveMutationResults(final String[] parts) {
    this.lastRunMutation = extractMutationIndex(parts);
    final MutationResult mr = this.mutations.get(this.lastRunMutation);
    if (parts[2].contains("true")) {
      mr.detected = true;
    } else {
      mr.detected = false;
    }
  }

  public SlaveResult getResult() {
    return new SlaveResult(this.lastRunMutation, this.stats);
  }

}
