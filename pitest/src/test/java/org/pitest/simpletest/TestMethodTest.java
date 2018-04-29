package org.pitest.simpletest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Method;

import org.junit.Test;
import org.pitest.reflection.Reflection;
import org.pitest.util.XStreamCloning;

public class TestMethodTest {

  @Test
  public void shouldCloneViaXStreamWithoutError() throws Exception {
    try {

      final Method m = Reflection.publicMethod(this.getClass(),
          "shouldCloneViaXStreamWithoutError");
      final TestMethod testee = new TestMethod(m, IOException.class);
      final TestMethod actual = (TestMethod) XStreamCloning.clone(testee);
      assertEquals(actual.getMethod(), testee.getMethod());
    } catch (final Throwable t) {
      fail();
    }
  }

}
