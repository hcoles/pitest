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
package org.pitest.mutationtest.engine.gregor.mutators.experimental;

import org.junit.Test;
import org.pitest.verifier.mutants.MutatorVerifierStart;

import java.util.concurrent.Callable;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class ReturnValuesMutatorTest {

    MutatorVerifierStart v = MutatorVerifierStart.forMutator(new ReturnValuesMutator())
            .notCheckingUnMutatedValues();

    @Test
    public void shouldProvideAMeaningfulName() {
        assertEquals("EXPERIMENTAL_RETURN_VALUES_MUTATOR",
                new ReturnValuesMutator().getName());
    }

    @Test
    public void shouldMutateReturnOfPrimitiveBooleanTrueToFalse() {
        v.forFunctionClass(HasPrimitiveBooleanReturn.class)
                .firstMutantShouldReturn(true, false);
    }

    @Test
    public void shouldMutateReturnOfPrimitiveBooleanFalseToTrue() {
        v.forFunctionClass(HasPrimitiveBooleanReturn.class)
                .firstMutantShouldReturn(false, true);
    }

    @Test
    public void shouldMutateReturnOfPrimitiveInteger1To0() {
        v.forFunctionClass(HasPrimitiveIntegerReturn.class)
                .firstMutantShouldReturn(1, 0);
    }

    @Test
    public void shouldMutateReturnOfPrimitiveInteger0To1() {
        v.forFunctionClass(HasPrimitiveIntegerReturn.class)
                .firstMutantShouldReturn(0, 1);
    }

    @Test
    public void shouldMutateReturnOfPrimitiveIntegerToValuePlus1() {
        v.forFunctionClass(HasPrimitiveIntegerReturn.class)
                .firstMutantShouldReturn(247, 248);
    }

    @Test
    public void shouldMutateReturnOfPrimitiveLongToValuePlus1() {
        v.forFunctionClass(HasPrimitiveLongReturn.class)
                .firstMutantShouldReturn(1234567891234567890L, 1234567891234567891L);
    }

    @Test
    public void shouldMutateReturnOfAnyNonZeroFloatToInverseOfOnePlusTheValue() {
        v.forFunctionClass(HasPrimitiveFloatReturn.class)
                .firstMutantShouldReturn(1234F, -1235.0F);
    }

    @Test
    public void shouldMutateReturnOfPrimitiveFloatNANToMinusOne() {
        v.forFunctionClass(HasPrimitiveFloatReturn.class)
                .firstMutantShouldReturn(Float.NaN, -1F);
    }

    @Test
    public void shouldMutateReturnOfAnyNonZeroDoubleToInverseOfOnePlusTheValue() {
        v.forFunctionClass(HasPrimitiveDoubleReturn.class)
                .firstMutantShouldReturn(1234D,
                        -1235.0D);
    }

    @Test
    public void shouldMutateReturnOfPrimitiveDoubleNANToMinusOne() {
        v.forFunctionClass(HasPrimitiveDoubleReturn.class)
                .firstMutantShouldReturn(Double.NaN,
                        -1D);
    }

    @Test
    public void shouldMutateReturnOfBooleanTrueToFalse() {
        v.forFunctionClass(HasBooleanReturn.class)
                .firstMutantShouldReturn(Boolean.TRUE,
                        Boolean.FALSE);
    }

    @Test
    public void shouldMutateReturnOfBooleanFalseToTrue() {
        v.forFunctionClass(HasBooleanReturn.class)
                .firstMutantShouldReturn(Boolean.FALSE,
                        Boolean.TRUE);
    }

    @Test
    public void shouldMutateReturnOfBooleanNullToTrue() {
        v.forFunctionClass(HasBooleanReturn.class)
                .firstMutantShouldReturn(() -> null, Boolean.TRUE);
    }

    @Test
    public void cannotMutateReturnOfBooleanIfDeclaredAsObject() {
        v.forFunctionClass(HasObjectReturn.class)
                .firstMutantShouldReturn(Boolean.TRUE, null);
    }

    @Test
    public void shouldMutateReturnOfIntegerToValuePlus1() {
        v.forFunctionClass(HasIntegerReturn.class)
                .firstMutantShouldReturn(Integer.valueOf(123),
                        Integer.valueOf(124));
    }

    @Test
    public void shouldMutateReturnOfInteger1To0() {
        v.forFunctionClass(HasIntegerReturn.class)
                .firstMutantShouldReturn(Integer.valueOf(1),
                        Integer.valueOf(0));
    }

    @Test
    public void shouldMutateReturnOfIntegerNullToOne() {
        v.forFunctionClass(HasIntegerReturn.class)
                .firstMutantShouldReturn(() -> null, Integer.valueOf(1));
    }

    @Test
    public void shouldMutateReturnOfLongToValuePlus1() {
        v.forFunctionClass(HasLongReturn.class)
                .firstMutantShouldReturn(Long.valueOf(Integer.MAX_VALUE + 5L),
                        Long.valueOf(Integer.MAX_VALUE + 6L));
    }

    @Test
    public void shouldMutateReturnOfLongNullToOne() {
        v.forFunctionClass(HasLongReturn.class)
                .firstMutantShouldReturn(() -> null, Long.valueOf(1));
    }

    @Test
    public void cannotMutateReturnOfIntegerIfDeclaredAsObject() {
        v.forFunctionClass(HasObjectReturn.class)
                .firstMutantShouldReturn(Integer.valueOf(1), null);
    }

    @Test
    public void shouldMutateReturnsOfNonNullObjectsToNull() {
        v.forFunctionClass(HasObjectReturn.class)
                .firstMutantShouldReturn(new Object(), null);
    }

    @Test
    public void shouldMutateReturnsOfNullObjectsToNewObject() {
        v.forFunctionClass(HasObjectReturn.class)
                .firstMutantShouldReturn(() -> null, o -> o != null);
    }

    @Test
    public void shouldMutateReturnsOfNonNullCustomObjectsToNull() {
        v.forCallableClass(HasCustomObjectReturn.class)
                .firstMutantShouldReturn(null);
    }

    @Test(expected = RuntimeException.class)
    public void shouldMutateReturnsOfNullCustomObjectsToThrownRuntimeException() {
        v.forCallableClass(ReturnsNull.class)
                .firstMutantShouldReturn(null);
    }

    private static class HasPrimitiveBooleanReturn implements Function<Boolean, Boolean> {
        private boolean value;

        private boolean returnPrimitiveBoolean() {
            return this.value;
        }

        @Override
        public Boolean apply(Boolean value) {
            this.value = value;
            return returnPrimitiveBoolean();
        }
    }

    private static class HasPrimitiveIntegerReturn implements Function<Integer, Integer> {

        private int value;

        public int returnPrimitiveInteger() {
            return this.value;
        }

        @Override
        public Integer apply(Integer value) {
            this.value = value;
            return returnPrimitiveInteger();
        }
    }

    private static class HasPrimitiveLongReturn implements Function<Long, Long> {

        private long value;

        public long returnPrimitiveLong() {
            return this.value;
        }

        @Override
        public Long apply(Long value) {
            this.value = value;
            return returnPrimitiveLong();
        }
    }

    private static class HasPrimitiveFloatReturn implements Function<Float, Float> {

        private float value;

        public float returnPrimitiveFloat() {
            return this.value;
        }

        @Override
        public Float apply(Float value) {
            this.value = value;
            return returnPrimitiveFloat();
        }
    }

    private static class HasPrimitiveDoubleReturn implements Function<Double, Double> {

        private double value;

        public double returnPrimitiveDouble() {
            return this.value;
        }

        @Override
        public Double apply(Double value) {
            this.value = value;
            return returnPrimitiveDouble();
        }
    }

    private static class HasBooleanReturn implements Function<Boolean, Boolean> {
        @Override
        public Boolean apply(Boolean value) {
            return value;
        }
    }

    private static class HasIntegerReturn implements Function<Integer, Integer> {
        @Override
        public Integer apply(Integer value) {
            return value;
        }
    }

    private static class HasLongReturn implements Function<Long, Long> {
        @Override
        public Long apply(Long value) {
            return value;
        }
    }

    private static class HasObjectReturn implements Function<Object, Object> {

        private Object value;

        @Override
        public Object apply(Object value) {
            this.value = value;
            return this.value;
        }
    }

    private static class CustomObject extends Object {
    }

    private static class HasCustomObjectReturn implements Callable<CustomObject> {
        private final CustomObject value = new CustomObject();

        @Override
        public CustomObject call() {
            return this.value;
        }
    }

    private static class ReturnsNull implements Callable<CustomObject> {
        @Override
        public CustomObject call() {
            return null;
        }
    }

}
