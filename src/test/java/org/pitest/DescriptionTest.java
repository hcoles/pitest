package org.pitest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Method;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Test;
import org.pitest.functional.Option;
import org.pitest.reflection.Reflection;

public class DescriptionTest {

  private Description testee;

  @Test
  public void testEqualsContractKept() {
    EqualsVerifier.forClass(Description.class).verify();
  }

  @Test
  public void testCanBeSerializedAndDeserialized() throws Exception {
    try {
      final Method m = Reflection.publicMethod(this.getClass(),
          "testCanBeSerializedAndDeserialized");
      this.testee = new Description("foo", IOException.class, new TestMethod(m,
          null));
      SerializationUtils.clone(this.testee);
    } catch (final Throwable t) {
      fail();
    }
  }

  @Test
  public void testGetMethodReturnsNoneIfNoMethodSupplied() {
    this.testee = new Description("foo", null, null);
    assertEquals(Option.none(), this.testee.getMethod());
  }

  @Test
  public void testGetMethodReturnsSomeIfMethodSupplied() {
    final TestMethod tm = new TestMethod(null, null);
    this.testee = new Description("foo", null, tm);
    assertEquals(Option.someOrNone(tm), this.testee.getMethod());
  }
}
