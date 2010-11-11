package org.pitest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Method;

import org.junit.Test;
import org.pitest.internal.IsolationUtils;
import org.pitest.reflection.Reflection;

public class TestMethodTest {

  @Test
  public void testCanBeSerializedAndDeserialized() throws Exception {
    try {
      final Method m = Reflection.publicMethod(this.getClass(),
          "testCanBeSerializedAndDeserialized");
      final TestMethod testee = new TestMethod(m, IOException.class);
      final String xml = IsolationUtils.toXml(testee);
      final TestMethod actual = (TestMethod) IsolationUtils.fromXml(xml);
      assertEquals(actual.getMethod(), testee.getMethod());
    } catch (final Throwable t) {
      fail();
    }
  }

}
