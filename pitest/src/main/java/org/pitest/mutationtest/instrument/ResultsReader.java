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

import org.pitest.mutationtest.MutationDetails;
import org.pitest.util.ExitCode;

public class ResultsReader  {

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

    public static DetectionStatus getForErrorExitCode(final ExitCode exitCode) {
      if (exitCode.equals(ExitCode.OUT_OF_MEMORY)) {
        return DetectionStatus.MEMORY_ERROR;
      } else if (exitCode.equals(ExitCode.TIMEOUT)) {
        return DetectionStatus.TIMED_OUT;

      } else {
        return DetectionStatus.RUN_ERROR;
      }
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


}
