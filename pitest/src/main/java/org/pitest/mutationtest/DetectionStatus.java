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
 * The detection status of a mutant. Equality is defined based on
 * the kind of detection (e.g. KILLED, SURVIVED, etc.) and does not
 * take into account extra information collected during testing —
 * such as stack trace.
 */
public class DetectionStatus implements Comparable<DetectionStatus> {

  private ActualStatus actualStatus;
  private String stackTrace;

  public DetectionStatus() {
  }

  public DetectionStatus(DetectionStatus status) {
    this.setActualStatus(status.getActualStatus());
  }

  public enum ActualStatus {
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
     * JVM ran out of file descriptors while processing a mutation. Might
     * indicate that the mutation increases file usage or leads to file resource
     * leaks but we don't know for sure.
     */
    TOO_MANY_FILES(true),

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

    ActualStatus(final boolean detected) {
      this.detected = detected;
    }
  }

  public static final DetectionStatus KILLED = new DetectionStatus();
  public static final DetectionStatus SURVIVED = new DetectionStatus();
  public static final DetectionStatus TIMED_OUT = new DetectionStatus();
  public static final DetectionStatus MEMORY_ERROR = new DetectionStatus();
  public static final DetectionStatus TOO_MANY_FILES = new DetectionStatus();
  public static final DetectionStatus NOT_STARTED = new DetectionStatus();
  public static final DetectionStatus STARTED = new DetectionStatus();
  public static final DetectionStatus RUN_ERROR = new DetectionStatus();
  public static final DetectionStatus NO_COVERAGE = new DetectionStatus();
  public static final DetectionStatus NON_VIABLE = new DetectionStatus();

  static {
    KILLED.setActualStatus(ActualStatus.KILLED);
    SURVIVED.setActualStatus(ActualStatus.SURVIVED);
    TIMED_OUT.setActualStatus(ActualStatus.TIMED_OUT);
    MEMORY_ERROR.setActualStatus(ActualStatus.MEMORY_ERROR);
    TOO_MANY_FILES.setActualStatus(ActualStatus.TOO_MANY_FILES);
    NOT_STARTED.setActualStatus(ActualStatus.NOT_STARTED);
    STARTED.setActualStatus(ActualStatus.STARTED);
    RUN_ERROR.setActualStatus(ActualStatus.RUN_ERROR);
    NO_COVERAGE.setActualStatus(ActualStatus.NO_COVERAGE);
    NON_VIABLE.setActualStatus(ActualStatus.NON_VIABLE);
  }

  public static final DetectionStatus[] VALUES = new DetectionStatus[]
      {KILLED, SURVIVED, TIMED_OUT, MEMORY_ERROR, NOT_STARTED, TOO_MANY_FILES,
          STARTED, RUN_ERROR, NO_COVERAGE, NON_VIABLE};

  public static DetectionStatus[] values() {
    return VALUES;
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
      return MEMORY_ERROR;
    } else if (exitCode.equals(ExitCode.TOO_MANY_FILES)) {
      return TOO_MANY_FILES;
    } else if (exitCode.equals(ExitCode.TIMEOUT)) {
      return TIMED_OUT;
    } else {
      return RUN_ERROR;
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
    return this.getActualStatus().detected;
  }

  public String name() {
    return getActualStatus().name();
  }

  public ActualStatus getActualStatus() {
    return actualStatus;
  }

  public void setActualStatus(ActualStatus actualStatus) {
    this.actualStatus = actualStatus;
  }

  public String getStackTrace() {
    return stackTrace;
  }

  public void setStackTrace(String stackTrace) {
    this.stackTrace = stackTrace;
  }

  @Override
  public String toString() {
    return "" + getActualStatus();
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || !(o instanceof DetectionStatus)) {
      return false;
    }
    if (getActualStatus() == null
        ^ ((DetectionStatus) o).getActualStatus() == null) {
      return false;
    }
    if (getActualStatus() == null) {
      return true;
    }
    return getActualStatus().equals(((DetectionStatus) o).getActualStatus());
  }

  @Override
  public int hashCode() {
    if (getActualStatus() == null) {
      return 0;
    }
    return getActualStatus().hashCode();
  }

  @Override
  public int compareTo(DetectionStatus o) {
    return getActualStatus().compareTo(o.getActualStatus());
  }

};
