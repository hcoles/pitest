package org.pitest.classinfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;

import org.junit.Before;
import org.junit.Test;
import org.pitest.coverage.codeassist.ClassUtils;

public class AddlerHashTest {

  private AddlerHash testee;

  @Before
  public void setUp() {
    this.testee = new AddlerHash();
  }

  @Test
  public void shouldCreateChecksumOfSuppliedBytes()
      throws ClassNotFoundException {
    assertTrue(this.testee.hash(ClassUtils.classAsBytes(String.class)) != 0);
  }

  @Test
  public void shouldGenerateSameHashForSameInput()
      throws ClassNotFoundException {
    final long expected = this.testee.hash(ClassUtils
        .classAsBytes(String.class));
    assertEquals(expected,
        this.testee.hash(ClassUtils.classAsBytes(String.class)));
  }

  @Test
  public void shouldCreateDifferentHashesForDifferentClasses()
      throws ClassNotFoundException {
    assertTrue(this.testee.hash(ClassUtils.classAsBytes(Comparable.class)) != this.testee
        .hash(ClassUtils.classAsBytes(Serializable.class)));
  }

}
