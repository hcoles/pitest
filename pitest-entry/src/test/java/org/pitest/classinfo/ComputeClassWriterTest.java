package org.pitest.classinfo;

import static org.assertj.core.api.Assertions.assertThat;

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
    assertThat(callTesteeWith(Integer.class, String.class)).isEqualTo(ClassName.fromClass(Object.class).asInternalName());
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
    assertThat(callTesteeWith(Child1.class, Child2.class)).isEqualTo(ClassName.fromClass(Parent.class).asInternalName());
  }

  static class ImplementsICommon1 implements ICommon {

  }

  static class ImplementsICommon2 implements ICommon {

  }

  @Test
  public void shouldResolveObjectAsSuperClassWhenImplementCommonInterface() {
    assertThat(callTesteeWith(ImplementsICommon1.class, ImplementsICommon2.class)).isEqualTo(ClassName.fromClass(Object.class).asInternalName());
  }

  static interface ICommon2 extends ICommon {

  }

  static interface ICommon3 extends ICommon {

  }

  @Test
  public void shouldResolveObjectAsSuperClassWhenInterfacesExtendCommonInterface() {
    assertThat(callTesteeWith(ICommon2.class, ICommon3.class)).isEqualTo(ClassName.fromClass(Object.class).asInternalName());
  }

  @Test
  public void shouldResolveParentInterfaceWhenSecondInterfaceExtendsTheFirst() {
    assertThat(callTesteeWith(ICommon.class, ICommon2.class)).isEqualTo(ClassName.fromClass(ICommon.class).asInternalName());
  }

  @Test
  public void shouldResolveParentInterfaceWhenFirstInterfaceExtendsTheSecond() {
    assertThat(callTesteeWith(ICommon2.class, ICommon.class)).isEqualTo(ClassName.fromClass(ICommon.class).asInternalName());
  }

  static class GrandChild extends Child2 {

  }

  @Test
  public void shouldResolveCommonParentWhenNotImmediateParentOfSecondType() {
    assertThat(callTesteeWith(Child1.class, GrandChild.class)).isEqualTo(ClassName.fromClass(Parent.class).asInternalName());
  }

  @Test
  public void shouldResolveCommonParentWhenNotImmediateParentOfFirstType() {
    assertThat(callTesteeWith(GrandChild.class, Child1.class)).isEqualTo(ClassName.fromClass(Parent.class).asInternalName());
  }

  static interface ICommonGrandChild extends ICommon3 {

  }

  static interface ICommonGreatGrandChild extends ICommon3 {

  }

  @Test
  public void shouldCommonParentInterfaceWhenNotImmediateParentOfSecondType() {
    assertThat(callTesteeWith(ICommon3.class, ICommonGreatGrandChild.class)).isEqualTo(ClassName.fromClass(ICommon3.class).asInternalName());
  }

  @Test
  public void shouldCommonParentInterfaceWhenNotImmediateParentOfFirstType() {
    assertThat(callTesteeWith(ICommonGreatGrandChild.class, ICommon3.class)).isEqualTo(ClassName.fromClass(ICommon3.class).asInternalName());
  }

  private final String callTesteeWith(final Class<?> first,
      final Class<?> second) {
    return this.testee.getCommonSuperClass(ClassName.fromClass(first)
        .asInternalName(), ClassName.fromClass(second).asInternalName());
  }

}