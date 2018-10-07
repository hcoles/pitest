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

import java.math.BigInteger;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;
import org.pitest.mutationtest.engine.gregor.mutators.experimental.BigIntegerMutator;

public class BigIntegerMutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToMutateOnlyReturnVals() {
    createTesteeWith(BigIntegerMutator.INSTANCE);
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
  public void modulo() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(Modulo.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new Modulo(25, 5), mutant, "125");
  }

  @Test
  public void abs() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(Abs.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new Abs(-25, 6), mutant, "25");
    assertMutantCallableReturns(new Abs(25, 6), mutant, "-25");
  }

  @Test
  public void not() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(Not.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new Not(0b1111), mutant, String.valueOf(-0b1111));
    assertMutantCallableReturns(new Not(0b1111), mutant, String.valueOf(-0b1111));
  }

  @Test
  public void negate() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(Negate.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new Negate(0xFFFFFF00), mutant, String.valueOf(0x000000FF));
    assertMutantCallableReturns(new Negate(0), mutant, String.valueOf(0xFFFFFFFF));
    assertMutantCallableReturns(new Negate(0xFF00FF00), mutant, String.valueOf(0x00FF00FF));
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
  public void setBit() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(SetBit.class);
    Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new SetBit(0b11101, 2), mutant, String.valueOf(0b11001));
    assertMutantCallableReturns(new SetBit(0b11001, 2), mutant, String.valueOf(0b11001));
  }

  @Test
  public void clearBit() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(ClearBit.class);
    Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new ClearBit(0b11101, 2), mutant, String.valueOf(0b11101));
    assertMutantCallableReturns(new ClearBit(0b01101, 0), mutant, String.valueOf(0b01101));
  }

  @Test
  public void flipBit() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(FlipBit.class);
    Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new FlipBit(0b11101, 2), mutant, String.valueOf(0b11101));
    assertMutantCallableReturns(new FlipBit(0b01101, 0), mutant, String.valueOf(0b01101));
  }

  @Test
  public void shiftLeft() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(ShiftLeft.class);
    Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new ShiftLeft(1, 1), mutant, String.valueOf(1 >> 1));
    assertMutantCallableReturns(new ShiftLeft(1, 2), mutant, String.valueOf(1 >> 2));
    assertMutantCallableReturns(new ShiftLeft(1 << 8, 8), mutant, String.valueOf(1));
    assertMutantCallableReturns(new ShiftLeft(1 << 8, 4), mutant, String.valueOf(1 << 8 >> 4));
  }

  @Test
  public void shiftRight() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(ShiftRight.class);
    Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new ShiftRight(1, 1), mutant, String.valueOf(1 << 1));
    assertMutantCallableReturns(new ShiftRight(1, 2), mutant, String.valueOf(1 << 2));
    assertMutantCallableReturns(new ShiftRight(1 << 8, 8), mutant, String.valueOf(1L << 16L));
    assertMutantCallableReturns(new ShiftRight(1 << 8, 4), mutant, String.valueOf(1 << 8 << 4));
  }

  @Test
  public void and() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(And.class);
    Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new And(0b01, 0b10), mutant, String.valueOf(0b11));
    assertMutantCallableReturns(new And(0b01, 0b01), mutant, String.valueOf(0b01));
    assertMutantCallableReturns(new And(0b01, 0b00), mutant, String.valueOf(0b01));
  }

  @Test
  public void or() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(Or.class);
    Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new Or(0b01, 0b10), mutant, String.valueOf(0b00));
    assertMutantCallableReturns(new Or(0b01, 0b01), mutant, String.valueOf(0b01));
    assertMutantCallableReturns(new Or(0b11, 0b10), mutant, String.valueOf(0b10));
  }

  @Test
  public void xor() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(Xor.class);
    Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new Xor(0b01, 0b10), mutant, String.valueOf(0b00));
    assertMutantCallableReturns(new Xor(0b01, 0b01), mutant, String.valueOf(0b01));
    assertMutantCallableReturns(new Xor(0b11, 0b10), mutant, String.valueOf(0b10));
  }

  @Test
  public void andNot() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(AndNot.class);
    Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new AndNot(0b01, 0b10), mutant, String.valueOf(0b00));
    assertMutantCallableReturns(new AndNot(0b01, 0b01), mutant, String.valueOf(0b01));
    assertMutantCallableReturns(new AndNot(0b11, 0b10), mutant, String.valueOf(0b10));
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
  public void moduloLambda() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(ModuloLambda.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new ModuloLambda(25, 5), mutant, "125");
  }

  @Test
  public void absLambda() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(AbsLambda.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new AbsLambda(-25, 6), mutant, "25");
    assertMutantCallableReturns(new AbsLambda(25, 6), mutant, "-25");
  }

  @Test
  public void notLambda() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(NotLambda.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new NotLambda(0b1111), mutant, String.valueOf(-0b1111));
    assertMutantCallableReturns(new NotLambda(0b1111), mutant, String.valueOf(-0b1111));
  }

  @Test
  public void negateLambda() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(NegateLambda.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new NegateLambda(0xFFFFFF00), mutant, String.valueOf(0x000000FF));
    assertMutantCallableReturns(new NegateLambda(0), mutant, String.valueOf(0xFFFFFFFF));
    assertMutantCallableReturns(new NegateLambda(0xFF00FF00), mutant, String.valueOf(0x00FF00FF));
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

  @Test
  public void setBitLambda() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(SetBitLambda.class);
    Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new SetBitLambda(0b11101, 2), mutant, String.valueOf(0b11001));
    assertMutantCallableReturns(new SetBitLambda(0b11001, 2), mutant, String.valueOf(0b11001));
  }

  @Test
  public void clearBitLambda() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(ClearBitLambda.class);
    Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new ClearBitLambda(0b11101, 2), mutant, String.valueOf(0b11101));
    assertMutantCallableReturns(new ClearBitLambda(0b01101, 0), mutant, String.valueOf(0b01101));
  }

  @Test
  public void flipBitLambda() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(FlipBitLambda.class);
    Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new FlipBitLambda(0b11101, 2), mutant, String.valueOf(0b11101));
    assertMutantCallableReturns(new FlipBitLambda(0b01101, 0), mutant, String.valueOf(0b01101));
  }

  @Test
  public void shiftLeftLambda() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(ShiftLeftLambda.class);
    Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new ShiftLeftLambda(1, 1), mutant, String.valueOf(1 >> 1));
    assertMutantCallableReturns(new ShiftLeftLambda(1, 2), mutant, String.valueOf(1 >> 2));
    assertMutantCallableReturns(new ShiftLeftLambda(1 << 8, 8), mutant, String.valueOf(1));
    assertMutantCallableReturns(new ShiftLeftLambda(1 << 8, 4), mutant, String.valueOf(1 << 8 >> 4));
  }

  @Test
  public void shiftRightLambda() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(ShiftRightLambda.class);
    Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new ShiftRightLambda(1, 1), mutant, String.valueOf(1 << 1));
    assertMutantCallableReturns(new ShiftRightLambda(1, 2), mutant, String.valueOf(1 << 2));
    assertMutantCallableReturns(new ShiftRightLambda(1 << 8, 8), mutant, String.valueOf(1L << 16L));
    assertMutantCallableReturns(new ShiftRightLambda(1 << 8, 4), mutant, String.valueOf(1 << 8 << 4));
  }

  @Test
  public void andLambda() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(AndLambda.class);
    Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new AndLambda(0b01, 0b10), mutant, String.valueOf(0b11));
    assertMutantCallableReturns(new AndLambda(0b01, 0b01), mutant, String.valueOf(0b01));
    assertMutantCallableReturns(new AndLambda(0b01, 0b00), mutant, String.valueOf(0b01));
  }

  @Test
  public void orLambda() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(OrLambda.class);
    Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new OrLambda(0b01, 0b10), mutant, String.valueOf(0b00));
    assertMutantCallableReturns(new OrLambda(0b01, 0b01), mutant, String.valueOf(0b01));
    assertMutantCallableReturns(new OrLambda(0b11, 0b10), mutant, String.valueOf(0b10));
  }

  @Test
  public void xorLambda() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(XorLambda.class);
    Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new XorLambda(0b01, 0b10), mutant, String.valueOf(0b00));
    assertMutantCallableReturns(new XorLambda(0b01, 0b01), mutant, String.valueOf(0b01));
    assertMutantCallableReturns(new XorLambda(0b11, 0b10), mutant, String.valueOf(0b10));
  }

  @Test
  public void andNotLambda() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(AndNotLambda.class);
    Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new AndNotLambda(0b01, 0b10), mutant, String.valueOf(0b00));
    assertMutantCallableReturns(new AndNotLambda(0b01, 0b01), mutant, String.valueOf(0b01));
    assertMutantCallableReturns(new AndNotLambda(0b11, 0b10), mutant, String.valueOf(0b10));
  }

  private static abstract class AbstractMath implements Callable<String> {

    private final BigInteger value1;
    private final BigInteger value2;

    AbstractMath(long v1, long v2) {
      this.value1 = BigInteger.valueOf(v1);
      this.value2 = BigInteger.valueOf(v2);
    }

    abstract BigInteger apply(BigInteger left, BigInteger right);

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
    BigInteger apply(BigInteger left, BigInteger right) {
      return left.add(right);
    }
  }

  private static class Subtract extends AbstractMath {

    Subtract(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      return left.subtract(right);
    }
  }

  private static class Divide extends AbstractMath {

    Divide(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      return left.divide(right);
    }
  }

  private static class Multiply extends AbstractMath {

    Multiply(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      return left.multiply(right);
    }
  }

  private static class Modulo extends AbstractMath {

    Modulo(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      return left.mod(right);
    }
  }

  private static class SetBit extends AbstractMath {

    SetBit(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      return left.setBit(right.intValue());
    }
  }

  private static class ClearBit extends AbstractMath {

    ClearBit(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      return left.clearBit(right.intValue());
    }
  }

  private static class FlipBit extends AbstractMath {

    FlipBit(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      return left.flipBit(right.intValue());
    }
  }

  private static class ShiftLeft extends AbstractMath {

    ShiftLeft(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      return left.shiftLeft(right.intValue());
    }
  }

  private static class ShiftRight extends AbstractMath {

    ShiftRight(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      return left.shiftRight(right.intValue());
    }
  }

  private static class And extends AbstractMath {

    And(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      return left.and(right);
    }
  }

  private static class Or extends AbstractMath {

    Or(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      return left.or(right);
    }
  }

  private static class Xor extends AbstractMath {

    Xor(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      return left.xor(right);
    }
  }

  private static class AndNot extends AbstractMath {

    AndNot(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      return left.andNot(right);
    }
  }

  private static class Max extends AbstractMath {

    Max(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      return left.max(right);
    }
  }

  private static class Min extends AbstractMath {

    Min(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      return left.min(right);
    }
  }

  private static class Not extends AbstractMath {

    Not(long v1) {
      super(v1, 0L);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      return left.not();
    }
  }

  private static class Negate extends AbstractMath {

    Negate(long v1) {
      super(v1, 0L);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      return left.negate();
    }
  }

  private static class Abs extends AbstractMath {

    Abs(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      return left.abs();
    }
  }

  private static class AddLambda extends AbstractMath {

    AddLambda(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      BiFunction<BigInteger, BigInteger, BigInteger> function = BigInteger::add;
      return function.apply(left, right);    }
  }

  private static class SubtractLambda extends AbstractMath {

    SubtractLambda(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      BiFunction<BigInteger, BigInteger, BigInteger> function = BigInteger::subtract;
      return function.apply(left, right);    }
  }

  private static class DivideLambda extends AbstractMath {

    DivideLambda(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      BiFunction<BigInteger, BigInteger, BigInteger> function = BigInteger::divide;
      return function.apply(left, right);
    }
  }

  private static class MultiplyLambda extends AbstractMath {

    MultiplyLambda(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      BiFunction<BigInteger, BigInteger, BigInteger> function = BigInteger::multiply;
      return function.apply(left, right);
    }
  }

  private static class SetBitLambda extends AbstractMath {

    SetBitLambda(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      IntFunction<BigInteger> function = left::setBit;
      return function.apply(right.intValue());
    }
  }

  private static class ClearBitLambda extends AbstractMath {

    ClearBitLambda(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      IntFunction<BigInteger> function = left::clearBit;
      return function.apply(right.intValue());
    }
  }

  private static class FlipBitLambda extends AbstractMath {

    FlipBitLambda(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      IntFunction<BigInteger> function = left::flipBit;
      return function.apply(right.intValue());
    }
  }

  private static class ShiftLeftLambda extends AbstractMath {

    ShiftLeftLambda(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      IntFunction<BigInteger> function = left::shiftLeft;
      return function.apply(right.intValue());    }
  }

  private static class ShiftRightLambda extends AbstractMath {

    ShiftRightLambda(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      IntFunction<BigInteger> function = left::shiftRight;
      return function.apply(right.intValue());    }
  }

  private static class AndLambda extends AbstractMath {

    AndLambda(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      BiFunction<BigInteger, BigInteger, BigInteger> function = BigInteger::and;
      return function.apply(left, right);
    }
  }

  private static class OrLambda extends AbstractMath {

    OrLambda(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      BiFunction<BigInteger, BigInteger, BigInteger> function = BigInteger::or;
      return function.apply(left, right);    }
  }

  private static class XorLambda extends AbstractMath {

    XorLambda(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      BiFunction<BigInteger, BigInteger, BigInteger> function = BigInteger::xor;
      return function.apply(left, right);    }
  }

  private static class AndNotLambda extends AbstractMath {

    AndNotLambda(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      BiFunction<BigInteger, BigInteger, BigInteger> function = BigInteger::andNot;
      return function.apply(left, right);    }
  }

  private static class MaxLambda extends AbstractMath {

    MaxLambda(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      BiFunction<BigInteger, BigInteger, BigInteger> function = BigInteger::max;
      return function.apply(left, right);    }
  }

  private static class MinLambda extends AbstractMath {

    MinLambda(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      BiFunction<BigInteger, BigInteger, BigInteger> function = BigInteger::min;
      return function.apply(left, right);    }
  }

  private static class NotLambda extends AbstractMath {

    NotLambda(long v1) {
      super(v1, 0L);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      Supplier<BigInteger> function = left::not;
      return function.get();
    }
  }

  private static class NegateLambda extends AbstractMath {

    NegateLambda(long v1) {
      super(v1, 0L);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      Function<BigInteger, BigInteger> function = BigInteger::negate;
      return function.apply(left);
    }
  }

  private static class AbsLambda extends AbstractMath {

    AbsLambda(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      Function<BigInteger, BigInteger> function = BigInteger::abs;
      return function.apply(left);    }
  }

  private static class ModuloLambda extends AbstractMath {

    ModuloLambda(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      BiFunction<BigInteger, BigInteger, BigInteger> function = BigInteger::mod;
      return function.apply(left, right);
    }
  }
}
