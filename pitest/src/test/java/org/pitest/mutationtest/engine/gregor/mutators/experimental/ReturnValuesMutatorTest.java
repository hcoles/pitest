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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

    @Override
    public Boolean call() throws Exception {
      return returnPrimitiveBoolean();
    }
  }

  @Test
  public void shouldProvideAMeaningfulName() {
    assertEquals("EXPERIMENTAL_RETURN_VALUES_MUTATOR",
        new ReturnValuesMutator().getName());
  }

  @Test
  public void shouldMutateReturnOfPrimitiveBooleanTrueToFalse()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasPrimitiveBooleanReturn.class);
    assertMutantCallableReturns(new HasPrimitiveBooleanReturn(true), mutant,
        false);
  }

  @Test
  public void shouldMutateReturnOfPrimitiveBooleanFalseToTrue()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasPrimitiveBooleanReturn.class);
    assertMutantCallableReturns(new HasPrimitiveBooleanReturn(false), mutant,
        true);
  }

  private static class HasPrimitiveIntegerReturn implements Callable<Integer> {

    private final int value;

    public HasPrimitiveIntegerReturn(final int value) {
      this.value = value;
    }

    public int returnPrimitiveInteger() {
      return this.value;
    }

    @Override
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

    @Override
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

    @Override
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
  public void shouldMutateReturnOfPrimitiveFloatNANToMinusOne()
      throws Exception {
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

    @Override
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
  public void shouldMutateReturnOfPrimitiveDoubleNANToMinusOne()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasPrimitiveDoubleReturn.class);
    assertMutantCallableReturns(new HasPrimitiveDoubleReturn(Double.NaN),
        mutant, -1D);
  }

  private static class HasBooleanReturn implements Callable<Boolean> {

    private final Boolean value;

    public HasBooleanReturn(final Boolean value) {
      this.value = value;
    }

    @Override
    public Boolean call() throws Exception {
      return this.value;
    }
  }

  @Test
  public void shouldMutateReturnOfBooleanTrueToFalse() throws Exception {
    final Mutant mutant = getFirstMutant(HasBooleanReturn.class);
    assertMutantCallableReturns(new HasBooleanReturn(Boolean.TRUE), mutant,
        Boolean.FALSE);
  }

  @Test
  public void shouldMutateReturnOfBooleanFalseToTrue() throws Exception {
    final Mutant mutant = getFirstMutant(HasBooleanReturn.class);
    assertMutantCallableReturns(new HasBooleanReturn(Boolean.FALSE), mutant,
        Boolean.TRUE);
  }

  @Test
  public void shouldMutateReturnOfBooleanNullToTrue() throws Exception {
    final Mutant mutant = getFirstMutant(HasBooleanReturn.class);
    assertMutantCallableReturns(new HasBooleanReturn(null), mutant,
        Boolean.TRUE);
  }

  @Test
  public void cannotMutateReturnOfBooleanIfDeclaredAsObject() throws Exception {
    final Mutant mutant = getFirstMutant(HasObjectReturn.class);
    assertMutantCallableReturns(new HasObjectReturn(Boolean.TRUE), mutant, null);
  }

  private static class HasIntegerReturn implements Callable<Integer> {

    private final Integer value;

    public HasIntegerReturn(final Integer value) {
      this.value = value;
    }

    @Override
    public Integer call() throws Exception {
      return this.value;
    }
  }

  @Test
  public void shouldMutateReturnOfIntegerToValuePlus1() throws Exception {
    final Mutant mutant = getFirstMutant(HasIntegerReturn.class);
    assertMutantCallableReturns(new HasIntegerReturn(Integer.valueOf(123)),
        mutant, Integer.valueOf(124));
  }

  @Test
  public void shouldMutateReturnOfInteger1To0() throws Exception {
    final Mutant mutant = getFirstMutant(HasIntegerReturn.class);
    assertMutantCallableReturns(new HasIntegerReturn(Integer.valueOf(1)),
        mutant, Integer.valueOf(0));
  }

  @Test
  public void shouldMutateReturnOfIntegerNullToOne() throws Exception {
    final Mutant mutant = getFirstMutant(HasIntegerReturn.class);
    assertMutantCallableReturns(new HasIntegerReturn(null), mutant,
        Integer.valueOf(1));
  }

  private static class HasLongReturn implements Callable<Long> {

    private final Long value;

    public HasLongReturn(final Long value) {
      this.value = value;
    }

    @Override
    public Long call() throws Exception {
      return this.value;
    }
  }

  @Test
  public void shouldMutateReturnOfLongToValuePlus1() throws Exception {
    final Mutant mutant = getFirstMutant(HasLongReturn.class);
    assertMutantCallableReturns(
        new HasLongReturn(Long.valueOf(Integer.MAX_VALUE + 5l)), mutant,
        Long.valueOf(Integer.MAX_VALUE + 6l));
  }

  @Test
  public void shouldMutateReturnOfLongNullToOne() throws Exception {
    final Mutant mutant = getFirstMutant(HasLongReturn.class);
    assertMutantCallableReturns(new HasLongReturn(null), mutant,
        Long.valueOf(1));
  }

  @Test
  public void cannotMutateReturnOfIntegerIfDeclaredAsObject() throws Exception {
    final Mutant mutant = getFirstMutant(HasObjectReturn.class);
    assertMutantCallableReturns(new HasObjectReturn(Integer.valueOf(1)),
        mutant, null);
  }

  private static class HasObjectReturn implements Callable<Object> {

    private final Object value;

    public HasObjectReturn(final Object value) {
      this.value = value;
    }

    @Override
    public Object call() throws Exception {
      return this.value;
    }
  }

  @Test
  public void shouldMutateReturnsOfNonNullObjectsToNull() throws Exception {
    final Mutant mutant = getFirstMutant(HasObjectReturn.class);
    assertMutantCallableReturns(new HasObjectReturn(new Object()), mutant, null);
  }

  @Test
  public void shouldMutateReturnsOfNullObjectsToNewObject() throws Exception {
    final Mutant mutant = getFirstMutant(HasObjectReturn.class);
    assertNotNull(mutateAndCall(new HasObjectReturn(null), mutant));
  }

  private static class CustomObject extends Object {
  }

  private static class HasCustomObjectReturn implements Callable<CustomObject> {

    private final CustomObject value;

    public HasCustomObjectReturn(final CustomObject value) {
      this.value = value;
    }

    @Override
    public CustomObject call() throws Exception {
      return this.value;
    }
  }

  @Test
  public void shouldMutateReturnsOfNonNullCustomObjectsToNull()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasCustomObjectReturn.class);
    assertMutantCallableReturns(new HasCustomObjectReturn(new CustomObject()),
        mutant, null);
  }

  @Test(expected = RuntimeException.class)
  public void shouldMutateReturnsOfNullCustomObjectsToThrownRuntimeException()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasCustomObjectReturn.class);
    mutateAndCall(new HasCustomObjectReturn(null), mutant);
  }

  private static class HasGenericStuff<T> implements Callable<T> {

    private final T object;

    private HasGenericStuff(final T object) {
      this.object = object;
    }

    @Override
    public T call() throws Exception {
      return this.object;
    }

  }

  @Test(expected = ClassCastException.class)
  public void cannotDetectGenericsStuff() {
    final Mutant mutant = getFirstMutant(HasGenericStuff.class);

    // assigning to x causes a class cast exception! The returned object is
    final CustomObject x = mutateAndCall(
        new HasGenericStuff<CustomObject>(null), mutant);
    assertNotNull(x); // this line never called but prevents x not used warning.
  }

}
