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

package com.example;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MultipleSurvivorsInSingleLineTest {

  @Test
  public void testSurvivingLine() {
    final MultipleSurvivorsInSingleLine testee = new MultipleSurvivorsInSingleLine();

    // called without assertion => surviving
    testee.coveredSurvivor1a();
    testee.coveredSurvivor1b();
    testee.coveredSurvivor3();

    // called with assertion => killed
    assertEquals(1, testee.killed1());
    assertEquals(5, testee.killed5());
  }
}
