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

package org.pitest.mutationtest.build;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PercentAndConstantTimeoutStrategyTest {

  private PercentAndConstantTimeoutStrategy testee;

  @Test
  public void shouldReturnProductOfFactorAnTestTimePlusConstant() {
    final long time = 1;
    this.testee = new PercentAndConstantTimeoutStrategy(1.2f, 666);
    assertEquals((Math.round(1.2f * time) + 666),
        this.testee.getAllowedTime(time));

  }
}
