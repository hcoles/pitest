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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.pitest.util.ExitCode;

public class DetectionStatusTest {

  @Test
  public void shouldConsiderKilledMutationsToBeDetected() {
    assertThat(DetectionStatus.KILLED.isDetected()).isTrue();
  }

  @Test
  public void shouldConsiderSurvivingMutationsToBeUnDetected() {
    assertThat(DetectionStatus.SURVIVED.isDetected()).isFalse();
  }

  @Test
  public void shouldConsiderTimedOutMutationsToBeDetected() {
    assertThat(DetectionStatus.TIMED_OUT.isDetected()).isTrue();
  }

  @Test
  public void shouldConsiderNonViableMutationsToBeDetected() {
    assertThat(DetectionStatus.NON_VIABLE.isDetected()).isTrue();
  }

  @Test
  public void shouldConsiderMemoryErrorsAsDetected() {
    assertThat(DetectionStatus.MEMORY_ERROR.isDetected()).isTrue();
  }

  @Test
  public void shouldConsiderUnAnalysedMutationsAsUnDetected() {
    assertThat(DetectionStatus.NOT_STARTED.isDetected()).isFalse();
  }

  @Test
  public void shouldConsiderUnfinishedAnalysAsUnDetected() {
    assertThat(DetectionStatus.STARTED.isDetected()).isFalse();
  }

  @Test
  public void shouldConsiderRunErrorAsDetected() {
    // really? dubious
    assertThat(DetectionStatus.RUN_ERROR.isDetected()).isTrue();
  }

  @Test
  public void shouldConsiderUnCoveredKilledMutationsToBeUnDetected() {
    assertThat(DetectionStatus.NO_COVERAGE.isDetected()).isFalse();
  }

  @Test
  public void shouldConverytutOfMemoryExitCode() {
    assertThat(DetectionStatus.getForErrorExitCode(ExitCode.OUT_OF_MEMORY))
        .isEqualTo(DetectionStatus.MEMORY_ERROR);
  }

  @Test
  public void shouldConvertTimeOutExitCode() {
    assertThat(DetectionStatus.getForErrorExitCode(ExitCode.TIMEOUT))
        .isEqualTo(DetectionStatus.TIMED_OUT);
  }

  @Test
  public void shouldConvertUnknownErrorExitCode() {
    assertThat(DetectionStatus.getForErrorExitCode(ExitCode.UNKNOWN_ERROR))
        .isEqualTo(DetectionStatus.RUN_ERROR);
  }

}
