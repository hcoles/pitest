package org.pitest.sequence;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MatchTest {

  private Context<Integer> unused = null;

  @Test
  public void alwaysShouldAlwaysMatch() {
    Match<Integer> testee = Match.always();
    assertTrue(testee.test(unused, 1));
    assertTrue(testee.test(unused, Integer.MAX_VALUE));
  }
  
  @Test
  public void neverShouldNeverMatch() {
    Match<Integer> testee = Match.never();
    assertFalse(testee.test(unused, 1));
    assertFalse(testee.test(unused, Integer.MAX_VALUE));
  }
  
  @Test
  public void negateShouldInvertLogic() {
    Match<Integer> testee = Match.never();
    assertTrue(testee.negate().test(unused, 1));
    assertFalse(testee.negate().negate().test(unused, Integer.MAX_VALUE));
  }
  
  @Test
  public void isEqualShouldCheckEquality() {
    Match<Integer> testee = Match.isEqual(1);
    assertTrue(testee.test(unused, 1));
    assertFalse(testee.test(unused, 2));
  }
  
  @Test
  public void andShouldLogicallyAnd() {
    Match<Integer> isTrue = Match.always();
    Match<Integer> isFalse = Match.never();
    assertTrue(isTrue.and(isTrue).test(unused, 1));
    assertFalse(isTrue.and(isFalse).test(unused, 1));
    assertFalse(isFalse.and(isFalse).test(unused, 1));
    assertFalse(isFalse.and(isTrue).test(unused, 1));
  }
  
  @Test
  public void orShouldLogicallyOr() {
    Match<Integer> isTrue = Match.always();
    Match<Integer> isFalse = Match.never();
    assertTrue(isTrue.or(isTrue).test(unused, 1));
    assertTrue(isTrue.or(isFalse).test(unused, 1));
    assertFalse(isFalse.or(isFalse).test(unused, 1));
    assertTrue(isFalse.or(isTrue).test(unused, 1));
  }

}
