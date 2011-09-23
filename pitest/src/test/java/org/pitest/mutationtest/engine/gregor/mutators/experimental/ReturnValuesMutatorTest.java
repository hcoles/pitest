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

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;

public class ReturnValuesMutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToMutateOnlyReturnValues() {
    createTesteeWith(new ReturnValuesMutator());
  }

  private static class HasPrimitiveBooleanReturn implements Callable<Boolean> {

    private final boolean value;

    public HasPrimitiveBooleanReturn(final boolean value) {
      this.value = value;
    }

    private boolean returnPrimitiveBoolean() {
      return this.value;
    }

    public Boolean call() throws Exception {
      return returnPrimitiveBoolean();
    }
  }

  @Test
  public void shouldMutateReturnOfPrimitiveBooleanTrueToFalse()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasPrimitiveBooleanReturn.class);
    assertMutantCallableReturns(new HasPrimitiveBooleanReturn(true), mutant,
        Boolean.FALSE);
  }

  @Test
  public void shouldMutateReturnOfPrimitiveBooleanFalseToTrue()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasPrimitiveBooleanReturn.class);
    assertMutantCallableReturns(new HasPrimitiveBooleanReturn(false), mutant,
        Boolean.TRUE);
  }

  private static class HasPrimitiveIntegerReturn implements Callable<Integer> {

    private final int value;

    public HasPrimitiveIntegerReturn(final int value) {
      this.value = value;
    }

    public int returnPrimitiveInteger() {
      return this.value;
    }

    public Integer call() throws Exception {
      return returnPrimitiveInteger();
    }
  }

  @Test
  public void shouldMutateReturnOfPrimitiveInteger1To0() throws Exception {
    final Mutant mutant = getFirstMutant(HasPrimitiveIntegerReturn.class);
    assertMutantCallableReturns(new HasPrimitiveIntegerReturn(1), mutant, 0);
  }

  @Test
  public void shouldMutateReturnOfPrimitiveInteger0To1() throws Exception {
    final Mutant mutant = getFirstMutant(HasPrimitiveIntegerReturn.class);
    assertMutantCallableReturns(new HasPrimitiveIntegerReturn(0), mutant, 1);
  }

  @Test
  public void shouldMutateReturnOfPrimitiveIntegerToValuePlus1()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasPrimitiveIntegerReturn.class);
    assertMutantCallableReturns(new HasPrimitiveIntegerReturn(247), mutant, 248);
  }

  private static class HasPrimitiveLongReturn implements Callable<Long> {

    private final long value;

    public HasPrimitiveLongReturn(final long value) {
      this.value = value;
    }

    public long returnPrimitiveLong() {
      return this.value;
    }

    public Long call() throws Exception {
      return returnPrimitiveLong();
    }
  }

  @Test
  public void shouldMutateReturnOfPrimitiveLongToValuePlus1() throws Exception {
    final Mutant mutant = getFirstMutant(HasPrimitiveLongReturn.class);
    assertMutantCallableReturns(
        new HasPrimitiveLongReturn(1234567891234567890L), mutant,
        1234567891234567891L);
  }

  private static class HasPrimitiveFloatReturn implements Callable<Float> {

    private final float value;

    public HasPrimitiveFloatReturn(final float value) {
      this.value = value;
    }

    public float returnPrimitiveFloat() {
      return this.value;
    }

    public Float call() throws Exception {
      return returnPrimitiveFloat();
    }
  }

  @Test
  public void shouldMutateReturnOfAnyNonZeroFloatToInverseOfOnePlusTheValue()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasPrimitiveFloatReturn.class);
    assertMutantCallableReturns(new HasPrimitiveFloatReturn(1234F), mutant,
        -1235.0F);
  }

  @Test
  public void shouldMutateReturnOfFloatNANToMinusOne() throws Exception {
    final Mutant mutant = getFirstMutant(HasPrimitiveFloatReturn.class);
    assertMutantCallableReturns(new HasPrimitiveFloatReturn(Float.NaN), mutant,
        -1F);
  }

  private static class HasPrimitiveDoubleReturn implements Callable<Double> {

    private final double value;

    public HasPrimitiveDoubleReturn(final double value) {
      this.value = value;
    }

    public double returnPrimitiveDouble() {
      return this.value;
    }

    public Double call() throws Exception {
      return returnPrimitiveDouble();
    }
  }

  @Test
  public void shouldMutateReturnOfAnyNonZeroDoubleToInverseOfOnePlusTheValue()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasPrimitiveDoubleReturn.class);
    assertMutantCallableReturns(new HasPrimitiveDoubleReturn(1234D), mutant,
        -1235.0D);
  }

  @Test
  public void shouldMutateReturnOfDoubleNANToMinusOne() throws Exception {
    final Mutant mutant = getFirstMutant(HasPrimitiveDoubleReturn.class);
    assertMutantCallableReturns(new HasPrimitiveDoubleReturn(Double.NaN),
        mutant, -1D);
  }

}
