package org.pitest.simpletest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.pitest.reflection.Reflection;

public class SignatureEqualityStrategyTest {

  SignatureEqualityStrategy testee;

  @Before
  public void createTestee() {
    this.testee = new SignatureEqualityStrategy();
  }

  @Test
  public void shouldConsiderDifferentlyNamedMethodsNotEqual() {
    assertFalse(this.testee.isEqual(createTestMethod("foo"),
        createTestMethod("bar")));
  }

  @Test
  public void shouldConsiderSameMethodEqual() {
    assertTrue(this.testee.isEqual(createTestMethod("foo"),
        createTestMethod("foo")));
  }

  @Test
  public void shouldConsiderMethodsWithSameNameButDifferentSignaturesNotEqual() {
    final Predicate<Method> noargs = a -> a.getName().equals("foo") && (a.getParameterTypes().length == 0);

    final Predicate<Method> onearg = a -> a.getName().equals("foo") && (a.getParameterTypes().length == 1);

    assertFalse(this.testee.isEqual(createTestMethod(noargs),
        createTestMethod(onearg)));
  }

  public void foo(final int i) {

  }

  public void foo() {

  }

  public void bar() {

  }

  private TestMethod createTestMethod(final Predicate<Method> p) {
    return new TestMethod(Reflection.publicMethod(this.getClass(), p));
  }

  private TestMethod createTestMethod(final String name) {
    final Predicate<Method> p = a -> a.getName().equals(name);
    return createTestMethod(p);
  }

}
