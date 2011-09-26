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
package org.pitest.reflection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

public class IsStaticTest {

  private IsStatic testee;

  @Before
  public void setUp() {
    this.testee = new IsStatic();
  }

  public static void staticMethod() {

  }

  @Test
  public void shouldReturnTrueIfIsStatic() {
    final Method method = Reflection.publicMethod(IsStaticTest.class,
        "staticMethod");
    assertTrue(this.testee.apply(method));
  }

  @Test
  public void shouldReturnFalseIfIsNotStatic() {
    final Method method = Reflection.publicMethod(IsStaticTest.class,
        "shouldReturnFalseIfIsNotStatic");
    assertFalse(this.testee.apply(method));
  }
}
