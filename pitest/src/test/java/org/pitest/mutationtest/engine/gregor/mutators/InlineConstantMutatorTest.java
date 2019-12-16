/*
 * Copyright 2011 Henry Coles and Stefan Penndorf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pitest.mutationtest.engine.gregor.mutators;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;

public class InlineConstantMutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToMutateOnlyInlineConstants() {
    createTesteeWith(new InlineConstantMutator());
  }

  private static class HasBooleanICONST0 implements Callable<Boolean> {

    @Override
    public Boolean call() throws Exception {
      return false;
    }

  }

  @Test
  public void shouldProvideAMeaningfulName() {
    assertEquals("INLINE_CONSTANT_MUTATOR",
        new InlineConstantMutator().getName());
  }

  @Test
  public void shouldReplaceBooleanFalseWithTrue() throws Exception {
    final Mutant mutant = getFirstMutant(HasBooleanICONST0.class);
    assertMutantCallableReturns(new HasBooleanICONST0(), mutant, Boolean.TRUE);
  }

  private static class HasIntegerICONST0 implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
      return 0;
    }

  }

  @Test
  public void shouldReplaceInteger0With1() throws Exception {
    final Mutant mutant = getFirstMutant(HasIntegerICONST0.class);
    assertMutantCallableReturns(new HasIntegerICONST0(), mutant, 1);
  }

  private static class HasBooleanICONST1 implements Callable<Boolean> {

    @Override
    public Boolean call() throws Exception {
      return true;
    }

  }

  @Test
  public void shouldReplaceBooleanTrueWithFalse() throws Exception {
    final Mutant mutant = getFirstMutant(HasBooleanICONST1.class);
    assertMutantCallableReturns(new HasBooleanICONST1(), mutant, Boolean.FALSE);
  }

  private static class HasIntegerICONST1 implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
      return 1;
    }

  }

  @Test
  public void shouldReplaceInteger1With0() throws Exception {
    final Mutant mutant = getFirstMutant(HasIntegerICONST1.class);
    assertMutantCallableReturns(new HasIntegerICONST1(), mutant, 0);
  }

  private static class HasIntegerICONST2 implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
      return 2;
    }

  }

  @Test
  public void shouldReplaceInteger2With3() throws Exception {
    final Mutant mutant = getFirstMutant(HasIntegerICONST2.class);
    assertMutantCallableReturns(new HasIntegerICONST2(), mutant, 3);
  }

  private static class HasIntegerICONST3 implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
      return 3;
    }

  }

  @Test
  public void shouldReplaceInteger3With4() throws Exception {
    final Mutant mutant = getFirstMutant(HasIntegerICONST3.class);
    assertMutantCallableReturns(new HasIntegerICONST3(), mutant, 4);
  }

  private static class HasIntegerICONST4 implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
      return 4;
    }

  }

  @Test
  public void shouldReplaceInteger4With5() throws Exception {
    final Mutant mutant = getFirstMutant(HasIntegerICONST4.class);
    assertMutantCallableReturns(new HasIntegerICONST4(), mutant, 5);
  }

  private static class HasIntegerICONST5 implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
      return 5;
    }

  }

  @Test
  public void shouldReplaceInteger5With6() throws Exception {
    final Mutant mutant = getFirstMutant(HasIntegerICONST5.class);
    assertMutantCallableReturns(new HasIntegerICONST5(), mutant, 6);
  }

  private static class HasIntegerLDC implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
      return 987654321;
    }

  }

  @Test
  public void shouldReplaceLargeIntegerConstantsWithValuePlus1()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasIntegerLDC.class);
    assertMutantCallableReturns(new HasIntegerLDC(), mutant, 987654321 + 1);
  }

  private static class HasIntegerICONSTM1 implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
      return -1;
    }

  }

  /**
   * Note: Integer numbers and booleans are actually represented in the same way
   * be the JVM, it is therefore never safe if one changes a 0 to anything but a
   * 1 or a 1 to anything but a 0. Nevertheless if we find a -1 (ICONST_M1) it
   * must be an integer, short, byte or long. It won't be a boolean. So it is
   * always safe to replace -1 with 0.
   */
  @Test
  public void shouldReplaceIntegerMinus1With0() throws Exception {
    final Mutant mutant = getFirstMutant(HasIntegerICONSTM1.class);
    assertMutantCallableReturns(new HasIntegerICONSTM1(), mutant, 0);
  }

  private static class HasBIPUSHMinus2 implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
      return -2;
    }

  }

  @Test
  public void shouldReplaceIntegerMinus2WithMinus1() throws Exception {
    final Mutant mutant = getFirstMutant(HasBIPUSHMinus2.class);
    assertMutantCallableReturns(new HasBIPUSHMinus2(), mutant, -1);
  }

  private static class HasBIPUSH implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
      return 28;
    }

  }

  @Test
  public void shouldReplaceSmallIntegerConstantsWithValuePlus1()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasBIPUSH.class);
    assertMutantCallableReturns(new HasBIPUSH(), mutant, 29);
  }

  private static class HasSIPUSH implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
      return 32700;
    }

  }

  @Test
  public void shouldReplaceMediumIntegerConstantsWithValuePlus1()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasSIPUSH.class);
    assertMutantCallableReturns(new HasSIPUSH(), mutant, 32701);
  }

  private static class HasTwoMutationPoints implements Callable<Boolean> {

    @Override
    public Boolean call() throws Exception {
      int i = Short.MAX_VALUE;

      if (i != Short.MAX_VALUE) {
        i++; // prevent source formating from making final
        return Boolean.TRUE; // expected
      }

      return Boolean.FALSE;
    }

  }

  @Test
  public void shouldReplaceFirstMutationPointOnly() throws Exception {
    final Mutant mutant = getFirstMutant(HasTwoMutationPoints.class);
    assertMutantCallableReturns(new HasTwoMutationPoints(), mutant,
        Boolean.TRUE);
  }

  private static class HasShortOverflow implements Callable<Short> {

    @Override
    public Short call() throws Exception {
      short s = Short.MAX_VALUE;
      s = preventSourceFormatingMakingFinal(s);
      return s;
    }

  }

  /**
   * The JVM does not have a short type, it uses integer under the hood.
   * <code>Short.MAX_VALUE</code> (=32767) will always be rolled to
   * <code>Short.MIN_VALUE</code> (=-32768). Note that the rolling will occur
   * even if only Integer/int variables are used! So <code>int i = 32767;</code>
   * will always be mutated to <code>int i = -32768;</code>.
   */
  @Test
  public void shouldOverflowOnShortMaxValue() throws Exception {
    final Mutant mutant = getFirstMutant(HasShortOverflow.class);
    assertMutantCallableReturns(new HasShortOverflow(), mutant, Short.MIN_VALUE);
  }

  private static class HasIntegerAtMaxShortValue implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
      int i = Short.MAX_VALUE;
      i = preventSourceFormatingMakingFinal(i);
      return i;
    }

  }

  /**
   * The JVM does not have a short type, it uses integer under the hood.
   * <code>Short.MAX_VALUE</code> (=32767) will always be rolled to
   * <code>Short.MIN_VALUE</code> (=-32768). Note that the rolling will occur
   * even if only Integer/int variables are used! So <code>int i = 32767;</code>
   * will always be mutated to <code>int i = -32768;</code>.
   */
  @Test
  public void shouldOverflowIntegerOnShortMaxValue() throws Exception {
    final Mutant mutant = getFirstMutant(HasIntegerAtMaxShortValue.class);
    assertMutantCallableReturns(new HasIntegerAtMaxShortValue(), mutant,
        (int) Short.MIN_VALUE);
  }

  public static short preventSourceFormatingMakingFinal(final short s) {
    return s;
  }

  private static class HasByteOverflow implements Callable<Byte> {

    @Override
    public Byte call() throws Exception {
      byte b = Byte.MAX_VALUE;
      b = preventSourceFormatingMakingFinal(b);
      return b;
    }
  }

  /**
   * eclipse source cleanup will make everything final if possible
   */
  public static byte preventSourceFormatingMakingFinal(final byte b) {
    return b;
  }

  /**
   * The JVM does not have a byte type, it uses integer under the hood.
   * <code>Byte.MAX_VALUE</code> (=127) will always be rolled to
   * <code>Byte.MIN_VALUE</code> (=-128). Note that the rolling will occur even
   * if only Integer/int variables are used! So <code>int i = 127;</code> will
   * always be mutated to <code>int i = -128;</code>.
   */
  @Test
  public void shouldOverflowOnByteMaxValue() throws Exception {
    final Mutant mutant = getFirstMutant(HasByteOverflow.class);
    assertMutantCallableReturns(new HasByteOverflow(), mutant, Byte.MIN_VALUE);
  }

  private static class HasIntegerMaxValue implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
      return Integer.MAX_VALUE;
    }

  }

  @Test
  public void shouldReplaceIntegerMaxWithIntegerMin() throws Exception {
    final Mutant mutant = getFirstMutant(HasIntegerMaxValue.class);
    assertMutantCallableReturns(new HasIntegerMaxValue(), mutant,
        Integer.MIN_VALUE);
  }

  private static class HasLongLCONST0 implements Callable<Long> {

    @Override
    public Long call() throws Exception {
      return 0L;
    }

  }

  @Test
  public void shouldReplaceLong0With1() throws Exception {
    final Mutant mutant = getFirstMutant(HasLongLCONST0.class);
    assertMutantCallableReturns(new HasLongLCONST0(), mutant, 1L);
  }

  private static class HasLongLCONST1 implements Callable<Long> {

    @Override
    public Long call() throws Exception {
      return 1L;
    }

  }

  @Test
  public void shouldReplaceLong1With2() throws Exception {
    final Mutant mutant = getFirstMutant(HasLongLCONST1.class);
    assertMutantCallableReturns(new HasLongLCONST1(), mutant, 2L);
  }

  private static class HasLongLDC implements Callable<Long> {

    @Override
    public Long call() throws Exception {
      return 2999999999L;
    }

  }

  @Test
  public void shouldReplaceLongWithValuePlus1() throws Exception {
    final Mutant mutant = getFirstMutant(HasLongLDC.class);
    assertMutantCallableReturns(new HasLongLDC(), mutant, 3000000000L);
  }

  private static class HasLongLDCMinus1 implements Callable<Long> {

    @Override
    public Long call() throws Exception {
      return -1L;
    }

  }

  @Test
  public void shouldReplaceLongMinus1With0() throws Exception {
    final Mutant mutant = getFirstMutant(HasLongLDCMinus1.class);
    assertMutantCallableReturns(new HasLongLDCMinus1(), mutant, 0L);
  }

  /*
   * Double and Float
   */

  private static class HasFloatFCONST0 implements Callable<Float> {

    @Override
    public Float call() throws Exception {
      return 0.0F;
    }

  }

  @Test
  public void shouldReplaceFloat0With1() throws Exception {
    final Mutant mutant = getFirstMutant(HasFloatFCONST0.class);
    assertMutantCallableReturns(new HasFloatFCONST0(), mutant, 1.0F);
  }

  private static class HasFloatFCONST1 implements Callable<Float> {

    @Override
    public Float call() throws Exception {
      return 1.0F;
    }

  }

  @Test
  public void shouldReplaceFloat1With2() throws Exception {
    final Mutant mutant = getFirstMutant(HasFloatFCONST1.class);
    assertMutantCallableReturns(new HasFloatFCONST1(), mutant, 2.0F);
  }

  private static class HasFloatFCONST2 implements Callable<Float> {

    @Override
    public Float call() throws Exception {
      return 2.0F;
    }

  }

  @Test
  public void shouldReplaceFloat2With1() throws Exception {
    final Mutant mutant = getFirstMutant(HasFloatFCONST2.class);
    assertMutantCallableReturns(new HasFloatFCONST2(), mutant, 1.0F);
  }

  private static class HasFloatLDC implements Callable<Float> {

    @Override
    public Float call() throws Exception {
      return 8364.123F;
    }

  }

  @Test
  public void shouldReplaceFloatWith1() throws Exception {
    final Mutant mutant = getFirstMutant(HasFloatLDC.class);
    assertMutantCallableReturns(new HasFloatLDC(), mutant, 1.0F);
  }

  private static class HasFloatMultipleLDC implements Callable<Float> {

    @Override
    public Float call() throws Exception {
      float f = 16.0F;
      float f2 = 4.0F;
      f = preventSourceFormatingMakingFinal(f);
      f2 = preventSourceFormatingMakingFinal(f2);
      return f * f2;
    }
  }

  private static <T> T preventSourceFormatingMakingFinal(final T f) {
    return f;
  }

  @Test
  public void shouldReplaceFirstFloatMutationPointOnly() throws Exception {
    final Mutant mutant = getFirstMutant(HasFloatMultipleLDC.class);
    assertMutantCallableReturns(new HasFloatMultipleLDC(), mutant, 4.0F);
  }

  private static class HasDoubleDCONST0 implements Callable<Double> {

    @Override
    public Double call() throws Exception {
      return 0.0D;
    }

  }

  @Test
  public void shouldReplaceDouble0With1() throws Exception {
    final Mutant mutant = getFirstMutant(HasDoubleDCONST0.class);
    assertMutantCallableReturns(new HasDoubleDCONST0(), mutant, 1.0D);
  }

  private static class HasDoubleDCONST1 implements Callable<Double> {

    @Override
    public Double call() throws Exception {
      return 1.0D;
    }

  }

  @Test
  public void shouldReplaceDouble1With2() throws Exception {
    final Mutant mutant = getFirstMutant(HasDoubleDCONST1.class);
    assertMutantCallableReturns(new HasDoubleDCONST1(), mutant, 2.0D);
  }

  private static class HasDoubleLDC implements Callable<Double> {

    @Override
    public Double call() throws Exception {
      return 123456789.123D;
    }

  }

  @Test
  public void shouldReplaceDoubleWith1() throws Exception {
    final Mutant mutant = getFirstMutant(HasDoubleLDC.class);
    assertMutantCallableReturns(new HasDoubleLDC(), mutant, 1.0D);
  }

  private static class HasDoubleMultipleLDC implements Callable<Double> {

    @Override
    public Double call() throws Exception {
      double d = 4578.1158D;
      double d2 = 2.0D;
      d = preventSourceFormatingMakingFinal(d);
      d2 = preventSourceFormatingMakingFinal(d2);
      return d * d2;
    }

  }

  @Test
  public void shouldReplaceFirstDoubleMutationPointOnly() throws Exception {
    final Mutant mutant = getFirstMutant(HasDoubleMultipleLDC.class);
    assertMutantCallableReturns(new HasDoubleMultipleLDC(), mutant, 2.0D);
  }

}