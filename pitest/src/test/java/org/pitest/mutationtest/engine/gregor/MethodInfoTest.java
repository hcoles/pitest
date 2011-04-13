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
package org.pitest.mutationtest.engine.gregor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.objectweb.asm.Opcodes;

public class MethodInfoTest {

  private MethodInfo testee;

  @Test
  public void isVoidShouldReturnTrueWhenMethodIsVoid() {
    this.testee = new MethodInfo("", 0, "", "()V", "", null);
    assertTrue(this.testee.isVoid());
  }

  @Test
  public void isVoidShouldReturnFalseWhenMethodIsNotVoid() {
    this.testee = new MethodInfo("", 0, "", "()Ljava/lang/String;", "", null);
    assertFalse(this.testee.isVoid());
  }

  @Test
  public void isStaticShouldReturnTrueWhenMethodIsStatic() {
    this.testee = new MethodInfo("", Opcodes.ACC_STATIC, "", "", "", null);
    assertTrue(this.testee.isStatic());
  }

  @Test
  public void isStaticShouldReturnFalseWhenMethodIsNotStatic() {
    this.testee = new MethodInfo("", 0, "", "", "", null);
    assertFalse(this.testee.isStatic());
  }

  @Test
  public void takesNoParametersShouldReturnTrueWhenMethodTakesNoParameters() {
    this.testee = new MethodInfo("", 0, "", "()V", "", null);
    assertTrue(this.testee.takesNoParameters());
  }

  @Test
  public void takesNoParametersShouldReturnFalseWhenMethodTakesNoParameters() {
    this.testee = new MethodInfo("", 0, "", "(Ljava/lang/String;)V", "", null);
    assertFalse(this.testee.takesNoParameters());
  }

}
