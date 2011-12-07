package org.pitest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;
import org.pitest.functional.F;
import org.pitest.internal.IsolationUtils;

public class DescriptionTest {

  private Description testee;

  @Test
  public void shouldCloneViaXStreamWithoutError() throws Exception {
    try {
      this.testee = new Description("foo", IOException.class);
      final Description actual = (Description) IsolationUtils
      .clone(this.testee);

      assertEquals(this.testee, actual);
    } catch (final Throwable t) {
      fail(t.getMessage());
    }
  }


  @Test
  public void shouldReturnTrueIfContainsClassMatchingPredicate() {
    this.testee = new Description("foo",String.class);
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
    this.testee = new Description("foo", Integer.class);
    assertFalse(this.testee.contains(theClass(String.class)));
  }
}
