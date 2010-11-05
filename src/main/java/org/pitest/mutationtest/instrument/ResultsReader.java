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

import java.util.Collection;

import org.pitest.functional.Option;
import org.pitest.functional.SideEffect1;
import org.pitest.internal.IsolationUtils;
import org.pitest.mutationtest.MutationDetails;

public class ResultsReader implements SideEffect1<String> {
  private int                              lastRunMutation;
  private StringBuffer                     lineBuffer = new StringBuffer();
  private Option<Statistics>               stats;
  private final Collection<AssertionError> results;

  public ResultsReader(final int lastRunMutation,
      final Collection<AssertionError> results, final Option<Statistics> stats) {
    this.lastRunMutation = lastRunMutation;
    this.stats = stats;
    this.results = results;
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
    this.lineBuffer = new StringBuffer();
    if (line.startsWith("STATS=")) {
      this.stats = (Option<Statistics>) IsolationUtils.fromTransportString(line
          .substring(6, line.length()));
    } else {
      final String[] parts = line.split(",");
      this.lastRunMutation = Integer.parseInt(parts[0].substring(0, parts[0]
          .indexOf("=")));
      if (parts[0].contains("false")) {
        this.results.add(arrayToAssertionError(parts));
      }
      System.out.println("Result from file " + line);
    }

  }

  public SlaveResult getResult() {
    return new SlaveResult(this.lastRunMutation, this.stats);
  }

  private AssertionError arrayToAssertionError(final String[] parts) {
    final MutationDetails details = new MutationDetails(parts[1], parts[2],
        parts[3], parts[4]);
    return createAssertionError(details);

  }

  private AssertionError createAssertionError(final MutationDetails md) {
    final AssertionError ae = new AssertionError("The mutation -> " + md
        + " did not result in any test failures");
    final StackTraceElement[] stackTrace = { md.stackTraceDescription() };
    ae.setStackTrace(stackTrace);
    return ae;
  }

}
