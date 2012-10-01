package org.pitest.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.pitest.classinfo.ClassName;
import org.pitest.internal.ClassloaderByteArraySource;
import org.pitest.internal.IsolationUtils;

public class ComputeClassWriterTest {
  
  static class Foo extends ClassWriter {
    public Foo(int arg0) {
      super(arg0);
      // TODO Auto-generated constructor stub
    }

    @Override
    public String getCommonSuperClass(final String type1, final String type2) {
      return super.getCommonSuperClass(type1, type2);
    }
  }
  
 //Foo testee = new Foo(0);
  private final ComputeClassWriter testee = new ComputeClassWriter(
                                              new ClassloaderByteArraySource(
                                                  IsolationUtils
                                                      .getContextClassLoader()),
                                              0);

  @Test
  public void shouldResolveObjectAsSuperClassWhenNoCommonParentExists() {
    assertEquals(
        ClassName.fromClass(Object.class).asInternalName(),
        callTesteeWith(Integer.class,
            String.class));
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
    assertEquals(
        ClassName.fromClass(Parent.class).asInternalName(),
        callTesteeWith(Child1.class,
            Child2.class));
  }
  
  static class ImplementsICommon1 implements ICommon {

  }

  static class ImplementsICommon2 implements ICommon {

  }
  
  @Test
  public void shouldResolveObjectAsSuperClassWhenImplementCommonInterface() {
    assertEquals(
        ClassName.fromClass(Object.class).asInternalName(),
        callTesteeWith(ImplementsICommon1.class,
            ImplementsICommon2.class));
  }
  
  
  static interface ICommon2 extends ICommon {

  }
  
  static interface ICommon3 extends ICommon {

  }
  
  @Test
  public void shouldResolveObjectAsSuperClassWhenInterfacesExtendCommonInterface() {
    assertEquals(
        ClassName.fromClass(Object.class).asInternalName(),
        callTesteeWith(ICommon2.class,
            ICommon3.class));
  }
  
  @Test
  public void shouldResolveParentInterfaceWhenSecondInterfaceExtendsTheFirst() {
    assertEquals(
        ClassName.fromClass(ICommon.class).asInternalName(),
        callTesteeWith(ICommon.class,
            ICommon2.class));
  }
  
  @Test
  public void shouldResolveParentInterfaceWhenFirstInterfaceExtendsTheSecond() {
    assertEquals(
        ClassName.fromClass(ICommon.class).asInternalName(),
        callTesteeWith(ICommon2.class,
            ICommon.class));
  }
  
  static class GrandChild extends Child2 {

  }
  
  @Test
  public void shouldResolveCommonParentWhenNotImmediateParentOfSecondType() {
    assertEquals(
        ClassName.fromClass(Parent.class).asInternalName(),
        callTesteeWith(Child1.class,
            GrandChild.class));
  }
  
  @Test
  public void shouldResolveCommonParentWhenNotImmediateParentOfFirstType() {
    assertEquals(
        ClassName.fromClass(Parent.class).asInternalName(),callTesteeWith(GrandChild.class,Child1.class)
        );
  }
  
  static interface ICommonGrandChild extends ICommon3 {

  }
  
  static interface ICommonGreatGrandChild extends ICommon3 {

  }
  
  
  @Test
  public void shouldCommonParentInterfaceWhenNotImmediateParentOfSecondType() {
    assertEquals(
        ClassName.fromClass(ICommon3.class).asInternalName(),
        callTesteeWith(ICommon3.class,
            ICommonGreatGrandChild.class));
  }
  
  @Test
  public void shouldCommonParentInterfaceWhenNotImmediateParentOfFirstType() {
    assertEquals(
        ClassName.fromClass(ICommon3.class).asInternalName(),
        callTesteeWith(ICommonGreatGrandChild.class,ICommon3.class));
  }
  
  private final String callTesteeWith(Class<?> first, Class<?> second) {
    return testee.getCommonSuperClass(ClassName.fromClass(first).asInternalName(), ClassName.fromClass(second).asInternalName());
  }

}
