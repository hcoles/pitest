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
package org.pitest.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TestClassTest {



  @Test
  public void shouldCloneViaXStreamWithoutError() throws Exception {
    try {
      final TestClass testee = new TestClass(TestClassTest.class);
      final TestClass actual = (TestClass) IsolationUtils.clone(testee);
      assertEquals(testee, actual);
    } catch (final Throwable t) {
      fail();
    }
  }

}
