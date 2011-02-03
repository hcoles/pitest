package org.pitest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Ignore;
import org.junit.Test;
import org.pitest.functional.F;
import org.pitest.functional.Option;
import org.pitest.internal.IsolationUtils;
import org.pitest.reflection.Reflection;

public class DescriptionTest {

  private Description testee;

  @Test
  @Ignore
  // issues with cgilib
  public void shouldKeepEqualsContract() {
    EqualsVerifier.forClass(Description.class).suppress(Warning.NULL_FIELDS)
        .verify();
  }

  @Test
  public void shouldCloneViaXStreamWithoutError() throws Exception {
    try {
      final Method m = Reflection.publicMethod(this.getClass(),
          "shouldCloneViaXStreamWithoutError");
      this.testee = new Description("foo", IOException.class, new TestMethod(m));
      final Description actual = (Description) IsolationUtils
          .clone(this.testee);

      assertEquals(this.testee, actual);
    } catch (final Throwable t) {
      fail(t.getMessage());
    }
  }

  @Test
  public void shouldAcceptNullMethods() {
    this.testee = new Description("foo", Collections.<Class<?>> emptySet(),
        null);
    assertEquals(Option.none(), this.testee.getMethod());
  }

  @Test
  public void shouldStoreSuppliedMethod() {
    final TestMethod tm = new TestMethod(null);
    this.testee = new Description("foo", Collections.<Class<?>> emptySet(), tm);
    assertEquals(Option.some(tm), this.testee.getMethod());
  }

  @Test
  public void shouldReturnTrueIfContainsClassMatchingPredicate() {
    this.testee = new Description("foo",
        Collections.<Class<?>> singletonList(String.class), null);
    assertTrue(this.testee.contains(theClass(String.class)));
  }

  private F<Class<?>, Boolean> theClass(final Class<String> match) {
    return new F<Class<?>, Boolean>() {
      public Boolean apply(final Class<?> a) {
        return a.equals(match);
      }
    };
  }

  @Test
  public void shouldReturnFalseIfDoesNotContainClassMatchingPredicate() {
    this.testee = new Description("foo",
        Collections.<Class<?>> singletonList(Integer.class), null);
    assertFalse(this.testee.contains(theClass(String.class)));
  }
}
