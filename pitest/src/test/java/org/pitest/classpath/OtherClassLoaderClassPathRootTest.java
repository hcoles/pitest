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
package org.pitest.classpath;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

public class OtherClassLoaderClassPathRootTest {

  private OtherClassLoaderClassPathRoot testee;

  @Before
  public void setup() {
    this.testee = new OtherClassLoaderClassPathRoot(Thread.currentThread()
        .getContextClassLoader());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getClassNamesShouldThrowUnsupportedOperation() {
    this.testee.classNames();
  }

  @Test
  public void shouldReturnsBytesForClassesVisibleToParentLoader()
      throws Exception {
    assertNotNull(this.testee.getData(OtherClassLoaderClassPathRootTest.class
        .getName()));
    assertNotNull(Test.class.getName());
  }

  @Test
  public void testReturnsNullForClassesNotVisibleToParentLoader()
      throws Exception {
    assertNull(this.testee.getData("FooFoo"));
  }

}
