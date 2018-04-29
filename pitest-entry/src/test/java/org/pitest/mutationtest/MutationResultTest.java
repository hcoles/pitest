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

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class MutationResultTest {

  private MutationResult testee;

  @Test
  public void shouldReturnNameOfKillingTestWhenKnown() {
    this.testee = new MutationResult(null, new MutationStatusTestPair(1,
        DetectionStatus.KILLED, "good test"));
    assertEquals("good test", this.testee.getKillingTestDescription());
  }

  @Test
  public void shouldNoneWhenNoKillingTest() {
    this.testee = new MutationResult(null, new MutationStatusTestPair(1,
        DetectionStatus.TIMED_OUT));
    assertEquals("none", this.testee.getKillingTestDescription());
  }

  @Test
  public void shouldReturnStatusDescription() {
    this.testee = new MutationResult(null, new MutationStatusTestPair(1,
        DetectionStatus.TIMED_OUT));
    assertEquals("TIMED_OUT", this.testee.getStatusDescription());
  }

  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(MutationResult.class).verify();
  }

}
