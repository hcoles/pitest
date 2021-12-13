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

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;
import org.pitest.mutationtest.engine.gregor.mutators.experimental.BigDecimalMutator;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BigDecimalMutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToMutateOnlyReturnVals() {
    createTesteeWith(BigDecimalMutator.INSTANCE);
  }

  @Test
  public void add() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(Add.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new Add(0, 2), mutant, "-2");
  }

  @Test
  public void subtract() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(Subtract.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new Subtract(0, 3), mutant, "3");
  }

  @Test
  public void mutliply() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(Multiply.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new Multiply(2, 2), mutant, "1");
  }

  @Test
  public void divide() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(Divide.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new Divide(2, 2), mutant, "4");
  }

  @Test
  public void abs() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(Abs.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new Abs(-25, 6), mutant, "25");
    assertMutantCallableReturns(new Abs(25, 6), mutant, "-25");
  }

  @Test
  public void negate() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(Negate.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new Negate(0xFFFFFF00), mutant, String.valueOf(0xFFFFFF00));
    assertMutantCallableReturns(new Negate(0), mutant, String.valueOf(0));
    assertMutantCallableReturns(new Negate(0xFF00FF00), mutant, String.valueOf(0xFF00FF00));
  }

  @Test
  public void plus() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(Plus.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new Plus(0xFFFFFF00), mutant, String.valueOf(0x00000100));
    assertMutantCallableReturns(new Plus(0), mutant, String.valueOf(0));
    assertMutantCallableReturns(new Plus(0xFF00FF00), mutant, String.valueOf(0x00FF0100));
  }

  @Test
  public void min() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(Min.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new Min(-25, 6), mutant, "6");
    assertMutantCallableReturns(new Min(25, 6), mutant, "25");
  }

  @Test
  public void max() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(Max.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new Max(-25, 6), mutant, "-25");
    assertMutantCallableReturns(new Max(25, 6), mutant, "6");
  }

  @Test
  public void addLambda() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(AddLambda.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new AddLambda(0, 2), mutant, "-2");
  }

  @Test
  public void subtractLambda() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(SubtractLambda.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new SubtractLambda(0, 3), mutant, "3");
  }

  @Test
  public void mutliplyLambda() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(MultiplyLambda.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new MultiplyLambda(2, 2), mutant, "1");
  }

  @Test
  public void divideLambda() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(DivideLambda.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new DivideLambda(2, 2), mutant, "4");
  }

  @Test
  public void absLambda() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(AbsLambda.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new AbsLambda(-25, 6), mutant, "25");
    assertMutantCallableReturns(new AbsLambda(25, 6), mutant, "-25");
  }

  @Test
  public void negateLambda() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(NegateLambda.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new NegateLambda(0xFFFFFF00), mutant, String.valueOf(0xFFFFFF00));
    assertMutantCallableReturns(new NegateLambda(0), mutant, String.valueOf(0));
    assertMutantCallableReturns(new NegateLambda(0xFF00FF00), mutant, String.valueOf(0xFF00FF00));
  }

  @Test
  public void plusLambda() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(PlusLambda.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new PlusLambda(0xFFFFFF00), mutant, String.valueOf(0x00000100));
    assertMutantCallableReturns(new PlusLambda(0), mutant, String.valueOf(0));
    assertMutantCallableReturns(new PlusLambda(0xFF00FF00), mutant, String.valueOf(0x00FF0100));
  }

  @Test
  public void minLambda() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(MinLambda.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new MinLambda(-25, 6), mutant, "6");
    assertMutantCallableReturns(new MinLambda(25, 6), mutant, "25");
  }

  @Test
  public void maxLambda() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(MaxLambda.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new MaxLambda(-25, 6), mutant, "-25");
    assertMutantCallableReturns(new MaxLambda(25, 6), mutant, "6");
  }

  private static abstract class AbstractMath implements Callable<String> {

    private final BigDecimal value1;
    private final BigDecimal value2;

    AbstractMath(long v1, long v2) {
      this.value1 = BigDecimal.valueOf(v1);
      this.value2 = BigDecimal.valueOf(v2);
    }

    abstract BigDecimal apply(BigDecimal left, BigDecimal right);

    @Override
    public String call() throws Exception {
      return String.valueOf(apply(value1, value2));
    }
  }

  private static class Add extends AbstractMath {

    Add(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigDecimal apply(BigDecimal left, BigDecimal right) {
      return left.add(right);
    }
  }

  private static class Subtract extends AbstractMath {

    Subtract(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigDecimal apply(BigDecimal left, BigDecimal right) {
      return left.subtract(right);
    }
  }

  private static class Divide extends AbstractMath {

    Divide(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigDecimal apply(BigDecimal left, BigDecimal right) {
      return left.divide(right);
    }
  }

  private static class Multiply extends AbstractMath {

    Multiply(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigDecimal apply(BigDecimal left, BigDecimal right) {
      return left.multiply(right);
    }
  }

  private static class Max extends AbstractMath {

    Max(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigDecimal apply(BigDecimal left, BigDecimal right) {
      return left.max(right);
    }
  }

  private static class Min extends AbstractMath {

    Min(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigDecimal apply(BigDecimal left, BigDecimal right) {
      return left.min(right);
    }
  }

  private static class Negate extends AbstractMath {

    Negate(long v1) {
      super(v1, 0L);
    }

    @Override
    BigDecimal apply(BigDecimal left, BigDecimal right) {
      return left.negate();
    }
  }

  private static class Plus extends AbstractMath {

    Plus(long v1) {
      super(v1, 0L);
    }

    @Override
    BigDecimal apply(BigDecimal left, BigDecimal right) {
      return left.plus();
    }
  }

  private static class Abs extends AbstractMath {

    Abs(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigDecimal apply(BigDecimal left, BigDecimal right) {
      return left.abs();
    }
  }

  private static class AddLambda extends AbstractMath {

    AddLambda(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigDecimal apply(BigDecimal left, BigDecimal right) {
      BiFunction<BigDecimal, BigDecimal, BigDecimal> function = BigDecimal::add;
      return function.apply(left, right);    }
  }

  private static class SubtractLambda extends AbstractMath {

    SubtractLambda(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigDecimal apply(BigDecimal left, BigDecimal right) {
      BiFunction<BigDecimal, BigDecimal, BigDecimal> function = BigDecimal::subtract;
      return function.apply(left, right);    }
  }

  private static class DivideLambda extends AbstractMath {

    DivideLambda(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigDecimal apply(BigDecimal left, BigDecimal right) {
      BiFunction<BigDecimal, BigDecimal, BigDecimal> function = BigDecimal::divide;
      return function.apply(left, right);
    }
  }

  private static class MultiplyLambda extends AbstractMath {

    MultiplyLambda(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigDecimal apply(BigDecimal left, BigDecimal right) {
      BiFunction<BigDecimal, BigDecimal, BigDecimal> function = BigDecimal::multiply;
      return function.apply(left, right);
    }
  }

  private static class MaxLambda extends AbstractMath {

    MaxLambda(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigDecimal apply(BigDecimal left, BigDecimal right) {
      BiFunction<BigDecimal, BigDecimal, BigDecimal> function = BigDecimal::max;
      return function.apply(left, right);    }
  }

  private static class MinLambda extends AbstractMath {

    MinLambda(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigDecimal apply(BigDecimal left, BigDecimal right) {
      BiFunction<BigDecimal, BigDecimal, BigDecimal> function = BigDecimal::min;
      return function.apply(left, right);    }
  }

  private static class NegateLambda extends AbstractMath {

    NegateLambda(long v1) {
      super(v1, 0L);
    }

    @Override
    BigDecimal apply(BigDecimal left, BigDecimal right) {
      Function<BigDecimal, BigDecimal> function = BigDecimal::negate;
      return function.apply(left);
    }
  }

  private static class PlusLambda extends AbstractMath {

    PlusLambda(long v1) {
      super(v1, 0L);
    }

    @Override
    BigDecimal apply(BigDecimal left, BigDecimal right) {
      Function<BigDecimal, BigDecimal> function = BigDecimal::plus;
      return function.apply(left);
    }
  }

  private static class AbsLambda extends AbstractMath {

    AbsLambda(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigDecimal apply(BigDecimal left, BigDecimal right) {
      Function<BigDecimal, BigDecimal> function = BigDecimal::abs;
      return function.apply(left);    }
  }

}
