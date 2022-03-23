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

import org.junit.Test;
import org.pitest.verifier.mutants.MutatorVerifierStart;

import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;

public class InlineConstantMutatorTest {

    MutatorVerifierStart v = MutatorVerifierStart.forMutator(new InlineConstantMutator());

    public static short preventSourceFormatingMakingFinal(final short s) {
        return s;
    }

    /**
     * eclipse source cleanup will make everything final if possible
     */
    public static byte preventSourceFormatingMakingFinal(final byte b) {
        return b;
    }

    private static <T> T preventSourceFormatingMakingFinal(final T f) {
        return f;
    }

    @Test
    public void shouldProvideAMeaningfulName() {
        assertEquals("INLINE_CONSTS",
                new InlineConstantMutator().getName());
    }

    @Test
    public void shouldReplaceBooleanFalseWithTrue(){
        v.forCallableClass(HasBooleanICONST0.class)
                .firstMutantShouldReturn(Boolean.TRUE);
    }

    @Test
    public void shouldReplaceInteger0With1(){
        v.forCallableClass(HasIntegerICONST0.class)
                .firstMutantShouldReturn(1);
    }

    @Test
    public void shouldReplaceBooleanTrueWithFalse(){
        v.forCallableClass(HasBooleanICONST1.class)
                .firstMutantShouldReturn(Boolean.FALSE);
    }

    @Test
    public void shouldReplaceInteger1With0(){
        v.forCallableClass(HasIntegerICONST1.class)
                .firstMutantShouldReturn(0);
    }

    @Test
    public void shouldReplaceInteger2With3(){
        v.forCallableClass(HasIntegerICONST2.class)
                .firstMutantShouldReturn(3);
    }

    @Test
    public void shouldReplaceInteger3With4(){
        v.forCallableClass(HasIntegerICONST3.class)
                .firstMutantShouldReturn(4);
    }

    @Test
    public void shouldReplaceInteger4With5(){
        v.forCallableClass(HasIntegerICONST4.class)
                .firstMutantShouldReturn(5);
    }

    @Test
    public void shouldReplaceInteger5With6(){
        v.forCallableClass(HasIntegerICONST5.class)
                .firstMutantShouldReturn(6);
    }

    @Test
    public void shouldReplaceLargeIntegerConstantsWithValuePlus1()
           {
        v.forCallableClass(HasIntegerLDC.class)
                .firstMutantShouldReturn(987654321 + 1);
    }

    /**
     * Note: Integer numbers and booleans are actually represented in the same way
     * be the JVM, it is therefore never safe if one changes a 0 to anything but a
     * 1 or a 1 to anything but a 0. Nevertheless if we find a -1 (ICONST_M1) it
     * must be an integer, short, byte or long. It won't be a boolean. So it is
     * always safe to replace -1 with 0.
     */
    @Test
    public void shouldReplaceIntegerMinus1With0(){
        v.forCallableClass(HasIntegerICONSTM1.class)
                .firstMutantShouldReturn(0);
    }

    @Test
    public void shouldReplaceIntegerMinus2WithMinus1(){
        v.forCallableClass(HasBIPUSHMinus2.class)
                .firstMutantShouldReturn(-1);
    }

    @Test
    public void shouldReplaceSmallIntegerConstantsWithValuePlus1()
           {
        v.forCallableClass(HasBIPUSH.class)
                .firstMutantShouldReturn(29);
    }

    @Test
    public void shouldReplaceMediumIntegerConstantsWithValuePlus1()
           {
        v.forCallableClass(HasSIPUSH.class)
                .firstMutantShouldReturn(32701);
    }

    @Test
    public void shouldReplaceFirstMutationPointOnly(){
        v.forCallableClass(HasTwoMutationPoints.class)
                .firstMutantShouldReturn(
                        Boolean.TRUE);
    }

    /**
     * The JVM does not have a short type, it uses integer under the hood.
     * <code>Short.MAX_VALUE</code> (=32767) will always be rolled to
     * <code>Short.MIN_VALUE</code> (=-32768). Note that the rolling will occur
     * even if only Integer/int variables are used! So <code>int i = 32767;</code>
     * will always be mutated to <code>int i = -32768;</code>.
     */
    @Test
    public void shouldOverflowOnShortMaxValue(){
        v.forCallableClass(HasShortOverflow.class)
                .firstMutantShouldReturn(Short.MIN_VALUE);
    }

    /**
     * The JVM does not have a short type, it uses integer under the hood.
     * <code>Short.MAX_VALUE</code> (=32767) will always be rolled to
     * <code>Short.MIN_VALUE</code> (=-32768). Note that the rolling will occur
     * even if only Integer/int variables are used! So <code>int i = 32767;</code>
     * will always be mutated to <code>int i = -32768;</code>.
     */
    @Test
    public void shouldOverflowIntegerOnShortMaxValue(){
        v.forCallableClass(HasIntegerAtMaxShortValue.class)
                .firstMutantShouldReturn(
                        (int) Short.MIN_VALUE);
    }

    /**
     * The JVM does not have a byte type, it uses integer under the hood.
     * <code>Byte.MAX_VALUE</code> (=127) will always be rolled to
     * <code>Byte.MIN_VALUE</code> (=-128). Note that the rolling will occur even
     * if only Integer/int variables are used! So <code>int i = 127;</code> will
     * always be mutated to <code>int i = -128;</code>.
     */
    @Test
    public void shouldOverflowOnByteMaxValue(){
        v.forCallableClass(HasByteOverflow.class)
                .firstMutantShouldReturn(Byte.MIN_VALUE);
    }

    @Test
    public void shouldReplaceIntegerMaxWithIntegerMin(){
        v.forCallableClass(HasIntegerMaxValue.class)
                .firstMutantShouldReturn(
                        Integer.MIN_VALUE);
    }

    @Test
    public void shouldReplaceLong0With1(){
        v.forCallableClass(HasLongLCONST0.class)
                .firstMutantShouldReturn(1L);
    }

    @Test
    public void shouldReplaceLong1With2(){
        v.forCallableClass(HasLongLCONST1.class)
                .firstMutantShouldReturn(2L);
    }

    @Test
    public void shouldReplaceLongWithValuePlus1(){
        v.forCallableClass(HasLongLDC.class)
                .firstMutantShouldReturn(3000000000L);
    }

    @Test
    public void shouldReplaceLongMinus1With0(){
        v.forCallableClass(HasLongLDCMinus1.class)
                .firstMutantShouldReturn(0L);
    }

    @Test
    public void shouldReplaceFloat0With1(){
        v.forCallableClass(HasFloatFCONST0.class)
                .firstMutantShouldReturn(1.0F);
    }

    @Test
    public void shouldReplaceFloat1With2(){
        v.forCallableClass(HasFloatFCONST1.class)
                .firstMutantShouldReturn(2.0F);
    }

    @Test
    public void shouldReplaceFloat2With1(){
        v.forCallableClass(HasFloatFCONST2.class)
                .firstMutantShouldReturn(1.0F);
    }

    @Test
    public void shouldReplaceFloatWith1(){
        v.forCallableClass(HasFloatLDC.class)
                .firstMutantShouldReturn(1.0F);
    }

    @Test
    public void shouldReplaceFirstFloatMutationPointOnly(){
        v.forCallableClass(HasFloatMultipleLDC.class)
                .firstMutantShouldReturn(4.0F);
    }

    @Test
    public void shouldReplaceDouble0With1(){
        v.forCallableClass(HasDoubleDCONST0.class)
                .firstMutantShouldReturn(1.0D);
    }

    @Test
    public void shouldReplaceDouble1With2(){
        v.forCallableClass(HasDoubleDCONST1.class)
                .firstMutantShouldReturn(2.0D);
    }

    @Test
    public void shouldReplaceDoubleWith1(){
        v.forCallableClass(HasDoubleLDC.class)
                .firstMutantShouldReturn(1.0D);
    }

    @Test
    public void shouldReplaceFirstDoubleMutationPointOnly(){
        v.forCallableClass(HasDoubleMultipleLDC.class)
                .firstMutantShouldReturn(2.0D);
    }

    private static class HasBooleanICONST0 implements Callable<Boolean> {

        @Override
        public Boolean call(){
            return false;
        }

    }

    private static class HasIntegerICONST0 implements Callable<Integer> {

        @Override
        public Integer call(){
            return 0;
        }

    }

    private static class HasBooleanICONST1 implements Callable<Boolean> {

        @Override
        public Boolean call(){
            return true;
        }

    }

    private static class HasIntegerICONST1 implements Callable<Integer> {

        @Override
        public Integer call(){
            return 1;
        }

    }

    private static class HasIntegerICONST2 implements Callable<Integer> {

        @Override
        public Integer call(){
            return 2;
        }

    }

    private static class HasIntegerICONST3 implements Callable<Integer> {

        @Override
        public Integer call(){
            return 3;
        }

    }

    private static class HasIntegerICONST4 implements Callable<Integer> {

        @Override
        public Integer call(){
            return 4;
        }

    }

    private static class HasIntegerICONST5 implements Callable<Integer> {

        @Override
        public Integer call(){
            return 5;
        }

    }

    private static class HasIntegerLDC implements Callable<Integer> {

        @Override
        public Integer call(){
            return 987654321;
        }

    }

    private static class HasIntegerICONSTM1 implements Callable<Integer> {

        @Override
        public Integer call(){
            return -1;
        }

    }

    private static class HasBIPUSHMinus2 implements Callable<Integer> {

        @Override
        public Integer call(){
            return -2;
        }

    }

    private static class HasBIPUSH implements Callable<Integer> {

        @Override
        public Integer call(){
            return 28;
        }

    }

    /*
     * Double and Float
     */

    private static class HasSIPUSH implements Callable<Integer> {

        @Override
        public Integer call(){
            return 32700;
        }

    }

    private static class HasTwoMutationPoints implements Callable<Boolean> {

        @Override
        public Boolean call(){
            int i = Short.MAX_VALUE;

            if (i != Short.MAX_VALUE) {
                i++; // prevent source formating from making final
                return Boolean.TRUE; // expected
            }

            return Boolean.FALSE;
        }

    }

    private static class HasShortOverflow implements Callable<Short> {

        @Override
        public Short call(){
            short s = Short.MAX_VALUE;
            s = preventSourceFormatingMakingFinal(s);
            return s;
        }

    }

    private static class HasIntegerAtMaxShortValue implements Callable<Integer> {

        @Override
        public Integer call(){
            int i = Short.MAX_VALUE;
            i = preventSourceFormatingMakingFinal(i);
            return i;
        }

    }

    private static class HasByteOverflow implements Callable<Byte> {

        @Override
        public Byte call(){
            byte b = Byte.MAX_VALUE;
            b = preventSourceFormatingMakingFinal(b);
            return b;
        }
    }

    private static class HasIntegerMaxValue implements Callable<Integer> {

        @Override
        public Integer call(){
            return Integer.MAX_VALUE;
        }

    }

    private static class HasLongLCONST0 implements Callable<Long> {

        @Override
        public Long call(){
            return 0L;
        }

    }

    private static class HasLongLCONST1 implements Callable<Long> {

        @Override
        public Long call(){
            return 1L;
        }

    }

    private static class HasLongLDC implements Callable<Long> {

        @Override
        public Long call(){
            return 2999999999L;
        }

    }

    private static class HasLongLDCMinus1 implements Callable<Long> {

        @Override
        public Long call(){
            return -1L;
        }

    }

    private static class HasFloatFCONST0 implements Callable<Float> {

        @Override
        public Float call(){
            return 0.0F;
        }

    }

    private static class HasFloatFCONST1 implements Callable<Float> {

        @Override
        public Float call(){
            return 1.0F;
        }

    }

    private static class HasFloatFCONST2 implements Callable<Float> {

        @Override
        public Float call(){
            return 2.0F;
        }

    }

    private static class HasFloatLDC implements Callable<Float> {

        @Override
        public Float call(){
            return 8364.123F;
        }

    }

    private static class HasFloatMultipleLDC implements Callable<Float> {

        @Override
        public Float call(){
            float f = 16.0F;
            float f2 = 4.0F;
            f = preventSourceFormatingMakingFinal(f);
            f2 = preventSourceFormatingMakingFinal(f2);
            return f * f2;
        }
    }

    private static class HasDoubleDCONST0 implements Callable<Double> {

        @Override
        public Double call(){
            return 0.0D;
        }

    }

    private static class HasDoubleDCONST1 implements Callable<Double> {

        @Override
        public Double call(){
            return 1.0D;
        }

    }

    private static class HasDoubleLDC implements Callable<Double> {

        @Override
        public Double call(){
            return 123456789.123D;
        }

    }

    private static class HasDoubleMultipleLDC implements Callable<Double> {

        @Override
        public Double call(){
            double d = 4578.1158D;
            double d2 = 2.0D;
            d = preventSourceFormatingMakingFinal(d);
            d2 = preventSourceFormatingMakingFinal(d2);
            return d * d2;
        }

    }

}