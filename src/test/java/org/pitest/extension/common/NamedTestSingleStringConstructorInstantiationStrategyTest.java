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
package org.pitest.extension.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pitest.extension.TestStep;
import org.pitest.teststeps.NameStringConstructorInstantiateStep;

public class NamedTestSingleStringConstructorInstantiationStrategyTest {

  private NamedTestSingleStringConstructorInstantiationStrategy testee;

  @Before
  public void setUp() {
    this.testee = new NamedTestSingleStringConstructorInstantiationStrategy();
  }

  @Test
  public void testCanInstantiateReturnsFalseIfSingleStringConstructorNotPresent() {
    assertFalse(this.testee
        .canInstantiate(NamedTestSingleStringConstructorInstantiationStrategyTest.class));
  }

  public static class StringConstructor {
    public StringConstructor(final String s) {

    }
  }

  @Test
  public void testCanInstantiateReturnsTrueIfSingleStringConstructorPresent() {
    assertTrue(this.testee.canInstantiate(StringConstructor.class));
  }

  @Test
  public void testReturnsSingleNameStringConstructorInstantiateStepTest() {
    final List<TestStep> expected = new ArrayList<TestStep>();
    expected.add(new NameStringConstructorInstantiateStep(
        StringConstructor.class));
    assertEquals(expected, this.testee.instantiations(StringConstructor.class));

  }
}
