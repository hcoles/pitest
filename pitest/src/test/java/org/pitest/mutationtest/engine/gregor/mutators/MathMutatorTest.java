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

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;

public class MathMutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToMutateOnlyMathFunctions() {
    createTesteeWith(MathMutator.MATH_MUTATOR);
  }

  private static class HasIAdd implements Callable<String> {
    private int i;

    HasIAdd(final int i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i++;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceIntegerAdditionWithSubtraction() throws Exception {
    final Mutant mutant = getFirstMutant(HasIAdd.class);
    assertMutantCallableReturns(new HasIAdd(2), mutant, "1");
    assertMutantCallableReturns(new HasIAdd(20), mutant, "19");
  }

  private static class HasISub implements Callable<String> {
    private int i;

    HasISub(final int i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i--;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceIntegerSubtractionWithAddition() throws Exception {
    final Mutant mutant = getFirstMutant(HasISub.class);
    assertMutantCallableReturns(new HasISub(2), mutant, "3");
    assertMutantCallableReturns(new HasISub(20), mutant, "21");
  }

  private static class HasIMul implements Callable<String> {
    private int i;

    HasIMul(final int i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i = this.i * 2;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceIntegerMultiplicationWithDivision() throws Exception {
    final Mutant mutant = getFirstMutant(HasIMul.class);
    assertMutantCallableReturns(new HasIMul(2), mutant, "1");
    assertMutantCallableReturns(new HasIMul(20), mutant, "10");
  }

  private static class HasIDiv implements Callable<String> {
    private int i;

    HasIDiv(final int i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i = this.i / 2;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceIntegerDivisionWithMultiplication() throws Exception {
    final Mutant mutant = getFirstMutant(HasIDiv.class);
    assertMutantCallableReturns(new HasIDiv(2), mutant, "4");
    assertMutantCallableReturns(new HasIDiv(20), mutant, "40");
  }

  private static class HasIOr implements Callable<String> {
    private int i;

    HasIOr(final int i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i = this.i | 2;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceIntegerBitwiseOrsWithAnds() throws Exception {
    final Mutant mutant = getFirstMutant(HasIOr.class);
    assertMutantCallableReturns(new HasIDiv(2), mutant, "1");
    assertMutantCallableReturns(new HasIOr(4), mutant, "0");
  }

  private static class HasIAnd implements Callable<String> {
    private int i;

    HasIAnd(final int i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i = this.i & 2;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceIntegerBitwiseAndsWithOrs() throws Exception {
    final Mutant mutant = getFirstMutant(HasIAnd.class);
    assertMutantCallableReturns(new HasIAnd(2), mutant, "2");
    assertMutantCallableReturns(new HasIAnd(4), mutant, "6");
  }

  private static class HasIRem implements Callable<String> {
    private int i;

    HasIRem(final int i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i = this.i % 2;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceIntegerModulusWithMultiplication() throws Exception {
    final Mutant mutant = getFirstMutant(HasIRem.class);
    assertMutantCallableReturns(new HasIRem(2), mutant, "4");
    assertMutantCallableReturns(new HasIRem(3), mutant, "6");
  }

  private static class HasIXor implements Callable<String> {
    private int i;

    HasIXor(final int i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i = this.i ^ 2;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceIntegerXORWithAND() throws Exception {
    final Mutant mutant = getFirstMutant(HasIXor.class);
    assertMutantCallableReturns(new HasIXor(2), mutant, "2");
    assertMutantCallableReturns(new HasIXor(1), mutant, "0");
  }

  private static class HasISHL implements Callable<String> {
    private int i;

    HasISHL(final int i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i = this.i << 2;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceIntegerShiftLeftWithShiftRight() throws Exception {
    final Mutant mutant = getFirstMutant(HasISHL.class);
    assertMutantCallableReturns(new HasISHL(100), mutant, "25");
    assertMutantCallableReturns(new HasISHL(20), mutant, "5");
  }

  private static class HasISHR implements Callable<String> {
    private int i;

    HasISHR(final int i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i = this.i >> 2;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceIntegerShiftRightWithShiftLeft() throws Exception {
    final Mutant mutant = getFirstMutant(HasISHR.class);
    assertMutantCallableReturns(new HasISHR(100), mutant, "400");
    assertMutantCallableReturns(new HasISHR(20), mutant, "80");
  }

  private static class HasIUSHR implements Callable<String> {
    private int i;

    HasIUSHR(final int i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i = this.i >>> 2;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceIntegerUnsignedShiftRightWithShiftLeft()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasIUSHR.class);
    assertMutantCallableReturns(new HasIUSHR(100), mutant, "400");
    assertMutantCallableReturns(new HasIUSHR(20), mutant, "80");
  }

  // LONGS

  private static class HasLAdd implements Callable<String> {
    private long i;

    HasLAdd(final long i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i++;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceLongAdditionWithSubtraction() throws Exception {
    final Mutant mutant = getFirstMutant(HasLAdd.class);
    assertMutantCallableReturns(new HasLAdd(2), mutant, "1");
    assertMutantCallableReturns(new HasLAdd(20), mutant, "19");
  }

  private static class HasLSub implements Callable<String> {
    private long i;

    HasLSub(final long i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i--;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceLongSubtractionWithAddition() throws Exception {
    final Mutant mutant = getFirstMutant(HasLSub.class);
    assertMutantCallableReturns(new HasLSub(2), mutant, "3");
    assertMutantCallableReturns(new HasLSub(20), mutant, "21");
  }

  private static class HasLMul implements Callable<String> {
    private long i;

    HasLMul(final long i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i = this.i * 2;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceLongMultiplicationWithDivision() throws Exception {
    final Mutant mutant = getFirstMutant(HasLMul.class);
    assertMutantCallableReturns(new HasLMul(2), mutant, "1");
    assertMutantCallableReturns(new HasLMul(20), mutant, "10");
  }

  private static class HasLDiv implements Callable<String> {
    private long i;

    HasLDiv(final long i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i = this.i / 2;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceLongDivisionWithMultiplication() throws Exception {
    final Mutant mutant = getFirstMutant(HasLDiv.class);
    assertMutantCallableReturns(new HasLDiv(2), mutant, "4");
    assertMutantCallableReturns(new HasLDiv(20), mutant, "40");
  }

  private static class HasLOr implements Callable<String> {
    private long i;

    HasLOr(final long i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i = this.i | 2;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceLongBitwiseOrsWithAnds() throws Exception {
    final Mutant mutant = getFirstMutant(HasLOr.class);
    assertMutantCallableReturns(new HasLDiv(2), mutant, "1");
    assertMutantCallableReturns(new HasLOr(4), mutant, "0");
  }

  private static class HasLAnd implements Callable<String> {
    private long i;

    HasLAnd(final long i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i = this.i & 2;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceLongBitwiseAndsWithOrs() throws Exception {
    final Mutant mutant = getFirstMutant(HasLAnd.class);
    assertMutantCallableReturns(new HasLAnd(2), mutant, "2");
    assertMutantCallableReturns(new HasLAnd(4), mutant, "6");
  }

  private static class HasLRem implements Callable<String> {
    private long i;

    HasLRem(final long i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i = this.i % 2;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceLongModulusWithMultiplication() throws Exception {
    final Mutant mutant = getFirstMutant(HasLRem.class);
    assertMutantCallableReturns(new HasLRem(2), mutant, "4");
    assertMutantCallableReturns(new HasLRem(3), mutant, "6");
  }

  private static class HasLXor implements Callable<String> {
    private long i;

    HasLXor(final long i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i = this.i ^ 2;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceLongXORWithAND() throws Exception {
    final Mutant mutant = getFirstMutant(HasLXor.class);
    assertMutantCallableReturns(new HasLXor(2), mutant, "2");
    assertMutantCallableReturns(new HasLXor(1), mutant, "0");
  }

  private static class HasLSHL implements Callable<String> {
    private long i;

    HasLSHL(final long i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i = this.i << 2;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceLongShiftLeftWithShiftRight() throws Exception {
    final Mutant mutant = getFirstMutant(HasLSHL.class);
    assertMutantCallableReturns(new HasLSHL(100), mutant, "25");
    assertMutantCallableReturns(new HasLSHL(20), mutant, "5");
  }

  private static class HasLSHR implements Callable<String> {
    private long i;

    HasLSHR(final long i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i = this.i >> 2;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceLongShiftRightWithShiftLeft() throws Exception {
    final Mutant mutant = getFirstMutant(HasLSHR.class);
    assertMutantCallableReturns(new HasLSHR(100), mutant, "400");
    assertMutantCallableReturns(new HasLSHR(20), mutant, "80");
  }

  private static class HasLUSHR implements Callable<String> {
    private long i;

    HasLUSHR(final long i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i = this.i >>> 2;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceLongUnsignedShiftRightWithShiftLeft()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasLUSHR.class);
    assertMutantCallableReturns(new HasLUSHR(100), mutant, "400");
    assertMutantCallableReturns(new HasLUSHR(20), mutant, "80");
  }

  // FLOATS

  private static class HasFADD implements Callable<String> {
    private float i;

    HasFADD(final float i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i++;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceFloatAdditionWithSubtraction() throws Exception {
    final Mutant mutant = getFirstMutant(HasFADD.class);
    assertMutantCallableReturns(new HasFADD(2), mutant, "1.0");
    assertMutantCallableReturns(new HasFADD(20), mutant, "19.0");
  }

  private static class HasFSUB implements Callable<String> {
    private float i;

    HasFSUB(final float i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i--;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceFloatSubtractionWithAddition() throws Exception {
    final Mutant mutant = getFirstMutant(HasFSUB.class);
    assertMutantCallableReturns(new HasFSUB(2), mutant, "3.0");
    assertMutantCallableReturns(new HasFSUB(20), mutant, "21.0");
  }

  private static class HasFMUL implements Callable<String> {
    private float i;

    HasFMUL(final float i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i = this.i * 2;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceFloatMultiplicationWithDivision() throws Exception {
    final Mutant mutant = getFirstMutant(HasFMUL.class);
    assertMutantCallableReturns(new HasFMUL(2), mutant, "1.0");
    assertMutantCallableReturns(new HasFMUL(20), mutant, "10.0");
  }

  private static class HasFDIV implements Callable<String> {
    private float i;

    HasFDIV(final float i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i = this.i / 2;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceFloatDivisionWithMultiplication() throws Exception {
    final Mutant mutant = getFirstMutant(HasFDIV.class);
    assertMutantCallableReturns(new HasFDIV(2), mutant, "4.0");
    assertMutantCallableReturns(new HasFDIV(20), mutant, "40.0");
  }

  private static class HasFREM implements Callable<String> {
    private float i;

    HasFREM(final float i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i = this.i % 2;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceFloatModulusWithMultiplication() throws Exception {
    final Mutant mutant = getFirstMutant(HasFREM.class);
    assertMutantCallableReturns(new HasFREM(2), mutant, "4.0");
    assertMutantCallableReturns(new HasFREM(3), mutant, "6.0");
  }

  // double

  private static class HasDADD implements Callable<String> {
    private double i;

    HasDADD(final double i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i++;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceDoubleAdditionWithSubtraction() throws Exception {
    final Mutant mutant = getFirstMutant(HasDADD.class);
    assertMutantCallableReturns(new HasDADD(2), mutant, "1.0");
    assertMutantCallableReturns(new HasDADD(20), mutant, "19.0");
  }

  private static class HasDSUB implements Callable<String> {
    private double i;

    HasDSUB(final double i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i--;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceDoubleSubtractionWithAddition() throws Exception {
    final Mutant mutant = getFirstMutant(HasDSUB.class);
    assertMutantCallableReturns(new HasDSUB(2), mutant, "3.0");
    assertMutantCallableReturns(new HasDSUB(20), mutant, "21.0");
  }

  private static class HasDMUL implements Callable<String> {
    private double i;

    HasDMUL(final double i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i = this.i * 2;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceDoubleMultiplicationWithDivision() throws Exception {
    final Mutant mutant = getFirstMutant(HasDMUL.class);
    assertMutantCallableReturns(new HasDMUL(2), mutant, "1.0");
    assertMutantCallableReturns(new HasDMUL(20), mutant, "10.0");
  }

  private static class HasDDIV implements Callable<String> {
    private double i;

    HasDDIV(final double i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i = this.i / 2;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceDoubleDivisionWithMultiplication() throws Exception {
    final Mutant mutant = getFirstMutant(HasDDIV.class);
    assertMutantCallableReturns(new HasDDIV(2), mutant, "4.0");
    assertMutantCallableReturns(new HasDDIV(20), mutant, "40.0");
  }

  private static class HasDREM implements Callable<String> {
    private double i;

    HasDREM(final double i) {
      this.i = i;
    }

    @Override
    public String call() {
      this.i = this.i % 2;
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceDoublerModulusWithMultiplication() throws Exception {
    final Mutant mutant = getFirstMutant(HasDREM.class);
    assertMutantCallableReturns(new HasDREM(2), mutant, "4.0");
    assertMutantCallableReturns(new HasDREM(3), mutant, "6.0");
  }

}
