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

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ClassInfoTest {

  @Test
  public void shouldIdentifyEnumsFromSuperClass() {
    final ClassInfo testee = new ClassInfo(0, 0, "foo", "", "java/lang/Enum",
        null);
    assertTrue(testee.isEnum());
  }

  @Test
  public void shouldIdentifyGroovyObjects() {
    final ClassInfo testee = new ClassInfo(0, 0, "foo", "", "foo",
        new String[] { "groovy/lang/GroovyObject" });
    assertTrue(testee.isGroovyClass());
  }

  @Test
  public void shouldIdentifyGroovyClosures() {
    final ClassInfo testee = new ClassInfo(0, 0, "foo", "",
        "groovy.lang.Closure",
        new String[] { "org/codehaus/groovy/runtime/GeneratedClosure" });
    assertTrue(testee.isGroovyClass());
  }

}
