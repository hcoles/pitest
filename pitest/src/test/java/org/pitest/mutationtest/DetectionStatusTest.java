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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pitest.util.ExitCode;

public class DetectionStatusTest {

  @Test
  public void shouldConsiderKilledMutationsToBeDetected() {
    assertTrue(DetectionStatus.KILLED.isDetected());
  }

  @Test
  public void shouldConsiderSurvivingMutationsToBeUnDetected() {
    assertFalse(DetectionStatus.SURVIVED.isDetected());
  }

  @Test
  public void shouldConsiderTimedOutMutationsToBeDetected() {
    assertTrue(DetectionStatus.TIMED_OUT.isDetected());
  }

  @Test
  public void shouldConsiderNonViableMutationsToBeDetected() {
    assertTrue(DetectionStatus.NON_VIABLE.isDetected());
  }

  @Test
  public void shouldConsiderMemoryErrorsAsDetected() {
    assertTrue(DetectionStatus.MEMORY_ERROR.isDetected());
  }

  @Test
  public void shouldConsiderUnAnalysedMutationsAsUnDetected() {
    assertFalse(DetectionStatus.NOT_STARTED.isDetected());
  }

  @Test
  public void shouldConsiderUnfinishedAnalysAsUnDetected() {
    assertFalse(DetectionStatus.STARTED.isDetected());
  }

  @Test
  public void shouldConsiderRunErrorAsDetected() {
    // really? dubious
    assertTrue(DetectionStatus.RUN_ERROR.isDetected());
  }

  @Test
  public void shouldConsiderUnCoveredKilledMutationsToBeUnDetected() {
    assertFalse(DetectionStatus.NO_COVERAGE.isDetected());
  }

  @Test
  public void shouldConverytutOfMemoryExitCode() {
    assertEquals(DetectionStatus.MEMORY_ERROR,
        DetectionStatus.getForErrorExitCode(ExitCode.OUT_OF_MEMORY));
  }

  @Test
  public void shouldConvertTimeOutExitCode() {
    assertEquals(DetectionStatus.TIMED_OUT,
        DetectionStatus.getForErrorExitCode(ExitCode.TIMEOUT));
  }

  @Test
  public void shouldConvertUnknownErrorExitCode() {
    assertEquals(DetectionStatus.RUN_ERROR,
        DetectionStatus.getForErrorExitCode(ExitCode.UNKNOWN_ERROR));
  }
}
