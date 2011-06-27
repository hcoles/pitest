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
package org.pitest.teststeps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.Collections;

import org.junit.Test;
import org.pitest.Description;
import org.pitest.internal.IsolationUtils;

public class NameStringConstructorInstantiateStepTest {


  public static class OneString {
    public final String s;

    public OneString(final String s) {
      this.s = s;
    }
  }

  @Test
  public void shouldConstructsObjectPassingInNameOfTest() {
    final NameStringConstructorInstantiateStep testee = new NameStringConstructorInstantiateStep(
        OneString.class);
    final OneString actual = (OneString) testee.execute(this.getClass()
        .getClassLoader(),
        new Description("foo", Collections.<Class<?>> emptySet(), null), null);
    assertEquals("foo", actual.s);
  }

  @Test
  public void shouldSerializeAndDeserializeViaXStreamWithoutError()
  throws Exception {
    try {
      final NameStringConstructorInstantiateStep expected = new NameStringConstructorInstantiateStep(
          OneString.class);
      final NameStringConstructorInstantiateStep actual = (NameStringConstructorInstantiateStep) IsolationUtils
      .clone(expected);
      assertSame(expected.getClazz(), actual.getClazz());
    } catch (final Throwable t) {
      fail();
    }
  }

}
