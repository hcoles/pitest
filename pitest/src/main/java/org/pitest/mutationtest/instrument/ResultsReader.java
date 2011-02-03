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
import org.pitest.mutationtest.engine.MutationIdentifier;

import com.thoughtworks.xstream.converters.ConversionException;

public class ResultsReader implements SideEffect1<String> {

  public static enum Confidence {
    HIGH, LOW;
  }

  public static enum DetectionStatus {
    KILLED(true, Confidence.HIGH, 4), SURVIVED(false, Confidence.HIGH, 0), TIMED_OUT(
        true, Confidence.LOW, 2), NON_VIABLE(true, Confidence.HIGH, 3), MEMORY_ERROR(
        true, Confidence.LOW, 1), NOT_STARTED(false, Confidence.LOW, 1), STARTED(
        false, Confidence.LOW, 1), RUN_ERROR(true, Confidence.LOW, 0);

    private final boolean    detected;
    private final Confidence confidence;
    private final int        ranking;

    DetectionStatus(final boolean detected, final Confidence confidence,
        final int ranking) {
      this.detected = detected;
      this.confidence = confidence;
      this.ranking = ranking;
    }

    public boolean isDetected() {
      return this.detected;
    }

    public Confidence getConfidence() {
      return this.confidence;
    }

    public int getRanking() {
      return this.ranking;
    }
  };

  public static class MutationResult {

    public MutationResult(final MutationDetails md, final DetectionStatus status) {
      this.details = md;
      this.status = status;
    }

    public final MutationDetails details;
    public final DetectionStatus status;
  }

  // private MutationIdentifier lastRunMutation;
  private Option<Statistics>                             stats;
  // store as map rather than list to allow possibility of out of order mutation
  // results
  private final Map<MutationIdentifier, DetectionStatus> mutations;

  public ResultsReader(
      final Map<MutationIdentifier, DetectionStatus> allmutations,
      final Option<Statistics> stats) {
    // // this.lastRunMutation = lastRunMutation;
    this.stats = stats;
    this.mutations = allmutations;
  }

  public void apply(final String a) {
    process(a);
  }

  @SuppressWarnings("unchecked")
  private void process(final String line) {
    if (line == null) {
      return;
    }
    if (line.startsWith("STATS=")) {
      try {
        this.stats = (Option<Statistics>) IsolationUtils
            .fromTransportString(line.substring(6, line.length()));
      } catch (final ConversionException ex) {
        ex.printStackTrace();
      }
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
    final MutationIdentifier mutation = extractMutationIndex(parts);
    this.mutations.put(mutation, DetectionStatus.STARTED);

  }

  private MutationIdentifier extractMutationIndex(final String[] parts) {
    return (MutationIdentifier) IsolationUtils.fromTransportString(parts[1]);
  }

  private void receiveMutationResults(final String[] parts) {
    final MutationIdentifier mutation = extractMutationIndex(parts);
    final DetectionStatus value = extractStatus(parts);
    this.mutations.put(mutation, value);

  }

  private DetectionStatus extractStatus(final String[] parts) {
    return DetectionStatus.valueOf(parts[2]);
  }

  public Option<Statistics> getStats() {
    return this.stats;
  }

}
