package org.pitest.sequence;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MatchTest {

  private final Context unused = null;

  @Test
  public void alwaysShouldAlwaysMatch() {
    final Match<Integer> testee = Match.always();
    assertTrue(testee.test(this.unused, 1).result());
    assertTrue(testee.test(this.unused, Integer.MAX_VALUE).result());
  }

  @Test
  public void neverShouldNeverMatch() {
    final Match<Integer> testee = Match.never();
    assertFalse(testee.test(this.unused, 1).result());
    assertFalse(testee.test(this.unused, Integer.MAX_VALUE).result());
  }

  @Test
  public void negateShouldInvertLogic() {
    final Match<Integer> testee = Match.never();
    assertTrue(testee.negate().test(this.unused, 1).result());
    assertFalse(testee.negate().negate().test(this.unused, Integer.MAX_VALUE).result());
  }

  @Test
  public void isEqualShouldCheckEquality() {
    final Match<Integer> testee = Match.isEqual(1);
    assertTrue(testee.test(this.unused, 1).result());
    assertFalse(testee.test(this.unused, 2).result());
  }

  @Test
  public void andShouldLogicallyAnd() {
    final Match<Integer> isTrue = Match.always();
    final Match<Integer> isFalse = Match.never();
    assertTrue(isTrue.and(isTrue).test(this.unused, 1).result());
    assertFalse(isTrue.and(isFalse).test(this.unused, 1).result());
    assertFalse(isFalse.and(isFalse).test(this.unused, 1).result());
    assertFalse(isFalse.and(isTrue).test(this.unused, 1).result());
  }

  @Test
  public void orShouldLogicallyOr() {
    final Match<Integer> isTrue = Match.always();
    final Match<Integer> isFalse = Match.never();
    assertTrue(isTrue.or(isTrue).test(this.unused, 1).result());
    assertTrue(isTrue.or(isFalse).test(this.unused, 1).result());
    assertFalse(isFalse.or(isFalse).test(this.unused, 1).result());
    assertTrue(isFalse.or(isTrue).test(this.unused, 1).result());
  }

  @Test
  public void asPredicateConvertsToPredicate() {
    assertThat(Match.always().asPredicate().test("anything")).isTrue();
    assertThat(Match.never().asPredicate().test("anything")).isFalse();
  }

}
