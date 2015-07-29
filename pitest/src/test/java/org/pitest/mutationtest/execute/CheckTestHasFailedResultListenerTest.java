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

import org.junit.Before;
import org.junit.Test;
import org.pitest.DescriptionMother;
import org.pitest.functional.Option;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.testapi.Description;
import org.pitest.testapi.TestResult;

public class CheckTestHasFailedResultListenerTest {

  private CheckTestHasFailedResultListener testee;

  private Description                      description;

  @Before
  public void setUp() {
    this.testee = new CheckTestHasFailedResultListener();
    this.description = DescriptionMother.createEmptyDescription("foo");
  }

  @Test
  public void shouldReturnDetectionStatusOfSurvivedWhenNoFailuresOrErrors() {
    this.testee.onTestSuccess(new TestResult(this.description, null));
    assertEquals(DetectionStatus.SURVIVED, this.testee.status());
  }

  @Test
  public void shouldReturnDetectionStatusOfKilledWhenEncountersFailure() {
    this.testee.onTestFailure(new TestResult(this.description, null));
    assertEquals(DetectionStatus.KILLED, this.testee.status());
  }

  @Test
  public void shouldRecordDescriptionOfLastFailingTest() {
    this.testee.onTestFailure(new TestResult(this.description, null));
    assertEquals(Option.some(this.description), this.testee.lastFailingTest());
  }

  @Test
  public void shouldRecordNumberOfTestsRun() {
    assertEquals(0, this.testee.getNumberOfTestsRun());
    this.testee.onTestStart(null);
    assertEquals(1, this.testee.getNumberOfTestsRun());
  }
}
