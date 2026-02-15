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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.coverage.codeassist.ClassUtils;
import java.util.Optional;
import org.pitest.util.IsolationUtils;

public class RepositoryTest {

  private Repository           testee;

  @Mock
  private ClassByteArraySource source;

  @Mock
  private HashFunction         hashFunction;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    this.testee = new Repository(new ClassloaderByteArraySource(
        IsolationUtils.getContextClassLoader()), this.hashFunction);
  }

  @Test
  public void shouldReturnTrueWhenAskedForKnownClass() {
    assertThat(this.testee.hasClass(ClassName.fromClass(Integer.class))).isTrue();
  }

  @Test
  public void shouldReturnFalseWhenAskedForUnknownClass() {
    assertThat(this.testee.hasClass(ClassName.fromString("never.heard.of.you"))).isFalse();
  }

  @Test
  public void shouldOnlyCheckSourceForUnknownClassesOnce() {
    this.testee = new Repository(this.source);
    when(this.source.getBytes(anyString())).thenReturn(Optional.<byte[]> empty());
    this.testee.hasClass(ClassName.fromString("foo"));
    this.testee.hasClass(ClassName.fromString("foo"));
    verify(this.source, times(1)).getBytes("foo");
  }

  @Test
  public void shouldReturnNoneWhenAskedForUnknownClass() {
    assertThat(this.testee.fetchClass(ClassName.fromString("never.heard.of.you"))).isEmpty();
  }

  @Test
  public void shouldOnlyLookForUnknownClassesOnce() {
    this.testee = new Repository(this.source);
    when(this.source.getBytes(anyString())).thenReturn(Optional.<byte[]> empty());
    this.testee.fetchClass(ClassName.fromString("foo"));
    this.testee.fetchClass(ClassName.fromString("foo"));
    verify(this.source, times(1)).getBytes("foo");
  }

  @Test
  public void shouldOnlyQuerySourceForAnUnknownClassOnce() {
    this.testee = new Repository(this.source);
    when(this.source.getBytes(anyString())).thenReturn(Optional.<byte[]> empty());
    this.testee.hasClass(ClassName.fromString("foo"));
    this.testee.fetchClass(ClassName.fromString("foo"));
    verify(this.source, times(1)).getBytes("foo");
  }

  @Test
  public void shouldReturnInfoForClassOnClassPath() {
    assertThat(this.testee.fetchClass(Integer.class)).isPresent();
  }

  @Test
  public void shouldOnlyLookForKnownClassOnce() throws ClassNotFoundException {
    this.testee = new Repository(this.source);
    when(this.source.getBytes(anyString())).thenReturn(
        Optional.ofNullable(ClassUtils.classAsBytes(String.class)));
    this.testee.fetchClass(ClassName.fromString("foo"));
    this.testee.fetchClass(ClassName.fromString("foo"));
    verify(this.source, times(1)).getBytes("foo");
  }

  public static class SimpleInnerClass {

  }

  @Test
  public void shouldReportOuterClassForStaticInnerClasses() {
    final String actual = getOuterClassNameFor(SimpleInnerClass.class);
    assertThat(actual).isEqualTo(RepositoryTest.class.getName().replace(".", "/"));
  }

  @Test
  public void shouldReportOuterClassForLocalClasses() {
    final Object local = new Object() {

    };

    final String actual = getOuterClassNameFor(local.getClass());
    assertThat(actual).isEqualTo(RepositoryTest.class.getName().replace(".", "/"));
  }

  public class NonStaticInnerClass {

  }

  @Test
  public void shouldReportOuterClassForNonStaticInnerClasses() {
    final String actual = getOuterClassNameFor(NonStaticInnerClass.class);
    assertThat(actual).isEqualTo(RepositoryTest.class.getName().replace(".", "/"));
  }

  public static class OuterStaticInnerClass {
    public static class InnerStaticClass {

    }
  }

  @Test
  public void shouldReportInnermstOuterClassForNestedInnerClasses() {
    final String actual = getOuterClassNameFor(OuterStaticInnerClass.InnerStaticClass.class);
    assertThat(actual).isEqualTo(
        RepositoryTest.OuterStaticInnerClass.class.getName().replace(".", "/"));
  }

  static class Foo {

  }

  static class Bar extends Foo {

  }

  @Test
  public void shouldReportSuperClass() {
    final Optional<ClassInfo> aClass = this.testee.fetchClass(Bar.class);
    assertThat(aClass.get().getSuperClass().get().getName()).isEqualTo(ClassName.fromClass(Foo.class));
  }

  @Test
  public void shouldReportSuperClassAsObjectWhenNoneDeclared() {
    final Optional<ClassInfo> aClass = this.testee.fetchClass(Foo.class);
    assertThat(aClass.get().getSuperClass().get().getName()).isEqualTo(ClassName.fromClass(Object.class));
  }

  @Test
  public void shouldReportNoSuperClassForObject() {
    final Optional<ClassInfo> aClass = this.testee.fetchClass(Object.class);
    assertThat(aClass.get().getSuperClass()).isEmpty();
  }

  interface ITop {

  }

  static class Top implements ITop {

  }

  static class Middle extends Top {

  }

  static class Bottom extends Middle {

  }

  @Test
  public void shouldCorrectlyNegotiateClassHierarchies() {
    final Optional<ClassInfo> aClass = this.testee.fetchClass(Bottom.class);
    assertThat(aClass.get().descendsFrom(Middle.class)).isTrue();
    assertThat(aClass.get().descendsFrom(Top.class)).isTrue();
    assertThat(aClass.get().descendsFrom(Object.class)).isTrue();
    assertThat(aClass.get().descendsFrom(String.class)).isFalse();
  }

  @Test
  public void doesNotTreatInterfacesAsPartOfClassHierarchy() {
    final Optional<ClassInfo> aClass = this.testee.fetchClass(Bottom.class);
    assertThat(aClass.get().descendsFrom(ITop.class)).isFalse();
  }

  @Test
  public void shouldCalculateHashForSuppledClass() {
    this.testee.fetchClass(String.class);
    verify(this.hashFunction).hash(any(byte[].class));
  }

  private String getOuterClassNameFor(final Class<?> clazz) {
    return this.testee.fetchClass(clazz).get().getOuterClass().get()
        .getName().asInternalName();
  }

}