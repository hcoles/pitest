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
package org.pitest.dependency;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.Test;
import org.pitest.classpath.ClassPathByteArraySource;

public class DependencyExtractorTest {

  private DependencyExtractor testee;

  public static class Foo {
    public void one() {
      new Bar();
    }

    public void two() {
      new Car();
    }
  }

  public static class Bar {
    Far f = new Far();
  }

  public static class Car {

  }

  public static class Far {
    VeryFar f = new VeryFar();
  }

  public static class VeryFar {

  }

  public static class CyclicFoo {
    CyclicBar b = new CyclicBar();
  }

  public static class CyclicBar {
    CyclicFoo f = new CyclicFoo();
  }

  @Test
  public void shouldFindOnlyImmediateDependenciesWhenDepthIsOne()
      throws Exception {
    constructWithDepthOf(1);
    final Collection<String> actual = this.testee
        .extractCallDependenciesForPackages(Foo.class.getName(),
            s -> true);
    final Set<String> expected = asSet(classToJvmName(Bar.class),
        classToJvmName(Car.class));
    assertCollectionEquals(expected, actual);
  }

  @Test
  public void shouldTraverseTwoLevelsOfDependenciesWhenDepthIsTwo()
      throws Exception {
    constructWithDepthOf(2);
    final Collection<String> actual = this.testee
        .extractCallDependenciesForPackages(Foo.class.getName(),
            s -> true);
    final Set<String> expected = asSet(classToJvmName(Bar.class),
        classToJvmName(Car.class), classToJvmName(Far.class));
    assertCollectionEquals(expected, actual);
  }

  @Test
  public void shouldTraverseUnboundedWhenDepthIsZero() throws Exception {
    constructWithDepthOf(0);
    final Collection<String> actual = this.testee
        .extractCallDependenciesForPackages(Foo.class.getName(),
            s -> true);
    final List<String> expected = Arrays.asList(classToJvmName(Bar.class),
        classToJvmName(Car.class), classToJvmName(Far.class),
        classToJvmName(VeryFar.class));
    assertCollectionEquals(expected, actual);
  }

  @Test
  public void shouldNotPickUpDependenciesFromFilteredMethods() throws Exception {
    constructWithDepthOf(0);
    final Collection<String> actual = this.testee.extractCallDependencies(
        Foo.class.getName(), excludeMethodsCalledOne());
    final Set<String> expected = asSet(classToJvmName(Car.class));
    assertCollectionEquals(expected, actual);
  }

  @Test
  public void shouldFindDependenciesReachedViaClassesNotMatchingFilter()
      throws IOException {
    constructWithDepthOf(5);
    final Collection<String> actual = this.testee
        .extractCallDependenciesForPackages(Foo.class.getName(),
            includeOnlyThingsCalled("VeryFar"), ignoreCoreClasses());
    final Set<String> expected = asSet(classToJvmName(VeryFar.class));
    assertCollectionEquals(expected, actual);
  }

  @Test
  public void shouldHandleCyclicDependencies() throws Exception {
    constructWithDepthOf(0);
    final Collection<String> actual = this.testee
        .extractCallDependenciesForPackages(CyclicFoo.class.getName(),
            s -> true);
    final List<String> expected = Arrays
        .asList(classToJvmName(CyclicBar.class));
    assertCollectionEquals(expected, actual);
  }

  private Predicate<DependencyAccess> ignoreCoreClasses() {
    return a -> !a.getDest().getOwner().startsWith("java");

  }

  private Predicate<String> includeOnlyThingsCalled(final String subString) {
    return a -> a.contains(subString);
  }

  private void constructWithDepthOf(final int depth) {
    this.testee = new DependencyExtractor(new ClassPathByteArraySource(), depth);

  }

  private Predicate<DependencyAccess> excludeMethodsCalledOne() {
    return a -> !a.getSource().getName().equals("one");
  }

  private void assertCollectionEquals(final Collection<String> expected,
      final Collection<String> actual) {
    final Set<String> expectedSet = new HashSet<>();
    expectedSet.addAll(expected);

    final Set<String> actualSet = new HashSet<>();
    actualSet.addAll(actual);

    assertEquals(expectedSet, actualSet);

  }

  private Set<String> asSet(final String... values) {
    final Set<String> set = new HashSet<>();
    set.addAll(Arrays.asList(values));
    return set;
  }

  private String classToJvmName(final Class<?> clazz) {

    return clazz.getName().replace(".", "/");

  }
}
