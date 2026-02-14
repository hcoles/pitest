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

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import org.junit.Test;

public class ReflectionTest {

  static class Parent {
    public int first;

    public void foo() {

    }
  }

  static class Child extends Parent {
    public int        second;
    public static int third;
  }

  @Test
  public void allMethodsShouldReturnPublicMethodsDeclaredByParent() {
    final Set<Method> actual = Reflection.allMethods(Child.class);
    final Method expected = Reflection.publicMethod(Parent.class, "foo");
    assertThat(actual).contains(expected);
  }

  @Test
  public void publicFieldsReturnsFieldsDeclaredInParent() throws Exception {
    final Set<Field> actual = Reflection.publicFields(Child.class);
    final Field expected = Parent.class.getField("first");
    assertThat(actual).contains(expected);
  }

  @Test
  public void publicFieldsReturnsFieldsDeclaredInChild() throws Exception {
    final Set<Field> actual = Reflection.publicFields(Child.class);
    final Field expected = Child.class.getField("second");
    assertThat(actual).contains(expected);
  }

  @Test
  public void publicFieldsReturnsStaticFields() throws Exception {
    final Set<Field> actual = Reflection.publicFields(Child.class);
    final Field expected = Child.class.getField("third");
    assertThat(actual).contains(expected);
  }
}
