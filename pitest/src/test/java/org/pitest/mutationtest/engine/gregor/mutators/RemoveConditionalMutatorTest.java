package org.pitest.mutationtest.engine.gregor.mutators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;

public class RemoveConditionalMutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToMutateOnlyConditionals() {
    createTesteeWith(RemoveConditionalMutator.REMOVE_CONDITIONALS_MUTATOR);
  }

  @Test
  public void shouldProvideAMeaningfulName() {
    assertEquals("REMOVE_CONDITIONALS_MUTATOR",
        RemoveConditionalMutator.REMOVE_CONDITIONALS_MUTATOR.getName());
  }

  private static int getZeroButPreventInlining() {
    return 0;
  }

  private static class HasIFEQ implements Callable<String> {
    private final int i;

    HasIFEQ(final int i) {
      this.i = i;
    }

    public String call() {
      if (this.i != 0) {
        return "was not zero";
      } else {
        return "was zero";
      }
    }
  }

  @Test
  public void shouldReplaceIFEQ() throws Exception {
    final Mutant mutant = getFirstMutant(HasIFEQ.class);
    final String expected = "was not zero";
    assertMutantCallableReturns(new HasIFEQ(1), mutant, expected);
    assertMutantCallableReturns(new HasIFEQ(0), mutant, expected);
  }

  private static class HasIFNE implements Callable<String> {
    private final int i;

    HasIFNE(final int i) {
      this.i = i;
    }

    public String call() {
      if (this.i == 0) {
        return "was zero";
      } else {
        return "was not zero";
      }
    }
  }

  @Test
  public void shouldReplaceIFNE() throws Exception {
    final Mutant mutant = getFirstMutant(HasIFNE.class);
    final String expected = "was zero";
    assertMutantCallableReturns(new HasIFNE(1), mutant, expected);
    assertMutantCallableReturns(new HasIFNE(0), mutant, expected);
  }

  private static class HasIFNULL implements Callable<String> {
    private final Object i;

    HasIFNULL(final Object i) {
      this.i = i;
    }

    public String call() {
      if (this.i != null) {
        return "was not null";
      } else {
        return "was null";
      }
    }
  }

  @Test
  public void shouldReplaceIFNULL() throws Exception {
    final Mutant mutant = getFirstMutant(HasIFNULL.class);
    final String expected = "was not null";
    assertMutantCallableReturns(new HasIFNULL(null), mutant, expected);
    assertMutantCallableReturns(new HasIFNULL("foo"), mutant, expected);
  }

  private static class HasIFNONNULL implements Callable<String> {
    private final Object i;

    HasIFNONNULL(final Object i) {
      this.i = i;
    }

    public String call() {
      if (this.i == null) {
        return "was null";
      } else {
        return "was not null";
      }
    }
  }

  @Test
  public void shouldReplaceIFNONNULL() throws Exception {
    final Mutant mutant = getFirstMutant(HasIFNONNULL.class);
    final String expected = "was null";
    assertMutantCallableReturns(new HasIFNONNULL(null), mutant, expected);
    assertMutantCallableReturns(new HasIFNONNULL("foo"), mutant, expected);
  }

  private static class HasIF_ICMPNE implements Callable<String> {
    private final int i;

    HasIF_ICMPNE(final int i) {
      this.i = i;
    }

    public String call() {
      final int j = getZeroButPreventInlining();
      if (this.i == j) {
        return "was zero";
      } else {
        return "was not zero";
      }
    }
  }

  @Test
  public void shouldReplaceIF_ICMPNE() throws Exception {
    final Mutant mutant = getFirstMutant(HasIF_ICMPNE.class);
    final String expected = "was zero";
    assertMutantCallableReturns(new HasIF_ICMPNE(1), mutant, expected);
    assertMutantCallableReturns(new HasIF_ICMPNE(0), mutant, expected);
  }

  private static class HasIF_ICMPEQ implements Callable<String> {
    private final int i;

    HasIF_ICMPEQ(final int i) {
      this.i = i;
    }

    public String call() {
      final int j = getZeroButPreventInlining();
      if (this.i != j) {
        return "was not zero";
      } else {
        return "was zero";
      }
    }
  }

  @Test
  public void shouldReplaceIF_ICMPEQ() throws Exception {
    final Mutant mutant = getFirstMutant(HasIF_ICMPEQ.class);
    final String expected = "was not zero";
    assertMutantCallableReturns(new HasIF_ICMPEQ(1), mutant, expected);
    assertMutantCallableReturns(new HasIF_ICMPEQ(0), mutant, expected);
  }
  
 static class HasIF_ACMPEQ implements Callable<String> {
    private final Object i;

    HasIF_ACMPEQ(final Object i) {
      this.i = i;
    }

    public String call() {
      if (this.i != this) {
        return "was not zero";
      } else {
        return "was zero";
      }
    }
  }

  @Test
  public void shouldReplaceIF_ACMPEQ() throws Exception {
    final Mutant mutant = getFirstMutant(HasIF_ACMPEQ.class);
    final String expected = "was not zero";
    assertMutantCallableReturns(new HasIF_ACMPEQ(1), mutant, expected);
    assertMutantCallableReturns(new HasIF_ACMPEQ(0), mutant, expected);
  }
  
  static class HasIF_ACMPNE implements Callable<String> {
    private final Object i;

    HasIF_ACMPNE(final Object i) {
      this.i = i;
    }

    public String call() {
      if (this.i == this) {
        return "was not zero";
      } else {
        return "was zero";
      }
    }
  }

  @Test
  public void shouldReplaceIF_ACMPNE() throws Exception {
    final Mutant mutant = getFirstMutant(HasIF_ACMPNE.class);
    final String expected = "was not zero";
    assertMutantCallableReturns(new HasIF_ACMPNE(1), mutant, expected);
    assertMutantCallableReturns(new HasIF_ACMPNE(0), mutant, expected);
  }

  static class HasIFLE implements Callable<String> {
    private final int i;

    HasIFLE(final int i) {
      this.i = i;
    }

    public String call() {
      if (this.i > 0) {
        return "was > zero";
      } else {
        return "was <= zero";
      }
    }
  }

  @Test
  public void shouldNotReplaceIFLE() throws Exception {
    assertNoMutants(HasIFLE.class);
  }

  static class HasIFGE implements Callable<String> {
    private final int i;

    HasIFGE(final int i) {
      this.i = i;
    }

    public String call() {
      if (this.i < 0) {
        return "was < zero";
      } else {
        return "was >= zero";
      }
    }
  }

  @Test
  public void shouldNotReplaceIFGE() throws Exception {
    assertNoMutants(HasIFGE.class);
  }

  static class HasIFGT implements Callable<String> {
    private final int i;

    HasIFGT(final int i) {
      this.i = i;
    }

    public String call() {
      if (this.i <= 0) {
        return "was <= zero";
      } else {
        return "was > zero";
      }
    }
  }

  @Test
  public void shouldNotReplaceIFGT() throws Exception {
    assertNoMutants(HasIFGT.class);
  }

  static class HasIFLT implements Callable<String> {
    private final int i;

    HasIFLT(final int i) {
      this.i = i;
    }

    public String call() {
      if (this.i >= 0) {
        return "was >= zero";
      } else {
        return "was < zero";
      }
    }
  }

  @Test
  public void shouldNotReplaceIFLT() throws Exception {
    assertNoMutants(HasIFLT.class);
  }

  static class HasIF_ICMPLE implements Callable<String> {
    private final int i;

    HasIF_ICMPLE(final int i) {
      this.i = i;
    }

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
  public void shouldNotReplaceIF_ICMPLE() throws Exception {
    assertNoMutants(HasIF_ICMPLE.class);
  }

  static class HasIF_ICMPGE implements Callable<String> {
    private final int i;

    HasIF_ICMPGE(final int i) {
      this.i = i;
    }

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
  public void shouldNotReplaceIF_ICMPGE() throws Exception {
    assertNoMutants(HasIF_ICMPGE.class);
  }

  static class HasIF_ICMPGT implements Callable<String> {
    private final int i;

    HasIF_ICMPGT(final int i) {
      this.i = i;
    }

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
  public void shouldNotReplaceIF_ICMPGT() throws Exception {
    assertNoMutants(HasIF_ICMPGT.class);
  }

  static class HasIF_ICMPLT implements Callable<String> {
    private final int i;

    HasIF_ICMPLT(final int i) {
      this.i = i;
    }

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
  public void shouldNotReplaceIF_ICMPLT() throws Exception {
    assertNoMutants(HasIF_ICMPLT.class);
  }

  private void assertNoMutants(final Class<?> mutee) {
    final Collection<MutationDetails> actual = findMutationsFor(mutee);
    assertTrue(actual.isEmpty());

  }

}
