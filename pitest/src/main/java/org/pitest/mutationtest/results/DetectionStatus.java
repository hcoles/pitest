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
package org.pitest.mutationtest.results;

import org.pitest.util.ExitCode;

public enum DetectionStatus {
  KILLED(true, 4), SURVIVED(false, 0), TIMED_OUT(true, 2), NON_VIABLE(true, 3), MEMORY_ERROR(
      true, 1), NOT_STARTED(false, 1), STARTED(false, 1), RUN_ERROR(true, 0), NO_COVERAGE(
      false, 0);

  private final boolean detected;
  private final int     ranking;

  DetectionStatus(final boolean detected, final int ranking) {
    this.detected = detected;
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

  public int getRanking() {
    return this.ranking;
  }
};
