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

import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pitest.internal.TestClass;

public class IsSubclassOfTest {

  private IsSubclassOf testee;

  @Before
  public void setup() {
    this.testee = new IsSubclassOf(Collection.class);
  }

  @Test
  public void testReturnsTrueWhenGivenClassIsASubclass() {
    assertTrue(this.testee.apply(new TestClass(List.class)));
  }

  @Test
  public void testReturnsFalseWhenGivenClassIsNotASubclass() {
    assertFalse(this.testee.apply(new TestClass(Integer.class)));
  }

}
