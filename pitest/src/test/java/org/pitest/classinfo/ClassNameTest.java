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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;
import org.pitest.functional.Option;

public class ClassNameTest {

  @Test
  public void shouldConvertJavaNamesToInternalNames() {
    final ClassName testee = new ClassName("com.foo.bar");
    assertEquals("com/foo/bar", testee.asInternalName());
  }

  @Test
  public void shouldConvertInternalNamesToJavaNames() {
    final ClassName testee = new ClassName("com/foo/bar");
    assertEquals("com.foo.bar", testee.asJavaName());
  }

  @Test
  public void shouldTreatSameClassNameAsEqual() {
    final ClassName left = new ClassName("com/foo/bar");
    final ClassName right = new ClassName("com.foo.bar");
    assertTrue(left.equals(right));
    assertTrue(right.equals(left));
  }

  @Test
  public void shouldDisplayJavaNameInToString() {
    final ClassName testee = new ClassName("com/foo/bar");
    assertEquals("com.foo.bar", testee.toString());
  }

  @Test
  public void getNameWithoutPackageShouldReturnNameOnlyWhenClassIsOuterClass() {
    assertEquals(new ClassName("String"),
        new ClassName(String.class).getNameWithoutPackage());
  }

  static class Foo {

  }

  @Test
  public void getNameWithoutPackageShouldReturnNameWhenClassIsInnerClass() {
    assertEquals(new ClassName("ClassNameTest$Foo"),
        new ClassName(Foo.class).getNameWithoutPackage());
  }

  @Test
  public void getNameWithoutPackageShouldReturnNameWhenClassInPackageDefault() {
    assertEquals(new ClassName("Foo"),
        new ClassName("Foo").getNameWithoutPackage());
  }

  @Test
  public void getPackageShouldReturnEmptyPackageWhenClassInPackageDefault() {
    assertEquals(new ClassName(""), new ClassName("Foo").getPackage());
  }

  @Test
  public void getPackageShouldReturnPackageWhenClassWithinAPackage() {
    assertEquals(new ClassName("org.pitest.classinfo"), new ClassName(
        ClassNameTest.class).getPackage());
  }

  @Test
  public void withoutSuffixCharsShouldReturnPacakgeAndClassWithoutSuffixChars() {
    assertEquals(new ClassName("com.example.Foo"), new ClassName(
        "com.example.FooTest").withoutSuffixChars(4));
  }

  @Test
  public void withoutPrefeixCharsShouldReturnPacakgeAndClassWithoutPrefixChars() {
    assertEquals(new ClassName("com.example.Foo"), new ClassName(
        "com.example.TestFoo").withoutPrefixChars(4));
  }

  @Test
  public void shouldSortByName() {
    final ClassName a = ClassName.fromString("a.a.c");
    final ClassName b = ClassName.fromString("a.b.c");
    final ClassName c = ClassName.fromString("b.a.c");

    final List<ClassName> actual = Arrays.asList(b, c, a);
    Collections.sort(actual);
    assertEquals(Arrays.asList(a, b, c), actual);
  }

  @Test
  public void shouldConvertStringToClassName() {
    assertEquals(ClassName.fromString("foo"), ClassName.stringToClassName()
        .apply("foo"));
  }

  @Test
  public void shouldProduceSameHashCodeForSameClass() {
    assertEquals(ClassName.fromString("org/example/Foo").hashCode(), ClassName
        .fromString("org.example.Foo").hashCode());
  }

  @Test
  public void shouldProduceDifferentHashCodeForDifferentClasses() {
    assertFalse(ClassName.fromString("org/example/Foo").hashCode() == ClassName
        .fromString("org.example.Bar").hashCode());
  }

  @Test
  public void shouldTreatSameClassAsEqual() {
    assertEquals(ClassName.fromString("org/example/Foo"),
        ClassName.fromString("org.example.Foo"));
  }

  @Test
  public void shouldTreatDifferentClassesAsNotEqual() {
    assertFalse(ClassName.fromString("org/example/Foo").equals(
        ClassName.fromString("org.example.Bar")));
  }

  @Test
  public void nameToClassShouldReturnClassWhenKnownToLoader() {
    assertEquals(Option.some(String.class),
        ClassName.nameToClass().apply(new ClassName("java.lang.String")));
  }

  @Test
  public void stringToClassShouldReturnNoneWhenClassNotKnownToLoader() {
    assertEquals(Option.none(),
        ClassName.nameToClass().apply(new ClassName("org.unknown.Unknown")));
  }

  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(ClassName.class).verify();
  }

}
