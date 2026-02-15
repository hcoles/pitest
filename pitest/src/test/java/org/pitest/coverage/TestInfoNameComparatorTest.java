package org.pitest.coverage;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.pitest.classinfo.ClassName;
import java.util.Optional;

public class TestInfoNameComparatorTest {

  private TestInfoNameComparator testee;
  private TestInfo               lhs;
  private TestInfo               rhs;

  @Before
  public void setUp() {
    this.testee = new TestInfoNameComparator();
    this.lhs = new TestInfo("foo", "0name", 0, Optional.<ClassName> empty(), 0);
    this.rhs = new TestInfo("foo", "1name", 0, Optional.<ClassName> empty(), 0);
  }

  @Test
  public void shouldCompareTestsBasedOnTheirNames() {
    assertThat(this.testee.compare(this.lhs, this.rhs)).isLessThan(0);
    assertThat(this.testee.compare(this.rhs, this.lhs)).isGreaterThan(0);
  }

  @Test
  public void shouldTreatIdenticallyNamesTestsAsEqual() {
    final TestInfo sameName = new TestInfo("bar", "0name", 1000,
        Optional.<ClassName> empty(), 0);
    assertThat(this.testee.compare(this.lhs, sameName)).isEqualTo(0);
  }

}
