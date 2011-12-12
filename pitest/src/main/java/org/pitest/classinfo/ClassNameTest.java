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
package org.pitest.classinfo;

import static org.junit.Assert.*;

import org.junit.Test;


public class ClassNameTest {

  @Test
  public void shouldConvertJavaNamesToInternalNames() {
    ClassName testee = new ClassName("com.foo.bar");
    assertEquals("com/foo/bar", testee.asInternalName());
  }

  @Test
  public void shouldConvertInternalNamesToJavaNames() {
    ClassName testee = new ClassName("com/foo/bar");
    assertEquals("com.foo.bar", testee.asJavaName());
  }

  @Test
  public void shouldTreatSameClassNameAsEqual() {
    ClassName left = new ClassName("com/foo/bar");
    ClassName right = new ClassName("com.foo.bar");
    assertTrue(left.equals(right));
    assertTrue(right.equals(left));
  }


}
