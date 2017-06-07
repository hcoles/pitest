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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.coverage.codeassist.ClassUtils;
import org.pitest.functional.Option;
import org.pitest.util.IsolationUtils;

public class RepositoryTest {

  private Repository           testee;

  @Mock
  private ClassByteArraySource source;

  @Mock
  private HashFunction         hashFunction;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.testee = new Repository(new ClassloaderByteArraySource(
        IsolationUtils.getContextClassLoader()), this.hashFunction);
  }

  @Test
  public void shouldReturnTrueWhenAskedForKnownClass() {
    assertTrue(this.testee.hasClass(ClassName.fromClass(Integer.class)));
  }

  @Test
  public void shouldReturnFalseWhenAskedForUnknownClass() {
    assertFalse(this.testee.hasClass(ClassName.fromString("never.heard.of.you")));
  }

  @Test
  public void shouldOnlyCheckSourceForUnknownClassesOnce() {
    this.testee = new Repository(this.source);
    when(this.source.getBytes(anyString())).thenReturn(Option.<byte[]> none());
    this.testee.hasClass(ClassName.fromString("foo"));
    this.testee.hasClass(ClassName.fromString("foo"));
    verify(this.source, times(1)).getBytes("foo");
  }

  @Test
  public void shouldReturnNoneWhenAskedForUnknownClass() {
    assertEquals(Option.none(),
        this.testee.fetchClass(ClassName.fromString("never.heard.of.you")));
  }

  @Test
  public void shouldOnlyLookForUnknownClassesOnce() {
    this.testee = new Repository(this.source);
    when(this.source.getBytes(anyString())).thenReturn(Option.<byte[]> none());
    this.testee.fetchClass(ClassName.fromString("foo"));
    this.testee.fetchClass(ClassName.fromString("foo"));
    verify(this.source, times(1)).getBytes("foo");
  }

  @Test
  public void shouldOnlyQuerySourceForAnUnknownClassOnce() {
    this.testee = new Repository(this.source);
    when(this.source.getBytes(anyString())).thenReturn(Option.<byte[]> none());
    this.testee.hasClass(ClassName.fromString("foo"));
    this.testee.fetchClass(ClassName.fromString("foo"));
    verify(this.source, times(1)).getBytes("foo");
  }

  @Test
  public void shouldReturnInfoForClassOnClassPath() {
    assertTrue(this.testee.fetchClass(Integer.class).hasSome());
  }

  @Test
  public void shouldOnlyLookForKnownClassOnce() throws ClassNotFoundException {
    this.testee = new Repository(this.source);
    when(this.source.getBytes(anyString())).thenReturn(
        Option.some(ClassUtils.classAsBytes(String.class)));
    this.testee.fetchClass(ClassName.fromString("foo"));
    this.testee.fetchClass(ClassName.fromString("foo"));
    verify(this.source, times(1)).getBytes("foo");
  }

  @Test
  public void shouldDetectInterfacesAsInterfaces() {
    final Option<ClassInfo> anInterface = this.testee
        .fetchClass(Serializable.class);
    assertTrue(anInterface.value().isInterface());
  }

  @Test
  public void shouldDetectInterfacesAsAbstract() {
    final Option<ClassInfo> anInterface = this.testee
        .fetchClass(Serializable.class);
    assertTrue(anInterface.value().isAbstract());
  }

  @Test
  public void shouldDetectConcreteClassesAsNotInterfaces() {
    final Option<ClassInfo> aClass = this.testee.fetchClass(String.class);
    assertFalse(aClass.value().isInterface());
  }

  @Test
  public void shouldDetectConcreteClassesAsNotAbstract() {
    final Option<ClassInfo> aClass = this.testee.fetchClass(String.class);
    assertFalse(aClass.value().isAbstract());
  }

  public static class SimpleInnerClass {

  }

  @Test
  public void shouldReportOuterClassForStaticInnerClasses() {
    final String actual = getOuterClassNameFor(SimpleInnerClass.class);
    assertEquals(RepositoryTest.class.getName().replace(".", "/"), actual);
  }

  @Test
  public void shouldReportOuterClassForLocalClasses() {
    final Object local = new Object() {

    };

    final String actual = getOuterClassNameFor(local.getClass());
    assertEquals(RepositoryTest.class.getName().replace(".", "/"), actual);
  }

  public class NonStaticInnerClass {

  }

  @Test
  public void shouldReportOuterClassForNonStaticInnerClasses() {
    final String actual = getOuterClassNameFor(NonStaticInnerClass.class);
    assertEquals(RepositoryTest.class.getName().replace(".", "/"), actual);
  }

  public static class OuterStaticInnerClass {
    public static class InnerStaticClass {

    }
  }

  @Test
  public void shouldReportInnermstOuterClassForNestedInnerClasses() {
    final String actual = getOuterClassNameFor(OuterStaticInnerClass.InnerStaticClass.class);
    assertEquals(
        RepositoryTest.OuterStaticInnerClass.class.getName().replace(".", "/"),
        actual);
  }

  static class Foo {

  }

  static class Bar extends Foo {

  }

  @Test
  public void shouldReportSuperClass() {
    final Option<ClassInfo> aClass = this.testee.fetchClass(Bar.class);
    assertEquals(ClassName.fromClass(Foo.class), aClass.value().getSuperClass()
        .value().getName());
  }

  @Test
  public void shouldReportSuperClassAsObjectWhenNoneDeclared() {
    final Option<ClassInfo> aClass = this.testee.fetchClass(Foo.class);
    assertEquals(ClassName.fromClass(Object.class), aClass.value().getSuperClass()
        .value().getName());
  }

  @Test
  public void shouldReportNoSuperClassForObject() {
    final Option<ClassInfo> aClass = this.testee.fetchClass(Object.class);
    assertEquals(Option.none(), aClass.value().getSuperClass());
  }

  @Test
  public void shouldReportCodeLines() {
    final Option<ClassInfo> aClass = this.testee
        .fetchClass(RepositoryTest.class);
    aClass.value().isCodeLine(139); // flakey
  }

  @Ignore
  static class Annotated {

  }

  @Test
  public void shouldRecordClassLevelAnnotations() {
    final Option<ClassInfo> aClass = this.testee.fetchClass(Annotated.class);
    assertTrue(aClass.value().hasAnnotation(Ignore.class));
  }

  static class HasAnnotatedMethod {
    @Test
    public void foo() {

    }
  }

  @Test
  public void shouldRecordMethodLevelAnnotations() {
    final Option<ClassInfo> aClass = this.testee
        .fetchClass(HasAnnotatedMethod.class);
    assertTrue(aClass.value().hasAnnotation(Test.class));
  }

  static interface ITop {

  }

  static class Top implements ITop {

  }

  static class Middle extends Top {

  }

  static class Bottom extends Middle {

  }

  @Test
  public void shouldCorrectlyNegotiateClassHierachies() {
    final Option<ClassInfo> aClass = this.testee.fetchClass(Bottom.class);
    assertTrue(aClass.value().descendsFrom(Middle.class));
    assertTrue(aClass.value().descendsFrom(Top.class));
    assertTrue(aClass.value().descendsFrom(Object.class));
    assertFalse(aClass.value().descendsFrom(String.class));
  }

  @Test
  public void doesNotTreatInterfacesAsPartOfClassHierachy() {
    final Option<ClassInfo> aClass = this.testee.fetchClass(Bottom.class);
    assertFalse(aClass.value().descendsFrom(ITop.class));
  }

  @Test
  public void shouldRecordSourceFile() {
    final Option<ClassInfo> aClass = this.testee.fetchClass(String.class);
    assertEquals("String.java", aClass.value().getSourceFileName());
  }

  @Test
  public void shouldCalculateHashForSuppledClass() {
    this.testee.fetchClass(String.class);
    verify(this.hashFunction).hash(any(byte[].class));
  }

  private String getOuterClassNameFor(final Class<?> clazz) {
    return this.testee.fetchClass(clazz).value().getOuterClass().value()
        .getName().asInternalName();
  }

}
