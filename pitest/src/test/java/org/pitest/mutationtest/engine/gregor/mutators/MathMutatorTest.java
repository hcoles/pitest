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

import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.LongFunction;

import static org.pitest.mutationtest.engine.gregor.mutators.MathMutator.MATH;

public class MathMutatorTest {

    MutatorVerifierStart v = MutatorVerifierStart.forMutator(MATH)
            .notCheckingUnMutatedValues();

    @Test
    public void shouldReplaceIntegerAdditionWithSubtraction() {
        v.forIntFunctionClass(HasIAdd.class)
                .firstMutantShouldReturn(2, "-40");
        v.forIntFunctionClass(HasIAdd.class)
                .firstMutantShouldReturn(20, "-22");
    }

    @Test
    public void shouldReplaceIntegerSubtractionWithAddition() {
        v.forIntFunctionClass(HasISub.class)
                .firstMutantShouldReturn(2, "44");
        v.forIntFunctionClass(HasISub.class)
                .firstMutantShouldReturn(20, "62");
    }

    @Test
    public void shouldReplaceIntegerMultiplicationWithDivision() {
        v.forIntFunctionClass(HasIMul.class)
                .firstMutantShouldReturn(2, "1");
        v.forIntFunctionClass(HasIMul.class)
                .firstMutantShouldReturn(20, "10");
    }

    @Test
    public void shouldReplaceIntegerDivisionWithMultiplication() {
        v.forIntFunctionClass(HasIDiv.class)
                .firstMutantShouldReturn(2, "4");
        v.forIntFunctionClass(HasIDiv.class)
                .firstMutantShouldReturn(20, "40");
    }

    @Test
    public void shouldReplaceIntegerBitwiseOrsWithAnds() {
        v.forIntFunctionClass(HasIOr.class)
                .firstMutantShouldReturn(2, "" + (2L & 2));
        v.forIntFunctionClass(HasIOr.class)
                .firstMutantShouldReturn(4, "" + (4L & 2));
    }

    @Test
    public void shouldReplaceIntegerBitwiseAndsWithOrs() {
        v.forIntFunctionClass(HasIAnd.class)
                .firstMutantShouldReturn(2, "2");
        v.forIntFunctionClass(HasIAnd.class)
                .firstMutantShouldReturn(4, "6");
    }

    @Test
    public void shouldReplaceIntegerModulusWithMultiplication() {
        v.forIntFunctionClass(HasIRem.class)
                .firstMutantShouldReturn(2, "4");
        v.forIntFunctionClass(HasIRem.class)
                .firstMutantShouldReturn(3, "6");
    }

    @Test
    public void shouldReplaceIntegerXORWithAND() {
        v.forIntFunctionClass(HasIXor.class)
                .firstMutantShouldReturn(2, "2");
        v.forIntFunctionClass(HasIXor.class)
                .firstMutantShouldReturn(1, "0");
    }

    @Test
    public void shouldReplaceIntegerShiftLeftWithShiftRight() {
        v.forIntFunctionClass(HasISHL.class)
                .firstMutantShouldReturn(100, "25");
        v.forIntFunctionClass(HasISHL.class)
                .firstMutantShouldReturn(20, "5");
    }

    @Test
    public void shouldReplaceIntegerShiftRightWithShiftLeft() {
        v.forIntFunctionClass(HasISHR.class)
                .firstMutantShouldReturn(100, "400");
        v.forIntFunctionClass(HasISHR.class)
                .firstMutantShouldReturn(20, "80");
    }

    @Test
    public void shouldReplaceIntegerUnsignedShiftRightWithShiftLeft() {
        v.forIntFunctionClass(HasIUSHR.class)
                .firstMutantShouldReturn(100, "400");
        v.forIntFunctionClass(HasIUSHR.class)
                .firstMutantShouldReturn(20, "80");
    }

    @Test
    public void shouldReplaceLongAdditionWithSubtraction() {
        v.forLongFunctionClass(HasLAdd.class)
                .firstMutantShouldReturn(2L, "1");
        v.forLongFunctionClass(HasLAdd.class)
                .firstMutantShouldReturn(20L, "19");
    }

    @Test
    public void shouldReplaceLongSubtractionWithAddition() {
        v.forLongFunctionClass(HasLSub.class)
                .firstMutantShouldReturn(2, "3");
        v.forLongFunctionClass(HasLSub.class)
                .firstMutantShouldReturn(20, "21");
    }

    @Test
    public void shouldReplaceLongMultiplicationWithDivision() {
        v.forLongFunctionClass(HasLMul.class)
                .firstMutantShouldReturn(2, "1");
        v.forLongFunctionClass(HasLMul.class)
                .firstMutantShouldReturn(20, "10");
    }

    @Test
    public void shouldReplaceLongDivisionWithMultiplication() {
        v.forLongFunctionClass(HasLDiv.class)
                .firstMutantShouldReturn(2, "4");
        v.forLongFunctionClass(HasLDiv.class)
                .firstMutantShouldReturn(20, "40");
    }

    @Test
    public void shouldReplaceLongBitwiseOrsWithAnds() {
          v.forLongFunctionClass(HasLOr.class)
                .firstMutantShouldReturn(2L, "2");
        v.forLongFunctionClass(HasLOr.class)
                .firstMutantShouldReturn(4L, "0");
    }

    @Test
    public void shouldReplaceLongBitwiseAndsWithOrs() {
        v.forLongFunctionClass(HasLAnd.class)
                .firstMutantShouldReturn(2, "2");
        v.forLongFunctionClass(HasLAnd.class)
                .firstMutantShouldReturn(4, "6");
    }

    @Test
    public void shouldReplaceLongModulusWithMultiplication() {
        v.forLongFunctionClass(HasLRem.class)
                .firstMutantShouldReturn(2, "4");
        v.forLongFunctionClass(HasLRem.class)
                .firstMutantShouldReturn(3, "6");
    }

    @Test
    public void shouldReplaceLongXORWithAND() {
        v.forLongFunctionClass(HasLXor.class)
                .firstMutantShouldReturn(2, "2");
        v.forLongFunctionClass(HasLXor.class)
                .firstMutantShouldReturn(1, "0");
    }

    @Test
    public void shouldReplaceLongShiftLeftWithShiftRight() {
        v.forLongFunctionClass(HasLSHL.class)
                .firstMutantShouldReturn(100, "25");
        v.forLongFunctionClass(HasLSHL.class)
                .firstMutantShouldReturn(20, "5");
    }

    @Test
    public void shouldReplaceLongShiftRightWithShiftLeft() {
        v.forLongFunctionClass(HasLSHR.class)
                .firstMutantShouldReturn(100, "400");
        v.forLongFunctionClass(HasLSHR.class)
                .firstMutantShouldReturn(20, "80");
    }

    @Test
    public void shouldReplaceLongUnsignedShiftRightWithShiftLeft() {
        v.forLongFunctionClass(HasLUSHR.class)
                .firstMutantShouldReturn(100, "400");
        v.forLongFunctionClass(HasLUSHR.class)
                .firstMutantShouldReturn(20, "80");
    }

    // LONGS

    @Test
    public void shouldReplaceFloatAdditionWithSubtraction() {
        v.forFunctionClass(HasFADD.class)
                .firstMutantShouldReturn(2f, "1.0");
        v.forFunctionClass(HasFADD.class)
                .firstMutantShouldReturn(20f, "19.0");
    }

    @Test
    public void shouldReplaceFloatSubtractionWithAddition() {
        v.forFunctionClass(HasFSUB.class)
                .firstMutantShouldReturn(2f, "3.0");
        v.forFunctionClass(HasFSUB.class)
                .firstMutantShouldReturn(20f, "21.0");
    }

    @Test
    public void shouldReplaceFloatMultiplicationWithDivision() {
        v.forFunctionClass(HasFMUL.class)
                .firstMutantShouldReturn(2f, "1.0");
        v.forFunctionClass(HasFMUL.class)
                .firstMutantShouldReturn(20f, "10.0");
    }

    @Test
    public void shouldReplaceFloatDivisionWithMultiplication() {
        v.forFunctionClass(HasFDIV.class)
                .firstMutantShouldReturn(2f, "4.0");
        v.forFunctionClass(HasFDIV.class)
                .firstMutantShouldReturn(20f, "40.0");
    }

    @Test
    public void shouldReplaceFloatModulusWithMultiplication() {
        v.forFunctionClass(HasFREM.class)
                .firstMutantShouldReturn(2f, "4.0");
        v.forFunctionClass(HasFREM.class)
                .firstMutantShouldReturn(3f, "6.0");
    }

    @Test
    public void shouldReplaceDoubleAdditionWithSubtraction() {
        v.forDoubleFunctionClass(HasDADD.class)
                .firstMutantShouldReturn(2D, "1.0");
        v.forDoubleFunctionClass(HasDADD.class)
                .firstMutantShouldReturn(20D, "19.0");
    }

    @Test
    public void shouldReplaceDoubleSubtractionWithAddition() {
        v.forDoubleFunctionClass(HasDSUB.class)
                .firstMutantShouldReturn(2, "3.0");
        v.forDoubleFunctionClass(HasDSUB.class)
                .firstMutantShouldReturn(20, "21.0");
    }

    @Test
    public void shouldReplaceDoubleMultiplicationWithDivision() {
        v.forDoubleFunctionClass(HasDMUL.class)
                .firstMutantShouldReturn(2, "1.0");
        v.forDoubleFunctionClass(HasDMUL.class)
                .firstMutantShouldReturn(20, "10.0");
    }

    @Test
    public void shouldReplaceDoubleDivisionWithMultiplication() {
        v.forDoubleFunctionClass(HasDDIV.class)
                .firstMutantShouldReturn(2, "4.0");
        v.forDoubleFunctionClass(HasDDIV.class)
                .firstMutantShouldReturn(20, "40.0");
    }

    @Test
    public void shouldReplaceDoublerModulusWithMultiplication() {
        v.forDoubleFunctionClass(HasDREM.class)
                .firstMutantShouldReturn(2, "4.0");
        v.forDoubleFunctionClass(HasDREM.class)
                .firstMutantShouldReturn(3, "6.0");
    }

    private static class HasIAdd implements IntFunction<String> {

        @Override
        public String apply(int i) {
            i = i + 42;
            return "" + i;
        }
    }

    private static class HasISub implements IntFunction<String> {

        @Override
        public String apply(int i) {
            i = i - 42;
            return "" + i;
        }
    }

    private static class HasIMul implements IntFunction<String> {
        @Override
        public String apply(int i) {
            i = i * 2;
            return "" + i;
        }
    }

    private static class HasIDiv implements IntFunction<String> {
        @Override
        public String apply(int i) {
            i = i / 2;
            return "" + i;
        }
    }

    private static class HasIOr implements IntFunction<String> {

        @Override
        public String apply(int i) {
            i = i | 2;
            return "" + i;
        }
    }

    private static class HasIAnd implements IntFunction<String> {
        @Override
        public String apply(int i) {
            i = i & 2;
            return "" + i;
        }
    }

    private static class HasIRem implements IntFunction<String> {
        @Override
        public String apply(int i) {
            i = i % 2;
            return "" + i;
        }
    }

    private static class HasIXor implements IntFunction<String> {
        @Override
        public String apply(int i) {
            i = i ^ 2;
            return "" + i;
        }
    }

    private static class HasISHL implements IntFunction<String> {
        @Override
        public String apply(int i) {
            i = i << 2;
            return "" + i;
        }
    }

    private static class HasISHR implements IntFunction<String> {
        @Override
        public String apply(int i) {
            i = i >> 2;
            return "" + i;
        }
    }

    private static class HasIUSHR implements IntFunction<String> {
        @Override
        public String apply(int i) {
            i = i >>> 2;
            return "" + i;
        }
    }

    private static class HasLAdd implements LongFunction<String> {
        @Override
        public String apply(long i) {
            i++;
            return "" + i;
        }
    }

    // FLOATS

    private static class HasLSub implements LongFunction<String> {

        @Override
        public String apply(long i) {
            i--;
            return "" + i;
        }
    }

    private static class HasLMul implements LongFunction<String> {
        @Override
        public String apply(long i) {
            i = i * 2;
            return "" + i;
        }
    }

    private static class HasLDiv implements LongFunction<String> {

        @Override
        public String apply(long i) {
            i = i / 2;
            return "" + i;
        }
    }

    private static class HasLOr implements LongFunction<String> {
        @Override
        public String apply(long i) {
            i = i | 2;
            return "" + i;
        }
    }

    private static class HasLAnd implements LongFunction<String> {
        @Override
        public String apply(long i) {
            i = i & 2;
            return "" + i;
        }
    }

    private static class HasLRem implements LongFunction<String> {
        @Override
        public String apply(long i) {
            i = i % 2;
            return "" + i;
        }
    }

    private static class HasLXor implements LongFunction<String> {
        @Override
        public String apply(long i) {
            i = i ^ 2;
            return "" + i;
        }
    }

    private static class HasLSHL implements LongFunction<String> {
        @Override
        public String apply(long i) {
            i = i << 2;
            return "" + i;
        }
    }

    private static class HasLSHR implements LongFunction<String> {
        @Override
        public String apply(long i) {
            i = i >> 2;
            return "" + i;
        }
    }

    private static class HasLUSHR implements LongFunction<String> {
        @Override
        public String apply(long i) {
            i = i >>> 2;
            return "" + i;
        }
    }

    // double

    private static class HasFADD implements Function<Float, String> {
        @Override
        public String apply(Float f) {
            float i = f.floatValue();
            i++;
            return "" + i;
        }
    }

    private static class HasFSUB implements Function<Float, String> {
        @Override
        public String apply(Float f) {
            float i = f.floatValue();
            i--;
            return "" + i;
        }
    }

    private static class HasFMUL implements Function<Float, String> {
        @Override
        public String apply(Float f) {
            float i = f.floatValue();
            i = i * 2;
            return "" + i;
        }
    }

    private static class HasFDIV implements Function<Float, String> {
        @Override
        public String apply(Float f) {
            float i = f.floatValue();
            i = i / 2;
            return "" + i;
        }
    }

    private static class HasFREM implements Function<Float, String> {
        @Override
        public String apply(Float f) {
            float i = f.floatValue();
            i = i % 2;
            return "" + i;
        }
    }

    private static class HasDADD implements DoubleFunction<String> {
        @Override
        public String apply(double i) {
            i++;
            return "" + i;
        }
    }

    private static class HasDSUB implements DoubleFunction<String> {

        @Override
        public String apply(double i) {
            i--;
            return "" + i;
        }
    }

    private static class HasDMUL implements DoubleFunction<String> {
        @Override
        public String apply(double i) {
            i = i * 2;
            return "" + i;
        }
    }

    private static class HasDDIV implements DoubleFunction<String> {
        @Override
        public String apply(double i) {
            i = i / 2;
            return "" + i;
        }
    }

    private static class HasDREM implements DoubleFunction<String> {
        @Override
        public String apply(double i) {
            i = i % 2;
            return "" + i;
        }
    }

}
