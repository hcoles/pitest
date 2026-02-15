package org.pitest.classinfo;

import static org.assertj.core.api.Assertions.assertThat;

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
    assertThat(this.testee.hash(ClassUtils.classAsBytes(String.class))).isNotZero();

  }

  @Test
  public void shouldGenerateSameHashForSameInput()
      throws ClassNotFoundException {
    final long expected = this.testee.hash(ClassUtils
        .classAsBytes(String.class));
    assertThat(this.testee.hash(ClassUtils.classAsBytes(String.class))).isEqualTo(expected);

  }

  @Test
  public void shouldCreateDifferentHashesForDifferentClasses()
      throws ClassNotFoundException {
    assertThat(this.testee.hash(ClassUtils.classAsBytes(Comparable.class))).isNotEqualTo(this.testee
        .hash(ClassUtils.classAsBytes(Serializable.class)));

  }

}