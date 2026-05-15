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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class ClassNameTest {

  @Test
  public void shouldConvertJavaNamesToInternalNames() {
    final ClassName testee = ClassName.fromString("com.foo.bar");
    assertThat(testee.asInternalName()).isEqualTo("com/foo/bar");
  }

  @Test
  public void shouldConvertInternalNamesToJavaNames() {
    final ClassName testee = ClassName.fromString("com/foo/bar");
    assertThat(testee.asJavaName()).isEqualTo("com.foo.bar");
  }

  @Test
  public void shouldTreatSameClassNameAsEqual() {
    final ClassName left = ClassName.fromString("com/foo/bar");
    final ClassName right = ClassName.fromString("com.foo.bar");
    assertThat(left.equals(right)).isTrue();
    assertThat(right.equals(left)).isTrue();
  }

  @Test
  public void shouldDisplayJavaNameInToString() {
    final ClassName testee = ClassName.fromString("com/foo/bar");
    assertThat(testee.toString()).isEqualTo("com.foo.bar");
  }

  @Test
  public void getNameWithoutPackageShouldReturnNameOnlyWhenClassIsOuterClass() {
    assertThat(ClassName.fromClass(String.class).getNameWithoutPackage()).isEqualTo(ClassName.fromString("String"));
  }

  static class Foo {

  }

  @Test
  public void getNameWithoutPackageShouldReturnNameWhenClassIsInnerClass() {
    assertThat(ClassName.fromClass(Foo.class).getNameWithoutPackage()).isEqualTo(ClassName.fromString("ClassNameTest$Foo"));
  }

  @Test
  public void getNameWithoutPackageShouldReturnNameWhenClassInPackageDefault() {
    assertThat(ClassName.fromString("Foo").getNameWithoutPackage()).isEqualTo(ClassName.fromString("Foo"));
  }

  @Test
  public void getPackageShouldReturnEmptyPackageWhenClassInPackageDefault() {
    assertThat(ClassName.fromString("Foo").getPackage()).isEqualTo(ClassName.fromString(""));
  }

  @Test
  public void getPackageShouldReturnPackageWhenClassWithinAPackage() {
    assertThat(ClassName.fromClass(ClassNameTest.class).getPackage()).isEqualTo(ClassName.fromString("org.pitest.classinfo"));
  }

  @Test
  public void withoutSuffixCharsShouldReturnPacakgeAndClassWithoutSuffixChars() {
    assertThat(ClassName.fromString("com.example.FooTest").withoutSuffixChars(4)).isEqualTo(ClassName.fromString("com.example.Foo"));
  }

  @Test
  public void withoutPrefeixCharsShouldReturnPacakgeAndClassWithoutPrefixChars() {
    assertThat(ClassName.fromString("com.example.TestFoo").withoutPrefixChars(4)).isEqualTo(ClassName.fromString("com.example.Foo"));
  }

  @Test
  public void shouldSortByName() {
    final ClassName a = ClassName.fromString("a.a.c");
    final ClassName b = ClassName.fromString("a.b.c");
    final ClassName c = ClassName.fromString("b.a.c");

    final List<ClassName> actual = Arrays.asList(b, c, a);
    Collections.sort(actual);
    assertThat(actual).isEqualTo(Arrays.asList(a, b, c));
  }

  @Test
  public void shouldProduceSameHashCodeForSameClass() {
    assertThat(ClassName.fromString("org/example/Foo").hashCode()).isEqualTo(ClassName.fromString("org.example.Foo").hashCode());
  }

  @Test
  public void shouldProduceDifferentHashCodeForDifferentClasses() {
    assertThat(ClassName.fromString("org/example/Foo").hashCode()).isNotEqualTo(ClassName.fromString("org.example.Bar").hashCode());
  }

  @Test
  public void shouldTreatSameClassAsEqual() {
    assertThat(ClassName.fromString("org/example/Foo")).isEqualTo(ClassName.fromString("org.example.Foo"));
  }

  @Test
  public void shouldTreatDifferentClassesAsNotEqual() {
    assertThat(ClassName.fromString("org/example/Foo")).isNotEqualTo(ClassName.fromString("org.example.Bar"));
  }

  @Test
  public void nameToClassShouldReturnClassWhenKnownToLoader() {
    assertThat(ClassName.nameToClass().apply(ClassName.fromString("java.lang.String")))
    .contains(String.class);
  }

  @Test
  public void stringToClassShouldReturnEmptyWhenClassNotKnownToLoader() {
    assertThat(ClassName.nameToClass()
        .apply(ClassName.fromString("org.unknown.Unknown")))
    .isEmpty();
  }

  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(ClassName.class).withNonnullFields("name").verify();
  }

  @Test
  public void shouldUseCachedInstancesForObject() {
    assertThat(ClassName.fromClass(Object.class)).isSameAs(ClassName.fromClass(Object.class));
  }

  @Test
  public void shouldUseCachedInstancesForString() {
    assertThat(ClassName.fromClass(String.class)).isSameAs(ClassName.fromClass(String.class));
  }

  @Test
  public void shouldUseCachedInstancesForInteger() {
    assertThat(ClassName.fromClass(Integer.class)).isSameAs(ClassName.fromClass(Integer.class));
  }

  @Test
  public void shouldUseCachedInstancesForList() {
    assertThat(ClassName.fromClass(List.class)).isSameAs(ClassName.fromClass(List.class));
  }

  @Test
  public void shouldUseCachedInstancesForArrayList() {
    assertThat(ClassName.fromClass(ArrayList.class)).isSameAs(ClassName.fromClass(ArrayList.class));
  }

  @Test
  public void shouldUseCachedInstancesForStream() {
    assertThat(ClassName.fromClass(Stream.class)).isSameAs(ClassName.fromClass(Stream.class));
  }

  @Test
  public void shouldUseCachedInstancesForFunction() {
    assertThat(ClassName.fromClass(Function.class)).isSameAs(ClassName.fromClass(Function.class));
  }

  @Test
  public void shouldUseCachedInstancesForPredicate() {
    assertThat(ClassName.fromClass(Predicate.class)).isSameAs(ClassName.fromClass(Predicate.class));
  }
}
