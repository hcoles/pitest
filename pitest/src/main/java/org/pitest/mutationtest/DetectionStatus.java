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
package org.pitest.mutationtest;

import org.pitest.util.ExitCode;

public enum DetectionStatus {
  KILLED(true), SURVIVED(false), TIMED_OUT(true), NON_VIABLE(true), MEMORY_ERROR(
      true), NOT_STARTED(false), STARTED(false), RUN_ERROR(true), NO_COVERAGE(
      false);

  private final boolean detected;

  DetectionStatus(final boolean detected) {
    this.detected = detected;
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

};
