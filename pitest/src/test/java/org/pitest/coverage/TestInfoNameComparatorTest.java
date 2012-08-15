package org.pitest.coverage;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.pitest.classinfo.ClassName;
import org.pitest.coverage.domain.TestInfo;
import org.pitest.functional.Option;


public class TestInfoNameComparatorTest {
  
  private TestInfoNameComparator testee;
  private TestInfo lhs;
  private TestInfo rhs;
  
  @Before
  public void setUp() {
    testee = new TestInfoNameComparator();
    lhs = new TestInfo("foo", "0name", 0, Option.<ClassName>none(),0);
    rhs = new TestInfo("foo", "1name", 0, Option.<ClassName>none(),0);
  }
  
  @Test
  public void shouldCompareTestsBasedOnTheirNames() {
    assertEquals(-1,testee.compare(lhs, rhs));
    assertEquals(1,testee.compare(rhs, lhs));
  }
  
  @Test
  public void shouldTreatIdenticallyNamesTestsAsEqual() {
    TestInfo sameName = new TestInfo("bar", "0name", 1000, Option.<ClassName>none(),0);
    assertEquals(0,testee.compare(lhs, sameName));
  }

}
