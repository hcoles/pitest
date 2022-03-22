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

import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.LongFunction;

import org.junit.Test;
import org.pitest.verifier.mutants.MutatorVerifierStart;

import static org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator.RETURN_VALS;

public class ReturnValsMutatorTest {

  MutatorVerifierStart v = MutatorVerifierStart.forMutator(RETURN_VALS);

  private static class IReturn implements IntFunction<String> {
    private int value;

    public int mutable() {
      return this.value;
    }

    @Override
    public String apply(int value) {
      this.value = value;
      return "" + mutable();
    }

  }

  @Test
  public void shouldMutateIReturnsOf0To1() {
    v.forIntFunctionClass(IReturn.class)
            .firstMutantShouldReturn(() -> 0, "1");
  }

  @Test
  public void shouldMutateIReturnsOf1To0() {
    v.forIntFunctionClass(IReturn.class)
            .firstMutantShouldReturn(() -> 1, "0");
  }

  @Test
  public void shouldMutateIReturnsOfAnyNonZeroValueTo0() {
    v.forIntFunctionClass(IReturn.class)
            .firstMutantShouldReturn(() -> 1234, "0");
  }

  private static class LReturn implements LongFunction<String> {

    private long value;

    public long mutable() {
      return this.value;
    }

    @Override
    public String apply(long value) {
      this.value = value;
      return "" + mutable();
    }

  }

  @Test
  public void shouldAddOneToValueOfLReturn() {
    v.forLongFunctionClass(LReturn.class)
            .firstMutantShouldReturn(() -> 0, "1");
  }

  private static class BooleanReturn implements Function<Boolean,String> {

    private boolean value;

    public boolean mutable() {
      return this.value;
    }

    @Override
    public String apply(Boolean b) {
      this.value = b;
      return "" + mutable();
    }

  }

  @Test
  public void shouldMutateReturnsTrueToReturnsFalse() {
    v.forFunctionClass(BooleanReturn.class)
            .firstMutantShouldReturn(() -> true, "false");
  }

  @Test
  public void shouldMutateReturnsFalseToReturnsTrue() {
    v.forFunctionClass(BooleanReturn.class)
            .firstMutantShouldReturn(() -> false, "true");
  }

  private static class FReturn implements Function<Float,String> {

    private float value;

    public float mutable() {
      return this.value;
    }

    @Override
    public String apply(Float f) {
      this.value = f;
      return "" + mutable();
    }

  }

  @Test
  public void shouldMutateFReturnsOfAnyNonZeroValueToInverseOfOnePlusTheValue() {
    v.forFunctionClass(FReturn.class)
            .firstMutantShouldReturn(() -> 1234f, "-1235.0");
  }

  @Test
  public void shouldMutateReturnsOfFloatNotANumberToMinusOne() throws Exception {
    v.forFunctionClass(FReturn.class)
            .firstMutantShouldReturn(() -> Float.NaN, "-1.0");
  }

  private static class DReturn implements DoubleFunction<String> {

    private double value;

    public double mutable() {
      return this.value;
    }

    @Override
    public String apply(double d) {
      this.value = d;
      return "" + mutable();
    }

  }

  @Test
  public void shouldMutateDReturnsOfAnyNonZeroValueToInverseOfOnePlusTheValue() {
    v.forDoubleFunctionClass(DReturn.class)
            .firstMutantShouldReturn(() -> 1234d, "-1235.0");
  }

  @Test
  public void shouldMutateReturnsOfDoubleNotANumberToMinusOne() {
    v.forDoubleFunctionClass(DReturn.class)
            .firstMutantShouldReturn(() -> Double.NaN, "-1.0");
  }

  private static class AReturn implements Function<Object, String> {

    private Object value;

    public Object mutable() {
      return this.value;
    }

    @Override
    public String apply(Object value) {
      this.value = value;
      return "" + mutable();
    }

  }

  @Test
  public void shouldMutateReturnsOfNonNullObjectsToNull() throws Exception {
    v.forFunctionClass(AReturn.class)
            .firstMutantShouldReturn(() -> "foo", "null");
  }

  @Test(expected = RuntimeException.class)
  public void shouldMutateReturnsOfNullObjectsToRuntimeExceptions() {
    v.forFunctionClass(AReturn.class)
            .firstMutantShouldReturn(() -> null, "");
  }

}
