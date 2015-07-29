package org.pitest.classinfo;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Test;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.util.IsolationUtils;

public class ComputeClassWriterTest {

  private final ComputeClassWriter testee = new ComputeClassWriter(
      new ClassloaderByteArraySource(
          IsolationUtils
          .getContextClassLoader()),
          new HashMap<String, String>(), 0);

  @Test
  public void shouldResolveObjectAsSuperClassWhenNoCommonParentExists() {
    assertEquals(ClassName.fromClass(Object.class).asInternalName(),
        callTesteeWith(Integer.class, String.class));
  }

  static interface ICommon {

  }

  static class Parent {

  }

  static class Child1 extends Parent {

  }

  static class Child2 extends Parent {

  }

  @Test
  public void shouldResolveSuperClassWhenCommonParentExists() {
    assertEquals(ClassName.fromClass(Parent.class).asInternalName(),
        callTesteeWith(Child1.class, Child2.class));
  }

  static class ImplementsICommon1 implements ICommon {

  }

  static class ImplementsICommon2 implements ICommon {

  }

  @Test
  public void shouldResolveObjectAsSuperClassWhenImplementCommonInterface() {
    assertEquals(ClassName.fromClass(Object.class).asInternalName(),
        callTesteeWith(ImplementsICommon1.class, ImplementsICommon2.class));
  }

  static interface ICommon2 extends ICommon {

  }

  static interface ICommon3 extends ICommon {

  }

  @Test
  public void shouldResolveObjectAsSuperClassWhenInterfacesExtendCommonInterface() {
    assertEquals(ClassName.fromClass(Object.class).asInternalName(),
        callTesteeWith(ICommon2.class, ICommon3.class));
  }

  @Test
  public void shouldResolveParentInterfaceWhenSecondInterfaceExtendsTheFirst() {
    assertEquals(ClassName.fromClass(ICommon.class).asInternalName(),
        callTesteeWith(ICommon.class, ICommon2.class));
  }

  @Test
  public void shouldResolveParentInterfaceWhenFirstInterfaceExtendsTheSecond() {
    assertEquals(ClassName.fromClass(ICommon.class).asInternalName(),
        callTesteeWith(ICommon2.class, ICommon.class));
  }

  static class GrandChild extends Child2 {

  }

  @Test
  public void shouldResolveCommonParentWhenNotImmediateParentOfSecondType() {
    assertEquals(ClassName.fromClass(Parent.class).asInternalName(),
        callTesteeWith(Child1.class, GrandChild.class));
  }

  @Test
  public void shouldResolveCommonParentWhenNotImmediateParentOfFirstType() {
    assertEquals(ClassName.fromClass(Parent.class).asInternalName(),
        callTesteeWith(GrandChild.class, Child1.class));
  }

  static interface ICommonGrandChild extends ICommon3 {

  }

  static interface ICommonGreatGrandChild extends ICommon3 {

  }

  @Test
  public void shouldCommonParentInterfaceWhenNotImmediateParentOfSecondType() {
    assertEquals(ClassName.fromClass(ICommon3.class).asInternalName(),
        callTesteeWith(ICommon3.class, ICommonGreatGrandChild.class));
  }

  @Test
  public void shouldCommonParentInterfaceWhenNotImmediateParentOfFirstType() {
    assertEquals(ClassName.fromClass(ICommon3.class).asInternalName(),
        callTesteeWith(ICommonGreatGrandChild.class, ICommon3.class));
  }

  private final String callTesteeWith(final Class<?> first,
      final Class<?> second) {
    return this.testee.getCommonSuperClass(ClassName.fromClass(first)
        .asInternalName(), ClassName.fromClass(second).asInternalName());
  }

}
