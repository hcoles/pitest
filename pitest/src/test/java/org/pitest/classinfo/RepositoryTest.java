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

import java.io.Serializable;

import org.junit.Before;
import org.junit.Test;
import org.pitest.functional.Option;
import org.pitest.internal.ClassloaderByteArraySource;
import org.pitest.internal.IsolationUtils;

public class RepositoryTest {

  private Repository testee;

  @Before
  public void setUp() {
    this.testee = new Repository(new ClassloaderByteArraySource(
        IsolationUtils.getContextClassLoader()));
  }

  @Test
  public void shouldReturnNoneWhenAskedForUnknownClass() {
    assertEquals(Option.none(), this.testee.fetchClass("never.heard.of.you"));
  }

  @Test
  public void shouldReturnInfoForClassOnClassPath() {
    assertTrue(this.testee.fetchClass(Integer.class).hasSome());
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
    assertEquals(Foo.class.getName().replace(".", "/"), aClass.value()
        .getSuperClass().getName());
  }

  @Test
  public void shouldReportSuperClassAsObjectWhenNoneDeclared() {
    final Option<ClassInfo> aClass = this.testee.fetchClass(Foo.class);
    assertEquals(Object.class.getName().replace(".", "/"), aClass.value()
        .getSuperClass().getName());
  }

  @Test
  public void shouldReportCodeLines() {
    final Option<ClassInfo> aClass = this.testee
        .fetchClass(RepositoryTest.class);
    aClass.value().isCodeLine(139); // flakey
  }

  @Test
  public void matchIfTopLevelClassShouldReturnTrueForTopLevelClasses() {
    final Option<ClassInfo> aClass = this.testee
        .fetchClass(RepositoryTest.class);
    System.out.println(aClass.value().getOuterClass());
    assertTrue(ClassInfo.matchIfTopLevelClass().apply(aClass.value()));
  }

  @Test
  public void matchIfTopLevelClassShouldReturnFalseForInnerClasses() {
    final Option<ClassInfo> aClass = this.testee
        .fetchClass(NonStaticInnerClass.class);
    assertFalse(ClassInfo.matchIfTopLevelClass().apply(aClass.value()));
  }

  private String getOuterClassNameFor(final Class<?> clazz) {
    return this.testee.fetchClass(clazz).value().getOuterClass().value()
        .getName();
  }

}
