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

/**
 * The detection status of a mutant
 */
public enum DetectionStatus {
  /**
   * Mutation was detected by a test
   */
  KILLED(true),

  /**
   * No test failed in the presence of the mutation
   */
  SURVIVED(false),

  /**
   * A test took a long time to run when mutation was present, might indicate an
   * that the mutation caused an infinite loop but we don't know for sure.
   */
  TIMED_OUT(true),

  /**
   * Mutation could not be loaded into the jvm. Should never happen.
   */
  NON_VIABLE(true),

  /**
   * JVM ran out of memory while processing a mutation. Might indicate that the
   * mutation increases memory usage but we don't know for sure.
   */
  MEMORY_ERROR(true),
  /**
   * Mutation not yet assessed. For internal use only.
   */
  NOT_STARTED(false),

  /**
   * Processing of mutation has begun but not yet fully assessed. For internal
   * use only.
   */
  STARTED(false),

  /**
   * Something went wrong. Don't know what but it was probably bad.
   */
  RUN_ERROR(true),

  /**
   * Mutation is not covered by any test.
   */
  NO_COVERAGE(false);

  private final boolean detected;

  DetectionStatus(final boolean detected) {
    this.detected = detected;
  }

  /**
   * Converts a process exit code to a mutation status
   *
   * @param exitCode
   *          Exit code to convert
   * @return The status corresponding to the exit code
   */
  public static DetectionStatus getForErrorExitCode(final ExitCode exitCode) {
    if (exitCode.equals(ExitCode.OUT_OF_MEMORY)) {
      return DetectionStatus.MEMORY_ERROR;
    } else if (exitCode.equals(ExitCode.TIMEOUT)) {
      return DetectionStatus.TIMED_OUT;
    } else {
      return DetectionStatus.RUN_ERROR;
    }
  }

  /**
   * Returns true if this status indicates that the mutation was distinguished
   * from the un-mutated code by the test suite, ignores the slight ambiguity of
   * some of the statuses.
   *
   * @return True if detected, false if not.
   */
  public boolean isDetected() {
    return this.detected;
  }

};
