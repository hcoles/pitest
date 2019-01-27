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
package org.pitest.mutationtest.engine.gregor.mutators.rv;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;
import org.pitest.mutationtest.engine.gregor.mutators.rv.AOD1Mutator;

import java.util.concurrent.Callable;

public class AOD1MutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToMutateOnlyMathFunctions() {
    createTesteeWith(AOD1Mutator.AOD_1_MUTATOR);
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
  public void shouldReplaceIntegerAdditionWithFistMember() throws Exception {
    final Mutant mutant = getFirstMutant(HasIAdd.class);
    assertMutantCallableReturns(new HasIAdd(2), mutant, "2");
    assertMutantCallableReturns(new HasIAdd(20), mutant, "20");
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
  public void shouldReplaceIntegerSubtractionWithFistMember() throws Exception {
    final Mutant mutant = getFirstMutant(HasISub.class);
    assertMutantCallableReturns(new HasISub(2), mutant, "2");
    assertMutantCallableReturns(new HasISub(20), mutant, "20");
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
  public void shouldReplaceIntegerMultiplicationWithFistMember() throws Exception {
    final Mutant mutant = getFirstMutant(HasIMul.class);
    assertMutantCallableReturns(new HasIMul(2), mutant, "2");
    assertMutantCallableReturns(new HasIMul(20), mutant, "20");
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
  public void shouldReplaceIntegerDivisionWithFistMember() throws Exception {
    final Mutant mutant = getFirstMutant(HasIDiv.class);
    assertMutantCallableReturns(new HasIDiv(2), mutant, "2");
    assertMutantCallableReturns(new HasIDiv(20), mutant, "20");
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
  public void shouldReplaceIntegerModulusWithFistMember() throws Exception {
    final Mutant mutant = getFirstMutant(HasIRem.class);
    assertMutantCallableReturns(new HasIRem(2), mutant, "2");
    assertMutantCallableReturns(new HasIRem(3), mutant, "3");
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
  public void shouldReplaceLongAdditionWithFistMember() throws Exception {
    final Mutant mutant = getFirstMutant(HasLAdd.class);
    assertMutantCallableReturns(new HasLAdd(2), mutant, "2");
    assertMutantCallableReturns(new HasLAdd(20), mutant, "20");
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
  public void shouldReplaceLongSubtractionWithFistMember() throws Exception {
    final Mutant mutant = getFirstMutant(HasLSub.class);
    assertMutantCallableReturns(new HasLSub(2), mutant, "2");
    assertMutantCallableReturns(new HasLSub(20), mutant, "20");
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
  public void shouldReplaceLongMultiplicationWithFistMember() throws Exception {
    final Mutant mutant = getFirstMutant(HasLMul.class);
    assertMutantCallableReturns(new HasLMul(2), mutant, "2");
    assertMutantCallableReturns(new HasLMul(20), mutant, "20");
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
  public void shouldReplaceLongDivisionWithFistMember() throws Exception {
    final Mutant mutant = getFirstMutant(HasLDiv.class);
    assertMutantCallableReturns(new HasLDiv(2), mutant, "2");
    assertMutantCallableReturns(new HasLDiv(20), mutant, "20");
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
  public void shouldReplaceLongModulusWithFistMember() throws Exception {
    final Mutant mutant = getFirstMutant(HasLRem.class);
    assertMutantCallableReturns(new HasLRem(2), mutant, "2");
    assertMutantCallableReturns(new HasLRem(3), mutant, "3");
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
  public void shouldReplaceFloatAdditionWithFistMember() throws Exception {
    final Mutant mutant = getFirstMutant(HasFADD.class);
    assertMutantCallableReturns(new HasFADD(2), mutant, "2.0");
    assertMutantCallableReturns(new HasFADD(20), mutant, "20.0");
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
  public void shouldReplaceFloatSubtractionWithFistMember() throws Exception {
    final Mutant mutant = getFirstMutant(HasFSUB.class);
    assertMutantCallableReturns(new HasFSUB(2), mutant, "2.0");
    assertMutantCallableReturns(new HasFSUB(20), mutant, "20.0");
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
  public void shouldReplaceFloatMultiplicationWithFistMember() throws Exception {
    final Mutant mutant = getFirstMutant(HasFMUL.class);
    assertMutantCallableReturns(new HasFMUL(2), mutant, "2.0");
    assertMutantCallableReturns(new HasFMUL(20), mutant, "20.0");
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
  public void shouldReplaceFloatDivisionWithFistMember() throws Exception {
    final Mutant mutant = getFirstMutant(HasFDIV.class);
    assertMutantCallableReturns(new HasFDIV(2), mutant, "2.0");
    assertMutantCallableReturns(new HasFDIV(20), mutant, "20.0");
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
  public void shouldReplaceFloatModulusWithFistMember() throws Exception {
    final Mutant mutant = getFirstMutant(HasFREM.class);
    assertMutantCallableReturns(new HasFREM(2), mutant, "2.0");
    assertMutantCallableReturns(new HasFREM(3), mutant, "3.0");
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
  public void shouldReplaceDoubleAdditionWithFistMember() throws Exception {
    final Mutant mutant = getFirstMutant(HasDADD.class);
    assertMutantCallableReturns(new HasDADD(2), mutant, "2.0");
    assertMutantCallableReturns(new HasDADD(20), mutant, "20.0");
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
  public void shouldReplaceDoubleSubtractionWithFistMember() throws Exception {
    final Mutant mutant = getFirstMutant(HasDSUB.class);
    assertMutantCallableReturns(new HasDSUB(2), mutant, "2.0");
    assertMutantCallableReturns(new HasDSUB(20), mutant, "20.0");
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
  public void shouldReplaceDoubleMultiplicationWithFistMember() throws Exception {
    final Mutant mutant = getFirstMutant(HasDMUL.class);
    assertMutantCallableReturns(new HasDMUL(2), mutant, "2.0");
    assertMutantCallableReturns(new HasDMUL(20), mutant, "20.0");
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
  public void shouldReplaceDoubleDivisionWithFistMember() throws Exception {
    final Mutant mutant = getFirstMutant(HasDDIV.class);
    assertMutantCallableReturns(new HasDDIV(2), mutant, "2.0");
    assertMutantCallableReturns(new HasDDIV(20), mutant, "20.0");
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
  public void shouldReplaceDoublerModulusWithFistMember() throws Exception {
    final Mutant mutant = getFirstMutant(HasDREM.class);
    assertMutantCallableReturns(new HasDREM(2), mutant, "2.0");
    assertMutantCallableReturns(new HasDREM(3), mutant, "3.0");
  }

}
