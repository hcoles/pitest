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
package org.pitest.util;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class FunctionsTest {

  @Test
  public void classToNameShouldReturnClassName() {
    assertThat(Functions.classToName().apply(String.class)).isEqualTo(String.class.getName());
  }

  @Test
  public void startWithShouldReturnTrueIfGivenStringStartsWithParameter() {
    assertThat(Functions.startsWith("foo").test("foobar")).isTrue();
  }

  @Test
  public void startWithShouldReturnFalseIfGivenStringDoesNotStartsWithParameter() {
    assertThat(Functions.startsWith("foo").test("barfoo")).isFalse();
  }

  static enum Foo {
    A, B, C, Bar;
  };

  @Test
  public void stringToEnumShouldConvertStringsToEnumValues() {
    assertThat(Functions.stringToEnum(Foo.class).apply("A")).isEqualTo(Foo.A);
    assertThat(Functions.stringToEnum(Foo.class).apply("Bar")).isEqualTo(Foo.Bar);
  }

  @Test
  public void classNameToJVMClassNameShouldConvertDotsToSlashes() {
    assertThat(Functions.classNameToJVMClassName().apply("a.b.c")).isEqualTo("a/b/c");
  }

  @Test
  public void jvmClassToClassNameShouldConvertSlashesToDots() {
    assertThat(Functions.jvmClassToClassName().apply("a/b/c")).isEqualTo("a.b.c");
  }

}
