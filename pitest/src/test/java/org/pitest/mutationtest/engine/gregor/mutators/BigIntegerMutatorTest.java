/*
 * Copyright 2010 Henry Coles
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
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.mutationtest.engine.gregor.mutators;

import org.junit.Test;
import org.pitest.verifier.mutants.BiFunctionMutantVerifier;
import org.pitest.verifier.mutants.MutatorVerifierStart;

import java.math.BigInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import static org.pitest.mutationtest.engine.gregor.mutators.experimental.BigIntegerMutator.EXPERIMENTAL_BIG_INTEGER;

public class BigIntegerMutatorTest {

    MutatorVerifierStart v = MutatorVerifierStart.forMutator(EXPERIMENTAL_BIG_INTEGER)
            .notCheckingUnMutatedValues();

    @Test
    public void add() {
        v.forBiFunctionClass(Add.class)
                .firstMutantShouldReturn(0L, 2L, "-2");
    }

    @Test
    public void subtract() {
        v.forBiFunctionClass(Subtract.class)
                .firstMutantShouldReturn(0L, 3L, "3");
    }

    @Test
    public void mutliply() {
        v.forBiFunctionClass(Multiply.class)
                .firstMutantShouldReturn(2L, 2L, "1");
    }

    @Test
    public void divide() {
        v.forBiFunctionClass(Divide.class)
                .firstMutantShouldReturn(
                        2L, 2L, "4");
    }

    @Test
    public void modulo() {
        v.forBiFunctionClass(Modulo.class)
                .firstMutantShouldReturn(
                        25L, 5L, "125");
    }

    @Test
    public void abs() {
        v.forBiFunctionClass(Abs.class)
                .firstMutantShouldReturn(
                        -25L, 6L, "25");

        v.forBiFunctionClass(Abs.class)
                .firstMutantShouldReturn(
                        25L, 6L, "-25");

    }

    @Test
    public void not() {
        v.forBiFunctionClass(Not.class)
                .firstMutantShouldReturn(
                        0b1111L, 0L, String.valueOf(-0b1111));

        v.forBiFunctionClass(Not.class)
                .firstMutantShouldReturn(
                        0b1111L, 0L, String.valueOf(-0b1111));
    }

    @Test
    public void negate() {
        v.forBiFunctionClass(Negate.class)
                .firstMutantShouldReturn(
                        BigInteger.valueOf(0xFFFFFF00), null, BigInteger.valueOf(0x000000FF));
        v.forBiFunctionClass(Negate.class)
                .firstMutantShouldReturn(
                        BigInteger.ZERO, null, BigInteger.valueOf(0xFFFFFFFF));
        v.forBiFunctionClass(Negate.class)
                .firstMutantShouldReturn(
                        BigInteger.valueOf(0xFF00FF00), null, BigInteger.valueOf(0x00FF00FF));
    }

    @Test
    public void min() {
        v.forBiFunctionClass(Min.class)
                .firstMutantShouldReturn(
                        -25L, 6L, "6");
        v.forBiFunctionClass(Min.class)
                .firstMutantShouldReturn(
                        25L, 6L, "25");
    }

    @Test
    public void max() {
        v.forBiFunctionClass(Max.class)
                .firstMutantShouldReturn(
                        -25L, 6L, "-25");
        v.forBiFunctionClass(Max.class)
                .firstMutantShouldReturn(
                        25L, 6L, "6");
    }

    @Test
    public void setBit() {
        v.forBiFunctionClass(SetBit.class)
                .firstMutantShouldReturn(
                        0b11101L, 2L, String.valueOf(0b11001));

        v.forBiFunctionClass(SetBit.class)
                .firstMutantShouldReturn(
                        0b11001L, 2L, String.valueOf(0b11001));

    }

    @Test
    public void clearBit() {
        v.forBiFunctionClass(ClearBit.class)
                .firstMutantShouldReturn(
                        0b11101L, 2L, String.valueOf(0b11101));

        v.forBiFunctionClass(ClearBit.class)
                .firstMutantShouldReturn(
                        0b01101L, 0L, String.valueOf(0b01101));
    }

    @Test
    public void flipBit() {
        v.forBiFunctionClass(FlipBit.class)
                .firstMutantShouldReturn(
                        0b11101L, 2L, String.valueOf(0b11101));
        v.forBiFunctionClass(FlipBit.class)
                .firstMutantShouldReturn(
                        0b01101L, 0L, String.valueOf(0b01101));
    }

    @Test
    public void shiftLeft() {
        v.forBiFunctionClass(ShiftLeft.class)
                .firstMutantShouldReturn(
                        1L, 1L, String.valueOf(1 >> 1));
        v.forBiFunctionClass(ShiftLeft.class)
                .firstMutantShouldReturn(
                        1L, 2L, String.valueOf(1 >> 2));
        v.forBiFunctionClass(ShiftLeft.class)
                .firstMutantShouldReturn(
                        1L << 8, 8L, String.valueOf(1));
        v.forBiFunctionClass(ShiftLeft.class)
                .firstMutantShouldReturn(
                        1L << 8, 4L, String.valueOf(1 << 8 >> 4));
    }

    @Test
    public void shiftRight() {
        v.forBiFunctionClass(ShiftRight.class)
                .firstMutantShouldReturn(
                        1L, 1L, String.valueOf(1 << 1));
        v.forBiFunctionClass(ShiftRight.class)
                .firstMutantShouldReturn(
                        1L, 2L, String.valueOf(1 << 2));
        v.forBiFunctionClass(ShiftRight.class)
                .firstMutantShouldReturn(
                        1L << 8, 8L, String.valueOf(1L << 16L));
        v.forBiFunctionClass(ShiftRight.class)
                .firstMutantShouldReturn(
                        1L << 8, 4L, String.valueOf(1 << 8 << 4));
    }

    @Test
    public void and() {
        v.forBiFunctionClass(And.class)
                .firstMutantShouldReturn(
                        0b01L, 0b10L, String.valueOf(0b11));
        v.forBiFunctionClass(And.class)
                .firstMutantShouldReturn(
                        0b01L, 0b01L, String.valueOf(0b01));
        v.forBiFunctionClass(And.class)
                .firstMutantShouldReturn(
                        0b01L, 0b00L, String.valueOf(0b01));
    }

    @Test
    public void or() {
        v.forBiFunctionClass(Or.class)
                .firstMutantShouldReturn(
                        0b01L, 0b10L, String.valueOf(0b00));
        v.forBiFunctionClass(Or.class)
                .firstMutantShouldReturn(
                        0b01L, 0b01L, String.valueOf(0b01));
        v.forBiFunctionClass(Or.class)
                .firstMutantShouldReturn(
                        0b11L, 0b10L, String.valueOf(0b10));
    }

    @Test
    public void xor() {
        v.forBiFunctionClass(Xor.class)
                .firstMutantShouldReturn(
                        0b01L, 0b10L, String.valueOf(0b00));
        v.forBiFunctionClass(Xor.class)
                .firstMutantShouldReturn(
                        0b01L, 0b01L, String.valueOf(0b01));
        v.forBiFunctionClass(Xor.class)
                .firstMutantShouldReturn(
                        0b11L, 0b10L, String.valueOf(0b10));
    }

    @Test
    public void andNot() {
        v.forBiFunctionClass(AndNot.class)
                .firstMutantShouldReturn(
                        0b01L, 0b10L, String.valueOf(0b00));
        v.forBiFunctionClass(AndNot.class)
                .firstMutantShouldReturn(
                        0b01L, 0b01L, String.valueOf(0b01));
        v.forBiFunctionClass(AndNot.class)
                .firstMutantShouldReturn(
                        0b11L, 0b10L, String.valueOf(0b10));
    }

    @Test
    public void addLambda() {
        v.forBiFunctionClass(AddLambda.class)
                .firstMutantShouldReturn(
                        0L, 2L, "-2");
    }

    @Test
    public void subtractLambda() {
        v.forBiFunctionClass(SubtractLambda.class)
                .firstMutantShouldReturn(
                        0L, 3L, "3");
    }

    @Test
    public void mutliplyLambda() {
        v.forBiFunctionClass(MultiplyLambda.class)
                .firstMutantShouldReturn(
                        2L, 2L, "1");
    }

    @Test
    public void divideLambda() {
        v.forBiFunctionClass(DivideLambda.class)
                .firstMutantShouldReturn(
                        2L, 2L, "4");
    }

    @Test
    public void moduloLambda() {
        v.forBiFunctionClass(ModuloLambda.class)
                .firstMutantShouldReturn(
                        25L, 5L, "125");
    }

    @Test
    public void absLambda() {
        v.forBiFunctionClass(AbsLambda.class)
                .firstMutantShouldReturn(
                        -25L, 6L, "25");
        v.forBiFunctionClass(AbsLambda.class)
                .firstMutantShouldReturn(
                        25L, 6L, "-25");
    }

    @Test
    public void notLambda() {
        v.forBiFunctionClass(NotLambda.class)
                .firstMutantShouldReturn(
                        0b1111L, 0L, String.valueOf(-0b1111L));
        v.forBiFunctionClass(NotLambda.class)
                .firstMutantShouldReturn(
                        0b1111L, 0L, String.valueOf(-0b1111L));
    }

    @Test
    public void negateLambda() {
        BiFunctionMutantVerifier<BigInteger, BigInteger, BigInteger> v2 = v.forBiFunctionClass(NegateLambda.class);
        v2.firstMutantShouldReturn(
                BigInteger.valueOf(0xFFFFFF00), null, BigInteger.valueOf(0x000000FF));
        v2.firstMutantShouldReturn(
                BigInteger.ZERO, null, BigInteger.valueOf(0xFFFFFFFF));
        v2.firstMutantShouldReturn(
                BigInteger.valueOf(0xFF00FF00), null, BigInteger.valueOf(0x00FF00FF));
    }

    @Test
    public void minLambda() {
        v.forBiFunctionClass(MinLambda.class)
                .firstMutantShouldReturn(
                        -25L, 6L, "6");
        v.forBiFunctionClass(MinLambda.class)
                .firstMutantShouldReturn(
                        25L, 6L, "25");
    }

    @Test
    public void maxLambda() {
        v.forBiFunctionClass(MaxLambda.class)
                .firstMutantShouldReturn(
                        -25L, 6L, "-25");
        v.forBiFunctionClass(MaxLambda.class)
                .firstMutantShouldReturn(
                        25L, 6L, "6");
    }

    @Test
    public void setBitLambda() {
        v.forBiFunctionClass(SetBitLambda.class)
                .firstMutantShouldReturn(
                        0b11101L, 2L, String.valueOf(0b11001));
        v.forBiFunctionClass(SetBitLambda.class)
                .firstMutantShouldReturn(
                        0b11001L, 2L, String.valueOf(0b11001));
    }

    @Test
    public void clearBitLambda() {
        v.forBiFunctionClass(ClearBitLambda.class)
                .firstMutantShouldReturn(
                        0b11101L, 2L, String.valueOf(0b11101));
        v.forBiFunctionClass(ClearBitLambda.class)
                .firstMutantShouldReturn(
                        0b01101L, 0L, String.valueOf(0b01101));
    }

    @Test
    public void flipBitLambda() {
        v.forBiFunctionClass(FlipBitLambda.class)
                .firstMutantShouldReturn(
                        0b11101L, 2L, String.valueOf(0b11101));
        v.forBiFunctionClass(FlipBitLambda.class)
                .firstMutantShouldReturn(
                        0b01101L, 0L, String.valueOf(0b01101));
    }

    @Test
    public void shiftLeftLambda() {
        v.forBiFunctionClass(ShiftLeftLambda.class)
                .firstMutantShouldReturn(
                        1L, 1L, String.valueOf(1 >> 1));
        v.forBiFunctionClass(ShiftLeftLambda.class)
                .firstMutantShouldReturn(
                        1L, 2L, String.valueOf(1 >> 2));
        v.forBiFunctionClass(ShiftLeftLambda.class)
                .firstMutantShouldReturn(
                        1L << 8, 8L, String.valueOf(1));
        v.forBiFunctionClass(ShiftLeftLambda.class)
                .firstMutantShouldReturn(
                        1L << 8, 4L, String.valueOf(1 << 8 >> 4));
    }

    @Test
    public void shiftRightLambda() {
        v.forBiFunctionClass(ShiftRightLambda.class)
                .firstMutantShouldReturn(
                        1L, 1L, String.valueOf(1 << 1));
        v.forBiFunctionClass(ShiftRightLambda.class)
                .firstMutantShouldReturn(
                        1L, 2L, String.valueOf(1 << 2));
        v.forBiFunctionClass(ShiftRightLambda.class)
                .firstMutantShouldReturn(
                        1L << 8, 8L, String.valueOf(1L << 16L));
        v.forBiFunctionClass(ShiftRightLambda.class)
                .firstMutantShouldReturn(
                        1L << 8, 4L, String.valueOf(1 << 8 << 4));
    }

    @Test
    public void andLambda() {
        v.forBiFunctionClass(AndLambda.class)
                .firstMutantShouldReturn(
                        0b01L, 0b10L, String.valueOf(0b11));
        v.forBiFunctionClass(AndLambda.class)
                .firstMutantShouldReturn(
                        0b01L, 0b01L, String.valueOf(0b01));
        v.forBiFunctionClass(AndLambda.class)
                .firstMutantShouldReturn(
                        0b01L, 0b00L, String.valueOf(0b01));
    }

    @Test
    public void orLambda() {
        v.forBiFunctionClass(OrLambda.class)
                .firstMutantShouldReturn(
                        0b01L, 0b10L, String.valueOf(0b00));
        v.forBiFunctionClass(OrLambda.class)
                .firstMutantShouldReturn(
                        0b01L, 0b01L, String.valueOf(0b01));
        v.forBiFunctionClass(OrLambda.class)
                .firstMutantShouldReturn(
                        0b11L, 0b10L, String.valueOf(0b10));
    }

    @Test
    public void xorLambda() {
        v.forBiFunctionClass(XorLambda.class)
                .firstMutantShouldReturn(
                        0b01L, 0b10L, String.valueOf(0b00));
        v.forBiFunctionClass(XorLambda.class)
                .firstMutantShouldReturn(
                        0b01L, 0b01L, String.valueOf(0b01));
        v.forBiFunctionClass(XorLambda.class)
                .firstMutantShouldReturn(
                        0b11L, 0b10L, String.valueOf(0b10));
    }

    @Test
    public void andNotLambda() {
        v.forBiFunctionClass(AndNotLambda.class)
                .firstMutantShouldReturn(
                        0b01L, 0b10L, String.valueOf(0b00));
        v.forBiFunctionClass(AndNotLambda.class)
                .firstMutantShouldReturn(
                        0b01L, 0b01L, String.valueOf(0b01));
        v.forBiFunctionClass(AndNotLambda.class)
                .firstMutantShouldReturn(
                        0b11L, 0b10L, String.valueOf(0b10));
    }

    private static abstract class AbstractMath implements BiFunction<Long, Long, String> {

        @Override
        public String apply(Long left, Long right) {
            return String.valueOf(apply(BigInteger.valueOf(left), BigInteger.valueOf(right)));
        }

        abstract BigInteger apply(BigInteger left, BigInteger right);

    }

    private static class Add extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            return left.add(right);
        }
    }

    private static class Subtract extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            return left.subtract(right);
        }
    }

    private static class Divide extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            return left.divide(right);
        }
    }

    private static class Multiply extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            return left.multiply(right);
        }
    }

    private static class Modulo extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            return left.mod(right);
        }
    }

    private static class SetBit extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            return left.setBit(right.intValue());
        }
    }

    private static class ClearBit extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            return left.clearBit(right.intValue());
        }
    }

    private static class FlipBit extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            return left.flipBit(right.intValue());
        }
    }

    private static class ShiftLeft extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            return left.shiftLeft(right.intValue());
        }
    }

    private static class ShiftRight extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            return left.shiftRight(right.intValue());
        }
    }

    private static class And extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            return left.and(right);
        }
    }

    private static class Or extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            return left.or(right);
        }
    }

    private static class Xor extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            return left.xor(right);
        }
    }

    private static class AndNot extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            return left.andNot(right);
        }
    }

    private static class Max extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            return left.max(right);
        }
    }

    private static class Min extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            return left.min(right);
        }
    }

    private static class Not extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            return left.not();
        }
    }

    private static class Negate implements BiFunction<BigInteger, BigInteger, BigInteger> {
        @Override
        public BigInteger apply(BigInteger left, BigInteger right) {
            return left.negate();
        }
    }

    private static class Abs extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            return left.abs();
        }
    }

    private static class AddLambda extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            BiFunction<BigInteger, BigInteger, BigInteger> function = BigInteger::add;
            return function.apply(left, right);
        }
    }

    private static class SubtractLambda extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            BiFunction<BigInteger, BigInteger, BigInteger> function = BigInteger::subtract;
            return function.apply(left, right);
        }
    }

    private static class DivideLambda extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            BiFunction<BigInteger, BigInteger, BigInteger> function = BigInteger::divide;
            return function.apply(left, right);
        }
    }

    private static class MultiplyLambda extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            BiFunction<BigInteger, BigInteger, BigInteger> function = BigInteger::multiply;
            return function.apply(left, right);
        }
    }

    private static class SetBitLambda extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            IntFunction<BigInteger> function = left::setBit;
            return function.apply(right.intValue());
        }
    }

    private static class ClearBitLambda extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            IntFunction<BigInteger> function = left::clearBit;
            return function.apply(right.intValue());
        }
    }

    private static class FlipBitLambda extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            IntFunction<BigInteger> function = left::flipBit;
            return function.apply(right.intValue());
        }
    }

    private static class ShiftLeftLambda extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            IntFunction<BigInteger> function = left::shiftLeft;
            return function.apply(right.intValue());
        }
    }

    private static class ShiftRightLambda extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            IntFunction<BigInteger> function = left::shiftRight;
            return function.apply(right.intValue());
        }
    }

    private static class AndLambda extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            BiFunction<BigInteger, BigInteger, BigInteger> function = BigInteger::and;
            return function.apply(left, right);
        }
    }

    private static class OrLambda extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            BiFunction<BigInteger, BigInteger, BigInteger> function = BigInteger::or;
            return function.apply(left, right);
        }
    }

    private static class XorLambda extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            BiFunction<BigInteger, BigInteger, BigInteger> function = BigInteger::xor;
            return function.apply(left, right);
        }
    }

    private static class AndNotLambda extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            BiFunction<BigInteger, BigInteger, BigInteger> function = BigInteger::andNot;
            return function.apply(left, right);
        }
    }

    private static class MaxLambda extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            BiFunction<BigInteger, BigInteger, BigInteger> function = BigInteger::max;
            return function.apply(left, right);
        }
    }

    private static class MinLambda extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            BiFunction<BigInteger, BigInteger, BigInteger> function = BigInteger::min;
            return function.apply(left, right);
        }
    }

    private static class NotLambda extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            Supplier<BigInteger> function = left::not;
            return function.get();
        }
    }

    private static class NegateLambda implements BiFunction<BigInteger, BigInteger, BigInteger> {
        @Override
        public BigInteger apply(BigInteger left, BigInteger right) {
            Function<BigInteger, BigInteger> function = BigInteger::negate;
            return function.apply(left);
        }
    }

    private static class AbsLambda extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            Function<BigInteger, BigInteger> function = BigInteger::abs;
            return function.apply(left);
        }
    }

    private static class ModuloLambda extends AbstractMath {
        @Override
        BigInteger apply(BigInteger left, BigInteger right) {
            BiFunction<BigInteger, BigInteger, BigInteger> function = BigInteger::mod;
            return function.apply(left, right);
        }
    }
}
