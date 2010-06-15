package org.pitest;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Before;
import org.junit.Test;
import org.pitest.reflection.Reflection;

public class JavaMethodTest {

  private Method m1;
  private Method m2;

  @Before
  public void setup() {
    this.m1 = Reflection.publicMethod(JavaMethodTest.class,
        "testCanBeSerializedAndDeserialized");
    this.m2 = Reflection.publicMethod(JavaMethodTest.class,
        "testEqualsContractKept");
  }

  @Test
  public void testEqualsContractKept() {
    EqualsVerifier.forClass(JavaMethod.class).withPrefabValues(Method.class,
        this.m1, this.m2).suppress(Warning.NONFINAL_FIELDS).verify();
  }

  @Test
  public void testCanBeSerializedAndDeserialized() throws Exception {

    final JavaMethod testee = new JavaMethod(this.m1);

    final JavaMethod actual = (JavaMethod) SerializationUtils.clone(testee);
    assertEquals(testee.method(), actual.method());

  }

}
