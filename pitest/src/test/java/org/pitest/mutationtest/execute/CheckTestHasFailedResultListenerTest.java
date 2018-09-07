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
package org.pitest.mutationtest.execute;

import static org.junit.Assert.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.pitest.DescriptionMother;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.testapi.Description;
import org.pitest.testapi.TestResult;

public class CheckTestHasFailedResultListenerTest {

  private CheckTestHasFailedResultListener testee;

  private Description                      description;

  @Before
  public void setUp() {
    this.description = DescriptionMother.createEmptyDescription("foo");
  }

  @Test
  public void shouldReturnDetectionStatusOfSurvivedWhenNoFailuresOrErrors() {
    this.testee = new CheckTestHasFailedResultListener(false);
    this.testee.onTestSuccess(new TestResult(this.description, null));
    assertEquals(DetectionStatus.SURVIVED, this.testee.status());
  }

  @Test
  public void shouldReturnDetectionStatusOfKilledWhenEncountersFailure() {
    this.testee = new CheckTestHasFailedResultListener(false);
    this.testee.onTestFailure(new TestResult(this.description, null));
    assertEquals(DetectionStatus.KILLED, this.testee.status());
  }

  @Test
  public void shouldRecordDescriptionOfLastFailingTest() {
    this.testee = new CheckTestHasFailedResultListener(false);
    this.testee.onTestFailure(new TestResult(this.description, null));
    assertEquals(this.description, this.testee.getFailingTests().get(0));
  }

  @Test
  public void shouldRecordNumberOfTestsRun() {
    this.testee = new CheckTestHasFailedResultListener(false);
    assertEquals(0, this.testee.getNumberOfTestsRun());
    this.testee.onTestStart(null);
    assertEquals(1, this.testee.getNumberOfTestsRun());
  }
  
  @Test
  public void shouldNotRecordPassingTestsWhenFlagNotSet() {
    this.testee = new CheckTestHasFailedResultListener(false);
    this.testee.onTestSuccess(new TestResult(this.description, null));
    assertThat(testee.getSucceedingTests()).isEmpty();
  }

  @Test
  public void shouldRecordPassingTestsWhenFlagSet() {
    this.testee = new CheckTestHasFailedResultListener(true);
    this.testee.onTestSuccess(new TestResult(this.description, null));
    assertThat(testee.getSucceedingTests()).hasSize(1);
  }

}

