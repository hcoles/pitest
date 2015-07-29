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

import java.util.Collection;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;

public class ReturnValsMutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToMutateOnlyReturnVals() {
    createTesteeWith(ReturnValsMutator.RETURN_VALS_MUTATOR);
  }

  private static class IReturn implements Callable<String> {
    private final int value;

    public IReturn(final int value) {
      this.value = value;
    }

    public int mutable() {
      return this.value;
    }

    @Override
    public String call() throws Exception {
      return "" + mutable();
    }

  }

  @Test
  public void shouldMutateIReturnsOf0To1() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(IReturn.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new IReturn(0), mutant, "1");
  }

  @Test
  public void shouldMutateIReturnsOf1To0() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(IReturn.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new IReturn(1), mutant, "0");
  }

  @Test
  public void shouldMutateIReturnsOfAnyNonZeroValueTo0() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(IReturn.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new IReturn(1234), mutant, "0");
  }

  private static class LReturn implements Callable<String> {

    private final long value;

    public LReturn(final long value) {
      this.value = value;
    }

    public long mutable() {
      return this.value;
    }

    @Override
    public String call() throws Exception {
      return "" + mutable();
    }

  }

  @Test
  public void shouldAddOneToValueOfLReturn() throws Exception {
    assertMutantCallableReturns(new LReturn(0),
        createFirstMutant(LReturn.class), "1");
  }

  private static class BooleanReturn implements Callable<String> {

    private final boolean value;

    public BooleanReturn(final boolean value) {
      this.value = value;
    }

    public boolean mutable() {
      return this.value;
    }

    @Override
    public String call() throws Exception {
      return "" + mutable();
    }

  }

  @Test
  public void shouldMutateReturnsTrueToReturnsFalse() throws Exception {
    assertMutantCallableReturns(new BooleanReturn(true),
        createFirstMutant(BooleanReturn.class), "false");
  }

  @Test
  public void shouldMutateReturnsFalseToReturnsTrue() throws Exception {
    assertMutantCallableReturns(new BooleanReturn(false),
        createFirstMutant(BooleanReturn.class), "true");
  }

  private static class FReturn implements Callable<String> {

    private final float value;

    public FReturn(final float value) {
      this.value = value;
    }

    public float mutable() {
      return this.value;
    }

    @Override
    public String call() throws Exception {
      return "" + mutable();
    }

  }

  @Test
  public void shouldMutateFReturnsOfAnyNonZeroValueToInverseOfOnePlusTheValue()
      throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(FReturn.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new FReturn(1234f), mutant, "-1235.0");
  }

  @Test
  public void shouldMutateReturnsOfFloatNotANumberToMinusOne() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(FReturn.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new FReturn(Float.NaN), mutant, "-1.0");
  }

  private static class DReturn implements Callable<String> {

    private final double value;

    public DReturn(final double value) {
      this.value = value;
    }

    public double mutable() {
      return this.value;
    }

    @Override
    public String call() throws Exception {
      return "" + mutable();
    }

  }

  @Test
  public void shouldMutateDReturnsOfAnyNonZeroValueToInverseOfOnePlusTheValue()
      throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(DReturn.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new DReturn(1234d), mutant, "-1235.0");
  }

  @Test
  public void shouldMutateReturnsOfDoubleNotANumberToMinusOne()
      throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(DReturn.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new DReturn(Double.NaN), mutant, "-1.0");
  }

  private static class AReturn implements Callable<String> {

    private final Object value;

    public AReturn(final Object value) {
      this.value = value;
    }

    public Object mutable() {
      return this.value;
    }

    @Override
    public String call() throws Exception {
      return "" + mutable();
    }

  }

  @Test
  public void shouldMutateReturnsOfNonNullObjectsToNull() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(AReturn.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new AReturn("foo"), mutant, "null");
  }

  @Test(expected = RuntimeException.class)
  public void shouldMutateReturnsOfNullObjectsToRuntimeExceptions()
      throws Exception {
    final Mutant mutant = getFirstMutant(AReturn.class);
    mutateAndCall(new AReturn(null), mutant);
  }

}
