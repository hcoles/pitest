package org.pitest;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Method;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Test;
import org.pitest.reflection.Reflection;

public class TestMethodTest {

  @Test
  public void testCanBeSerializedAndDeserialized() throws Exception {
    try {
      final Method m = Reflection.publicMethod(this.getClass(),
          "testCanBeSerializedAndDeserialized");
      final TestMethod testee = new TestMethod(m, IOException.class);
      SerializationUtils.clone(testee);
    } catch (final Throwable t) {
      fail();
    }
  }

}
