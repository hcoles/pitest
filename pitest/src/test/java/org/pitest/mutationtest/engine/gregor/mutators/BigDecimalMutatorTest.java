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
import org.pitest.verifier.mutants.MutatorVerifierStart;

import java.math.BigDecimal;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.pitest.mutationtest.engine.gregor.mutators.experimental.BigDecimalMutator.EXPERIMENTAL_BIG_DECIMAL;

public class BigDecimalMutatorTest {

    MutatorVerifierStart v = MutatorVerifierStart.forMutator(EXPERIMENTAL_BIG_DECIMAL)
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
    public void multiply() {
        v.forBiFunctionClass(Multiply.class)
                .firstMutantShouldReturn(2L, 2L, "1");
    }

    @Test
    public void divide() {
        v.forBiFunctionClass(Divide.class)
                .firstMutantShouldReturn(2L, 2L, "4");
    }

    @Test
    public void abs() {
        v.forBiFunctionClass(Abs.class)
                .firstMutantShouldReturn(-25L, 6L, "25");

        v.forBiFunctionClass(Abs.class)
                .firstMutantShouldReturn(25L, 6L, "-25");
    }

    @Test
    public void negate() {
        v.forBiFunctionClass(Negate.class)
                .firstMutantShouldReturn(0xFFFFFF00L, 0l, String.valueOf(0xFFFFFF00L));
        v.forBiFunctionClass(Negate.class)
                .firstMutantShouldReturn(0L, 0L, String.valueOf(0L));
        v.forBiFunctionClass(Negate.class)
                .firstMutantShouldReturn(0xFF00FF00L, 0l, String.valueOf(0xFF00FF00L));

    }

    @Test
    public void plus() {
        v.forBiFunctionClass(Plus.class)
                .firstMutantShouldReturn(BigDecimal.valueOf(0xFFFFFF00), null, BigDecimal.valueOf(0x00000100));
        v.forBiFunctionClass(Plus.class)
                .firstMutantShouldReturn(BigDecimal.ZERO, null, BigDecimal.ZERO);
        v.forBiFunctionClass(Plus.class)
                .firstMutantShouldReturn(BigDecimal.valueOf(0xFF00FF00), null, BigDecimal.valueOf(0x00FF0100));
    }

    @Test
    public void min() {
        v.forBiFunctionClass(Min.class)
                .firstMutantShouldReturn(-25L, 6L, "6");
        v.forBiFunctionClass(Min.class)
                .firstMutantShouldReturn(25L, 6L, "25");

    }

    @Test
    public void max() {
        v.forBiFunctionClass(Max.class)
                .firstMutantShouldReturn(-25L, 6L, "-25");

        v.forBiFunctionClass(Max.class)
                .firstMutantShouldReturn(25L, 6L, "6");
    }

    @Test
    public void addLambda() {
        v.forBiFunctionClass(AddLambda.class)
                .firstMutantShouldReturn(0L, 2L, "-2");
    }

    @Test
    public void subtractLambda() {
        v.forBiFunctionClass(SubtractLambda.class)
                .firstMutantShouldReturn(0L, 3L, "3");
    }

    @Test
    public void mutliplyLambda() {
        v.forBiFunctionClass(MultiplyLambda.class)
                .firstMutantShouldReturn(2L, 2L, "1");
    }

    @Test
    public void divideLambda() {
        v.forBiFunctionClass(DivideLambda.class)
                .firstMutantShouldReturn(2L, 2L, "4");
    }

    @Test
    public void absLambda() {
        v.forBiFunctionClass(AbsLambda.class)
                .firstMutantShouldReturn(-25L, 6L, "25");

        v.forBiFunctionClass(AbsLambda.class)
                .firstMutantShouldReturn(25L, 6L, "-25");
    }

    @Test
    public void negateLambda() {
        v.forBiFunctionClass(NegateLambda.class)
                .firstMutantShouldReturn(0xFFFFFF00L, 0L, String.valueOf(0xFFFFFF00L));
        v.forBiFunctionClass(NegateLambda.class)
                .firstMutantShouldReturn(0L, 0l, String.valueOf(0L));
        v.forBiFunctionClass(NegateLambda.class)
                .firstMutantShouldReturn(0xFF00FF00L, 0L, String.valueOf(0xFF00FF00L));
    }

    @Test
    public void plusLambda() {
        v.forBiFunctionClass(PlusLambda.class)
                .firstMutantShouldReturn(BigDecimal.valueOf(0xFFFFFF00), null, BigDecimal.valueOf(0x00000100));
        v.forBiFunctionClass(PlusLambda.class)
                .firstMutantShouldReturn(BigDecimal.ZERO, null, BigDecimal.valueOf(0L));
        v.forBiFunctionClass(PlusLambda.class)
                .firstMutantShouldReturn(BigDecimal.valueOf(0xFF00FF00), null, BigDecimal.valueOf(0x00FF0100));
    }

    @Test
    public void minLambda() {
        v.forBiFunctionClass(MinLambda.class)
                .firstMutantShouldReturn(-25L, 6L, "6");

        v.forBiFunctionClass(MinLambda.class)
                .firstMutantShouldReturn(25L, 6L, "25");
    }

    @Test
    public void maxLambda() {
        v.forBiFunctionClass(MaxLambda.class)
                .firstMutantShouldReturn(-25L, 6L, "-25");

        v.forBiFunctionClass(MaxLambda.class)
                .firstMutantShouldReturn(25L, 6L, "6");
    }

    private static abstract class AbstractMath implements BiFunction<Long, Long, String> {
        public String apply(Long left, Long right) {
            return String.valueOf(apply(BigDecimal.valueOf(left), BigDecimal.valueOf(right)));
        }

        abstract BigDecimal apply(BigDecimal left, BigDecimal right);
    }

    private static class Add extends AbstractMath {
        @Override
        BigDecimal apply(BigDecimal left, BigDecimal right) {
            return left.add(right);
        }
    }

    private static class Subtract extends AbstractMath {
        @Override
        BigDecimal apply(BigDecimal left, BigDecimal right) {
            return left.subtract(right);
        }
    }

    private static class Divide extends AbstractMath {
        @Override
        BigDecimal apply(BigDecimal left, BigDecimal right) {
            return left.divide(right);
        }
    }

    private static class Multiply extends AbstractMath {
        @Override
        BigDecimal apply(BigDecimal left, BigDecimal right) {
            return left.multiply(right);
        }
    }

    private static class Max extends AbstractMath {
        @Override
        BigDecimal apply(BigDecimal left, BigDecimal right) {
            return left.max(right);
        }
    }

    private static class Min extends AbstractMath {
        @Override
        BigDecimal apply(BigDecimal left, BigDecimal right) {
            return left.min(right);
        }
    }

    private static class Negate extends AbstractMath {
        @Override
        BigDecimal apply(BigDecimal left, BigDecimal right) {
            return left.negate();
        }
    }

    private static class Plus implements BiFunction<BigDecimal, BigDecimal, BigDecimal> {
        @Override
        public BigDecimal apply(BigDecimal left, BigDecimal right) {
            return left.plus();
        }
    }

    private static class Abs extends AbstractMath {
        @Override
        BigDecimal apply(BigDecimal left, BigDecimal right) {
            return left.abs();
        }
    }

    private static class AddLambda extends AbstractMath {
        @Override
        BigDecimal apply(BigDecimal left, BigDecimal right) {
            BiFunction<BigDecimal, BigDecimal, BigDecimal> function = BigDecimal::add;
            return function.apply(left, right);
        }
    }

    private static class SubtractLambda extends AbstractMath {
        @Override
        BigDecimal apply(BigDecimal left, BigDecimal right) {
            BiFunction<BigDecimal, BigDecimal, BigDecimal> function = BigDecimal::subtract;
            return function.apply(left, right);
        }
    }

    private static class DivideLambda extends AbstractMath {
        @Override
        BigDecimal apply(BigDecimal left, BigDecimal right) {
            BiFunction<BigDecimal, BigDecimal, BigDecimal> function = BigDecimal::divide;
            return function.apply(left, right);
        }
    }

    private static class MultiplyLambda extends AbstractMath {
        @Override
        BigDecimal apply(BigDecimal left, BigDecimal right) {
            BiFunction<BigDecimal, BigDecimal, BigDecimal> function = BigDecimal::multiply;
            return function.apply(left, right);
        }
    }

    private static class MaxLambda extends AbstractMath {
        @Override
        BigDecimal apply(BigDecimal left, BigDecimal right) {
            BiFunction<BigDecimal, BigDecimal, BigDecimal> function = BigDecimal::max;
            return function.apply(left, right);
        }
    }

    private static class MinLambda extends AbstractMath {
        @Override
        BigDecimal apply(BigDecimal left, BigDecimal right) {
            BiFunction<BigDecimal, BigDecimal, BigDecimal> function = BigDecimal::min;
            return function.apply(left, right);
        }
    }

    private static class NegateLambda extends AbstractMath {
        @Override
        BigDecimal apply(BigDecimal left, BigDecimal right) {
            Function<BigDecimal, BigDecimal> function = BigDecimal::negate;
            return function.apply(left);
        }
    }

    private static class PlusLambda implements BiFunction<BigDecimal, BigDecimal, BigDecimal> {
        @Override
        public BigDecimal apply(BigDecimal left, BigDecimal right) {
            Function<BigDecimal, BigDecimal> function = BigDecimal::plus;
            return function.apply(left);
        }
    }

    private static class AbsLambda extends AbstractMath {
        @Override
        BigDecimal apply(BigDecimal left, BigDecimal right) {
            Function<BigDecimal, BigDecimal> function = BigDecimal::abs;
            return function.apply(left);
        }
    }

}
