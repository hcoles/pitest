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

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;

public class ConditionalsBoundaryMutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToMutateOnlyConditionals() {
    createTesteeWith(ConditionalsBoundaryMutator.CONDITIONALS_BOUNDARY_MUTATOR);
  }

  @Test
  public void shouldProvideAMeaningfulName() {
    assertEquals("CONDITIONALS_BOUNDARY_MUTATOR",
        ConditionalsBoundaryMutator.CONDITIONALS_BOUNDARY_MUTATOR.getName());
  }

  private static int getZeroButPreventInlining() {
    return 0;
  }

  private static class HasIFLE implements Callable<String> {
    private final int i;

    HasIFLE(final int i) {
      this.i = i;
    }

    @Override
    public String call() {
      if (this.i > 0) {
        return "was > zero";
      } else {
        return "was <= zero";
      }
    }
  }

  @Test
  public void shouldReplaceIFLEwithILT() throws Exception {
    final Mutant mutant = getFirstMutant(HasIFLE.class);
    assertMutantCallableReturns(new HasIFLE(1), mutant, "was > zero");
    assertMutantCallableReturns(new HasIFLE(-1), mutant, "was <= zero");
    assertMutantCallableReturns(new HasIFLE(0), mutant, "was > zero");
  }

  private static class HasIFGE implements Callable<String> {
    private final int i;

    HasIFGE(final int i) {
      this.i = i;
    }

    @Override
    public String call() {
      if (this.i < 0) {
        return "was < zero";
      } else {
        return "was >= zero";
      }
    }
  }

  @Test
  public void shouldReplaceIFGEwithIFGT() throws Exception {
    final Mutant mutant = getFirstMutant(HasIFGE.class);
    assertMutantCallableReturns(new HasIFGE(-1), mutant, "was < zero");
    assertMutantCallableReturns(new HasIFGE(1), mutant, "was >= zero");
    assertMutantCallableReturns(new HasIFGE(0), mutant, "was < zero");
  }

  private static class HasIFGT implements Callable<String> {
    private final int i;

    HasIFGT(final int i) {
      this.i = i;
    }

    @Override
    public String call() {
      if (this.i <= 0) {
        return "was <= zero";
      } else {
        return "was > zero";
      }
    }
  }

  @Test
  public void shouldReplaceIFGTwithIFGE() throws Exception {
    final Mutant mutant = getFirstMutant(HasIFGT.class);
    assertMutantCallableReturns(new HasIFGT(-1), mutant, "was <= zero");
    assertMutantCallableReturns(new HasIFGT(1), mutant, "was > zero");
    assertMutantCallableReturns(new HasIFGT(0), mutant, "was > zero");
  }

  private static class HasIFLT implements Callable<String> {
    private final int i;

    HasIFLT(final int i) {
      this.i = i;
    }

    @Override
    public String call() {
      if (this.i >= 0) {
        return "was >= zero";
      } else {
        return "was < zero";
      }
    }
  }

  @Test
  public void shouldReplaceIFLTwithIFLE() throws Exception {
    final Mutant mutant = getFirstMutant(HasIFLT.class);
    assertMutantCallableReturns(new HasIFLT(-1), mutant, "was < zero");
    assertMutantCallableReturns(new HasIFLT(1), mutant, "was >= zero");
    assertMutantCallableReturns(new HasIFLT(0), mutant, "was < zero");
  }

  private static class HasIF_ICMPLE implements Callable<String> {
    private final int i;

    HasIF_ICMPLE(final int i) {
      this.i = i;
    }

    @Override
    public String call() {
      final int j = getZeroButPreventInlining();
      if (this.i > j) {
        return "was > zero";
      } else {
        return "was <= zero";
      }
    }
  }

  @Test
  public void shouldReplaceICMPLEwithIF_ICMPLT() throws Exception {
    final Mutant mutant = getFirstMutant(HasIF_ICMPLE.class);
    assertMutantCallableReturns(new HasIF_ICMPLE(1), mutant, "was > zero");
    assertMutantCallableReturns(new HasIF_ICMPLE(-1), mutant, "was <= zero");
    assertMutantCallableReturns(new HasIF_ICMPLE(0), mutant, "was > zero");
  }

  private static class HasIF_ICMPGE implements Callable<String> {
    private final int i;

    HasIF_ICMPGE(final int i) {
      this.i = i;
    }

    @Override
    public String call() {
      final int j = getZeroButPreventInlining();
      if (this.i < j) {
        return "was < zero";
      } else {
        return "was >= zero";
      }
    }
  }

  @Test
  public void shouldReplaceIF_ICMPGEwithIF_ICMPGT() throws Exception {
    final Mutant mutant = getFirstMutant(HasIF_ICMPGE.class);
    assertMutantCallableReturns(new HasIF_ICMPGE(-1), mutant, "was < zero");
    assertMutantCallableReturns(new HasIF_ICMPGE(1), mutant, "was >= zero");
    assertMutantCallableReturns(new HasIF_ICMPGE(0), mutant, "was < zero");
  }

  private static class HasIF_ICMPGT implements Callable<String> {
    private final int i;

    HasIF_ICMPGT(final int i) {
      this.i = i;
    }

    @Override
    public String call() {
      final int j = getZeroButPreventInlining();
      if (this.i <= j) {
        return "was <= zero";
      } else {
        return "was > zero";
      }
    }
  }

  @Test
  public void shouldReplaceIF_ICMPGTwithIF_ICMPGE() throws Exception {
    final Mutant mutant = getFirstMutant(HasIF_ICMPGT.class);
    assertMutantCallableReturns(new HasIF_ICMPGT(-1), mutant, "was <= zero");
    assertMutantCallableReturns(new HasIF_ICMPGT(1), mutant, "was > zero");
    assertMutantCallableReturns(new HasIF_ICMPGT(0), mutant, "was > zero");
  }

  private static class HasIF_ICMPLT implements Callable<String> {
    private final int i;

    HasIF_ICMPLT(final int i) {
      this.i = i;
    }

    @Override
    public String call() {
      final int j = getZeroButPreventInlining();
      if (this.i >= j) {
        return "was >= zero";
      } else {
        return "was < zero";
      }
    }
  }

  @Test
  public void shouldReplaceIF_ICMPLTwithIF_ICMPGT() throws Exception {
    final Mutant mutant = getFirstMutant(HasIF_ICMPLT.class);
    assertMutantCallableReturns(new HasIF_ICMPLT(-1), mutant, "was < zero");
    assertMutantCallableReturns(new HasIF_ICMPLT(1), mutant, "was >= zero");
    assertMutantCallableReturns(new HasIF_ICMPLT(0), mutant, "was < zero");
  }

}
