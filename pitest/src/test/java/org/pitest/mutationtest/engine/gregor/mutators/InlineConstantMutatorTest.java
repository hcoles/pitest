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

public class InlineConstantMutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToMutateOnlyInlineConstants() {
    createTesteeWith(InlineConstantMutator.INLINE_CONSTANT_MUTATOR);
  }

  private static int preventCodeFormattingMakingFinal(final int i) {
    return i;
  }

  private static long preventCodeFormattingMakingFinal(final long i) {
    return i;
  }

  private static float preventCodeFormattingMakingFinal(final float f) {
    return f;
  }

  private static double preventCodeFormattingMakingFinal(final double d) {
    return d;
  }

  private static boolean preventCodeFormattingMakingFinal(final boolean i) {
    return i;
  }

  private static class HasICONSTM1 implements Callable<String> {
    public String call() throws Exception {
      int i = -1;
      i = preventCodeFormattingMakingFinal(i);
      return "" + i;
    }
  }

  @Test
  public void shouldReplaceIntegerMinus1With1() throws Exception {
    final Mutant mutant = getFirstMutant(HasICONSTM1.class);
    System.out.println(mutant);
    this.printMutant(mutant);
    assertMutantCallableReturns(new HasICONSTM1(), mutant, "1");
  }

  private static class HasICONST0 implements Callable<String> {
    public String call() throws Exception {
      int i = 0;
      i = preventCodeFormattingMakingFinal(i);
      return "" + i;
    }
  }

  @Test
  public void shouldReplaceInteger0With1() throws Exception {
    final Mutant mutant = getFirstMutant(HasICONST0.class);
    assertMutantCallableReturns(new HasICONST0(), mutant, "1");
  }

  private static class HasBooleanICONST0 implements Callable<String> {
    public String call() throws Exception {
      boolean b = false;
      b = preventCodeFormattingMakingFinal(b);
      return "" + b;
    }
  }

  @Test
  public void shouldReplaceBooleanFalseWithTrue() throws Exception {
    final Mutant mutant = getFirstMutant(HasBooleanICONST0.class);
    assertMutantCallableReturns(new HasBooleanICONST0(), mutant, "true");
  }

  private static class HasICONST1 implements Callable<String> {
    public String call() throws Exception {
      int i = 1;
      i = preventCodeFormattingMakingFinal(i);
      return "" + i;
    }
  }

  @Test
  public void shouldReplaceInteger1With0() throws Exception {
    final Mutant mutant = getFirstMutant(HasICONST1.class);
    assertMutantCallableReturns(new HasICONST1(), mutant, "0");
  }

  private static class HasBooleanICONST1 implements Callable<String> {
    public String call() throws Exception {
      boolean i = true;
      i = preventCodeFormattingMakingFinal(i);
      return "" + i;
    }
  }

  @Test
  public void shouldReplaceBooleanTrueWithFalse() throws Exception {
    final Mutant mutant = getFirstMutant(HasBooleanICONST1.class);
    assertMutantCallableReturns(new HasBooleanICONST1(), mutant, "false");
  }

  private static class HasICONST2 implements Callable<String> {
    public String call() throws Exception {
      int i = 2;
      i = preventCodeFormattingMakingFinal(i);
      return "" + i;
    }
  }

  @Test
  public void shouldReplaceInteger2With3() throws Exception {
    final Mutant mutant = getFirstMutant(HasICONST2.class);
    assertMutantCallableReturns(new HasICONST2(), mutant, "3");
  }

  private static class HasICONST3 implements Callable<String> {
    public String call() throws Exception {
      int i = 3;
      i = preventCodeFormattingMakingFinal(i);
      return "" + i;
    }
  }

  @Test
  public void shouldReplaceInteger3With4() throws Exception {
    final Mutant mutant = getFirstMutant(HasICONST3.class);
    assertMutantCallableReturns(new HasICONST3(), mutant, "4");
  }

  private static class HasICONST4 implements Callable<String> {
    public String call() throws Exception {
      int i = 4;
      i = preventCodeFormattingMakingFinal(i);
      return "" + i;
    }
  }

  @Test
  public void shouldReplaceInteger4With5() throws Exception {
    final Mutant mutant = getFirstMutant(HasICONST4.class);
    assertMutantCallableReturns(new HasICONST4(), mutant, "5");
  }

  private static class HasICONST5 implements Callable<String> {
    public String call() throws Exception {
      int i = 5;
      i = preventCodeFormattingMakingFinal(i);
      return "" + i;
    }
  }

  @Test
  public void shouldReplaceInteger5WithMinus1() throws Exception {
    final Mutant mutant = getFirstMutant(HasICONST5.class);
    assertMutantCallableReturns(new HasICONST5(), mutant, "-1");
  }

  private static class HasFCONST0 implements Callable<String> {
    public String call() throws Exception {
      float i = 0.0f;
      i = preventCodeFormattingMakingFinal(i);
      return "" + i;
    }
  }

  @Test
  public void shouldReplaceFloat0With1() throws Exception {
    final Mutant mutant = getFirstMutant(HasFCONST0.class);
    assertMutantCallableReturns(new HasFCONST0(), mutant, "1.0");
  }

  private static class HasFCONST1 implements Callable<String> {
    public String call() throws Exception {
      float i = 1.0f;
      i = preventCodeFormattingMakingFinal(i);
      return "" + i;
    }
  }

  @Test
  public void shouldReplaceFloat1With0() throws Exception {
    final Mutant mutant = getFirstMutant(HasFCONST1.class);
    assertMutantCallableReturns(new HasFCONST1(), mutant, "0.0");
  }

  private static class HasFCONST2 implements Callable<String> {
    public String call() throws Exception {
      float i = 2;
      i = preventCodeFormattingMakingFinal(i);
      return "" + i;
    }
  }

  @Test
  public void shouldReplaceFloat2With0() throws Exception {
    final Mutant mutant = getFirstMutant(HasFCONST2.class);
    assertMutantCallableReturns(new HasFCONST2(), mutant, "0.0");
  }

  private static class HasDCONST0 implements Callable<String> {
    public String call() throws Exception {
      double i = 0;
      i = preventCodeFormattingMakingFinal(i);
      return "" + i;
    }
  }

  @Test
  public void shouldReplaceDouble0With1() throws Exception {
    final Mutant mutant = getFirstMutant(HasDCONST0.class);
    assertMutantCallableReturns(new HasDCONST0(), mutant, "1.0");
  }

  private static class HasDCONST1 implements Callable<String> {
    public String call() throws Exception {
      double i = 1;
      i = preventCodeFormattingMakingFinal(i);
      return "" + i;
    }
  }

  @Test
  public void shouldReplaceDouble1With0() throws Exception {
    final Mutant mutant = getFirstMutant(HasDCONST1.class);
    assertMutantCallableReturns(new HasDCONST1(), mutant, "0.0");
  }

  private static class HasLCONST0 implements Callable<String> {
    public String call() throws Exception {
      long i = 0;
      i = preventCodeFormattingMakingFinal(i);
      return "" + i;
    }
  }

  @Test
  public void shouldReplaceLong0With1() throws Exception {
    final Mutant mutant = getFirstMutant(HasLCONST0.class);
    assertMutantCallableReturns(new HasLCONST0(), mutant, "1");
  }

  private static class HasLCONST1 implements Callable<String> {
    public String call() throws Exception {
      long i = 1;
      i = preventCodeFormattingMakingFinal(i);
      return "" + i;
    }
  }

  @Test
  public void shouldReplaceLong1With0() throws Exception {
    final Mutant mutant = getFirstMutant(HasLCONST1.class);
    assertMutantCallableReturns(new HasLCONST1(), mutant, "0");
  }

  private static class HasBIPUSH implements Callable<String> {
    public String call() throws Exception {
      int i = 100;
      i++;
      return "" + i;
    }
  }

  @Test
  public void shouldReplaceIntegerConstantsWithValuePlus1() throws Exception {
    final Mutant mutant = getFirstMutant(HasBIPUSH.class);
    assertMutantCallableReturns(new HasBIPUSH(), mutant, "102");
  }

  private static class HasSIPUSH implements Callable<String> {
    public String call() throws Exception {
      short s = Short.MAX_VALUE;
      s++;
      return "" + s;
    }
  }

  @Test
  public void shouldReplaceShortConstantsWithValuePlus1() throws Exception {
    final Mutant mutant = getFirstMutant(HasSIPUSH.class);
    assertMutantCallableReturns(new HasSIPUSH(), mutant, ""
        + (Short.MIN_VALUE + 1));
  }

  private static class HasIntegerLDC implements Callable<String> {
    private int i() {
      return 42;
    }

    public String call() throws Exception {
      return "" + i();
    }
  }

  @Test
  public void shouldReplaceIntegerConstantsFromTheConstantPoolWithValuePlus1()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasIntegerLDC.class);
    assertMutantCallableReturns(new HasIntegerLDC(), mutant, "43");
  }

  private static class HasLongLDC implements Callable<String> {
    private long i() {
      return 42l;
    }

    public String call() throws Exception {
      return "" + i();
    }
  }

  @Test
  public void shouldReplaceLongConstantsFromTheConstantPoolWithValuePlus1()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasLongLDC.class);
    assertMutantCallableReturns(new HasLongLDC(), mutant, "43");
  }

  private static class HasFloatLDC implements Callable<String> {
    private float i() {
      return 42f;
    }

    public String call() throws Exception {
      return "" + i();
    }
  }

  @Test
  public void shouldReplaceFloatConstantsFromTheConstantPoolWith1()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasFloatLDC.class);
    assertMutantCallableReturns(new HasFloatLDC(), mutant, "1.0");
  }

  private static class HasDoubleLDC implements Callable<String> {
    private double i() {
      return 0d;
    }

    public String call() throws Exception {
      return "" + i();
    }
  }

  @Test
  public void shouldReplaceDoubleConstantsFromTheConstantPoolWith1()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasDoubleLDC.class);
    assertMutantCallableReturns(new HasDoubleLDC(), mutant, "1.0");
  }

}
