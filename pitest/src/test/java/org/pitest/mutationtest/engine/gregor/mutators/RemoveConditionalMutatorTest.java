package org.pitest.mutationtest.engine.gregor.mutators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.Callable;

import org.junit.Test;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;
import org.pitest.mutationtest.engine.gregor.mutators.RemoveConditionalMutator.Choice;

public class RemoveConditionalMutatorTest extends MutatorTestBase {

  @Test
  public void shouldProvideAMeaningfulName() {
    assertEquals("REMOVE_CONDITIONALS_EQUAL_IF_MUTATOR",
        new RemoveConditionalMutator(Choice.EQUAL, true).getName());
    assertEquals("REMOVE_CONDITIONALS_EQUAL_ELSE_MUTATOR",
        new RemoveConditionalMutator(Choice.EQUAL, false).getName());
    assertEquals("REMOVE_CONDITIONALS_ORDER_IF_MUTATOR",
        new RemoveConditionalMutator(Choice.ORDER, true).getName());
    assertEquals("REMOVE_CONDITIONALS_ORDER_ELSE_MUTATOR",
        new RemoveConditionalMutator(Choice.ORDER, false).getName());
  }

  private static int getZeroButPreventInlining() {
    return 0;
  }

  private static class HasIFEQ implements Callable<String> {
    private final int i;

    HasIFEQ(final int i) {
      this.i = i;
    }

    @Override
    public String call() {
      if (this.i != 0) {
        return "was not zero";
      } else {
        return "was zero";
      }
    }
  }

  @Test
  public void shouldReplaceIFEQ_EQUAL_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, true));
    final Mutant mutant = getFirstMutant(HasIFEQ.class);
    final String expected = "was not zero";
    assertMutantCallableReturns(new HasIFEQ(1), mutant, expected);
    assertMutantCallableReturns(new HasIFEQ(0), mutant, expected);
  }

  @Test
  public void shouldDescribeReplacementOfEqualityChecksWithTrue() {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, true));
    final Mutant mutant = getFirstMutant(HasIFEQ.class);
    assertThat(mutant.getDetails().getDescription()).contains(
        "equality check with true");
  }

  @Test
  public void shouldDescribeReplacementOfEqualityChecksWithFalse() {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, false));
    final Mutant mutant = getFirstMutant(HasIFEQ.class);
    assertThat(mutant.getDetails().getDescription()).contains(
        "equality check with false");
  }

  @Test
  public void shouldReplaceIFEQ_EQUAL_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, false));
    final Mutant mutant = getFirstMutant(HasIFEQ.class);
    final String expected = "was zero";
    assertMutantCallableReturns(new HasIFEQ(1), mutant, expected);
    assertMutantCallableReturns(new HasIFEQ(0), mutant, expected);
  }

  @Test
  public void shouldNotReplaceIFEQ_ORDER_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, true));
    assertNoMutants(HasIFEQ.class);
  }

  @Test
  public void shouldNotReplaceIFEQ_ORDER_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, false));
    assertNoMutants(HasIFEQ.class);
  }

  private static class HasIFNE implements Callable<String> {
    private final int i;

    HasIFNE(final int i) {
      this.i = i;
    }

    @Override
    public String call() {
      if (this.i == 0) {
        return "was zero";
      } else {
        return "was not zero";
      }
    }
  }

  @Test
  public void shouldReplaceIFNE_EQUAL_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, true));
    final Mutant mutant = getFirstMutant(HasIFNE.class);
    final String expected = "was zero";
    assertMutantCallableReturns(new HasIFNE(1), mutant, expected);
    assertMutantCallableReturns(new HasIFNE(0), mutant, expected);
  }

  @Test
  public void shouldReplaceIFNE_EQUAL_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, false));
    final Mutant mutant = getFirstMutant(HasIFNE.class);
    final String expected = "was not zero";
    assertMutantCallableReturns(new HasIFNE(1), mutant, expected);
    assertMutantCallableReturns(new HasIFNE(0), mutant, expected);
  }

  @Test
  public void shouldReplaceIFNE_ORDER_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, true));
    assertNoMutants(HasIFNE.class);
  }

  @Test
  public void shouldReplaceIFNE_ORDER_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, false));
    assertNoMutants(HasIFNE.class);
  }

  private static class HasIFNULL implements Callable<String> {
    private final Object i;

    HasIFNULL(final Object i) {
      this.i = i;
    }

    @Override
    public String call() {
      if (this.i != null) {
        return "was not null";
      } else {
        return "was null";
      }
    }
  }

  @Test
  public void shouldReplaceIFNULL_EQUAL_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, true));
    final Mutant mutant = getFirstMutant(HasIFNULL.class);
    final String expected = "was not null";
    assertMutantCallableReturns(new HasIFNULL(null), mutant, expected);
    assertMutantCallableReturns(new HasIFNULL("foo"), mutant, expected);
  }

  @Test
  public void shouldReplaceIFNULL_EQUAL_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, false));
    final Mutant mutant = getFirstMutant(HasIFNULL.class);
    final String expected = "was null";
    assertMutantCallableReturns(new HasIFNULL(null), mutant, expected);
    assertMutantCallableReturns(new HasIFNULL("foo"), mutant, expected);
  }

  @Test
  public void shouldReplaceIFNULL_ORDER_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, true));
    assertNoMutants(HasIFNULL.class);
  }

  @Test
  public void shouldReplaceIFNULL_ORDER_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, false));
    assertNoMutants(HasIFNULL.class);
  }

  private static class HasIFNONNULL implements Callable<String> {
    private final Object i;

    HasIFNONNULL(final Object i) {
      this.i = i;
    }

    @Override
    public String call() {
      if (this.i == null) {
        return "was null";
      } else {
        return "was not null";
      }
    }
  }

  @Test
  public void shouldReplaceIFNONNULL_EQUAL_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, true));
    final Mutant mutant = getFirstMutant(HasIFNONNULL.class);
    final String expected = "was null";
    assertMutantCallableReturns(new HasIFNONNULL(null), mutant, expected);
    assertMutantCallableReturns(new HasIFNONNULL("foo"), mutant, expected);
  }

  @Test
  public void shouldReplaceIFNONNULL_EQUAL_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, false));
    final Mutant mutant = getFirstMutant(HasIFNONNULL.class);
    final String expected = "was not null";
    assertMutantCallableReturns(new HasIFNONNULL(null), mutant, expected);
    assertMutantCallableReturns(new HasIFNONNULL("foo"), mutant, expected);
  }

  @Test
  public void shouldReplaceIFNONNULL_ORDER_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, true));
    assertNoMutants(HasIFNONNULL.class);
  }

  @Test
  public void shouldReplaceIFNONNULL_ORDER_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, false));
    assertNoMutants(HasIFNONNULL.class);
  }

  private static class HasIF_ICMPNE implements Callable<String> {
    private final int i;

    HasIF_ICMPNE(final int i) {
      this.i = i;
    }

    @Override
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
  public void shouldReplaceIF_ICMPNE_EQUAL_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, true));
    final Mutant mutant = getFirstMutant(HasIF_ICMPNE.class);
    final String expected = "was zero";
    assertMutantCallableReturns(new HasIF_ICMPNE(1), mutant, expected);
    assertMutantCallableReturns(new HasIF_ICMPNE(0), mutant, expected);
  }

  @Test
  public void shouldReplaceIF_ICMPNE_EQUAL_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, false));
    final Mutant mutant = getFirstMutant(HasIF_ICMPNE.class);
    final String expected = "was not zero";
    assertMutantCallableReturns(new HasIF_ICMPNE(1), mutant, expected);
    assertMutantCallableReturns(new HasIF_ICMPNE(0), mutant, expected);
  }

  @Test
  public void shouldReplaceIF_ICMPNE_ORDER_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, true));
    assertNoMutants(HasIF_ICMPNE.class);
  }

  @Test
  public void shouldReplaceIF_ICMPNE_ORDER_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, false));
    assertNoMutants(HasIF_ICMPNE.class);
  }

  private static class HasIF_ICMPEQ implements Callable<String> {
    private final int i;

    HasIF_ICMPEQ(final int i) {
      this.i = i;
    }

    @Override
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
  public void shouldReplaceIF_ICMPEQ_EQUAL_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, true));
    final Mutant mutant = getFirstMutant(HasIF_ICMPEQ.class);
    final String expected = "was not zero";
    assertMutantCallableReturns(new HasIF_ICMPEQ(1), mutant, expected);
    assertMutantCallableReturns(new HasIF_ICMPEQ(0), mutant, expected);
  }

  @Test
  public void shouldReplaceIF_ICMPEQ_EQUAL_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, false));
    final Mutant mutant = getFirstMutant(HasIF_ICMPEQ.class);
    final String expected = "was zero";
    assertMutantCallableReturns(new HasIF_ICMPEQ(1), mutant, expected);
    assertMutantCallableReturns(new HasIF_ICMPEQ(0), mutant, expected);
  }

  @Test
  public void shouldReplaceIF_ICMPEQ_ORDER_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, true));
    assertNoMutants(HasIF_ICMPEQ.class);
  }

  @Test
  public void shouldReplaceIF_ICMPEQ_ORDER_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, false));
    assertNoMutants(HasIF_ICMPEQ.class);
  }

  static class HasIF_ACMPEQ implements Callable<String> {
    private final Object i;

    HasIF_ACMPEQ(final Object i) {
      this.i = i;
    }

    @Override
    public String call() {
      if (this.i != this) {
        return "was not zero";
      } else {
        return "was zero";
      }
    }
  }

  @Test
  public void shouldReplaceIF_ACMPEQ_EQUAL_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, true));
    final Mutant mutant = getFirstMutant(HasIF_ACMPEQ.class);
    final String expected = "was not zero";
    assertMutantCallableReturns(new HasIF_ACMPEQ(1), mutant, expected);
    assertMutantCallableReturns(new HasIF_ACMPEQ(0), mutant, expected);
  }

  @Test
  public void shouldReplaceIF_ACMPEQ_EQUAL_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, false));
    final Mutant mutant = getFirstMutant(HasIF_ACMPEQ.class);
    final String expected = "was zero";
    assertMutantCallableReturns(new HasIF_ACMPEQ(1), mutant, expected);
    assertMutantCallableReturns(new HasIF_ACMPEQ(0), mutant, expected);
  }

  @Test
  public void shouldReplaceIF_ACMPEQ_ORDER_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, true));
    assertNoMutants(HasIF_ACMPEQ.class);
  }

  @Test
  public void shouldReplaceIF_ACMPEQ_ORDER_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, false));
    assertNoMutants(HasIF_ACMPEQ.class);
  }

  static class HasIF_ACMPNE implements Callable<String> {
    private final Object i;

    HasIF_ACMPNE(final Object i) {
      this.i = i;
    }

    @Override
    public String call() {
      if (this.i == this) {
        return "was not zero";
      } else {
        return "was zero";
      }
    }
  }

  @Test
  public void shouldReplaceIF_ACMPNE_EQUAL_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, true));
    final Mutant mutant = getFirstMutant(HasIF_ACMPNE.class);
    final String expected = "was not zero";
    assertMutantCallableReturns(new HasIF_ACMPNE(1), mutant, expected);
    assertMutantCallableReturns(new HasIF_ACMPNE(0), mutant, expected);
  }

  @Test
  public void shouldReplaceIF_ACMPNE_EQUAL_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, false));
    final Mutant mutant = getFirstMutant(HasIF_ACMPNE.class);
    final String expected = "was zero";
    assertMutantCallableReturns(new HasIF_ACMPNE(1), mutant, expected);
    assertMutantCallableReturns(new HasIF_ACMPNE(0), mutant, expected);
  }

  @Test
  public void shouldReplaceIF_ACMPNE_ORDER_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, true));
    assertNoMutants(HasIF_ACMPNE.class);
  }

  @Test
  public void shouldReplaceIF_ACMPNE_ORDER_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, false));
    assertNoMutants(HasIF_ACMPNE.class);
  }

  static class HasIFLE implements Callable<String> {
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
  public void shouldNotReplaceIFLE_EQUAL_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, true));
    assertNoMutants(HasIFLE.class);
  }

  @Test
  public void shouldNotReplaceIFLE_EQUAL_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, false));
    assertNoMutants(HasIFLE.class);
  }

  @Test
  public void shouldReplaceIFLE_ORDER_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, true));
    final Mutant mutant = getFirstMutant(HasIFLE.class);
    final String expected = "was > zero";
    assertMutantCallableReturns(new HasIFLE(1), mutant, expected);
    assertMutantCallableReturns(new HasIFLE(0), mutant, expected);
  }

  @Test
  public void shouldReplaceIFLE_ORDER_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, false));
    final Mutant mutant = getFirstMutant(HasIFLE.class);
    final String expected = "was <= zero";
    assertMutantCallableReturns(new HasIFLE(1), mutant, expected);
    assertMutantCallableReturns(new HasIFLE(0), mutant, expected);
  }

  @Test
  public void shouldDescribeReplacementOfOrderCheckWithTrue() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, true));
    final Mutant mutant = getFirstMutant(HasIFLE.class);
    assertThat(mutant.getDetails().getDescription()).contains(
        " comparison check with true");
  }

  @Test
  public void shouldDescribeReplacementOfOrderCheckWithFalse() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, false));
    final Mutant mutant = getFirstMutant(HasIFLE.class);
    assertThat(mutant.getDetails().getDescription()).contains(
        " comparison check with false");
  }

  static class HasIFGE implements Callable<String> {
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
  public void shouldNotReplaceIFGE_EQUAL_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, true));
    assertNoMutants(HasIFGE.class);
  }

  @Test
  public void shouldNotReplaceIFGE_EQUAL_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, false));
    assertNoMutants(HasIFGE.class);
  }

  @Test
  public void shouldReplaceIFGE_ORDER_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, true));
    final Mutant mutant = getFirstMutant(HasIFGE.class);
    final String expected = "was < zero";
    assertMutantCallableReturns(new HasIFGE(1), mutant, expected);
    assertMutantCallableReturns(new HasIFGE(0), mutant, expected);
  }

  @Test
  public void shouldReplaceIFGE_ORDER_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, false));
    final Mutant mutant = getFirstMutant(HasIFGE.class);
    final String expected = "was >= zero";
    assertMutantCallableReturns(new HasIFGE(1), mutant, expected);
    assertMutantCallableReturns(new HasIFGE(0), mutant, expected);
  }

  static class HasIFGT implements Callable<String> {
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
  public void shouldNotReplaceIFGT_EQUAL_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, true));
    assertNoMutants(HasIFGT.class);
  }

  @Test
  public void shouldNotReplaceIFGT_EQUAL_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, false));
    assertNoMutants(HasIFGT.class);
  }

  @Test
  public void shouldReplaceIFGT_ORDER_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, true));
    final Mutant mutant = getFirstMutant(HasIFGT.class);
    final String expected = "was <= zero";
    assertMutantCallableReturns(new HasIFGT(1), mutant, expected);
    assertMutantCallableReturns(new HasIFGT(0), mutant, expected);
  }

  @Test
  public void shouldReplaceIFGT_ORDER_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, false));
    final Mutant mutant = getFirstMutant(HasIFGT.class);
    final String expected = "was > zero";
    assertMutantCallableReturns(new HasIFGT(1), mutant, expected);
    assertMutantCallableReturns(new HasIFGT(0), mutant, expected);
  }

  static class HasIFLT implements Callable<String> {
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
  public void shouldNotReplaceIFLT_EQUAL_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, true));
    assertNoMutants(HasIFLT.class);
  }

  @Test
  public void shouldNotReplaceIFLT_EQUAL_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, false));
    assertNoMutants(HasIFLT.class);
  }

  @Test
  public void shouldReplaceIFLT_ORDER_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, true));
    final Mutant mutant = getFirstMutant(HasIFLT.class);
    final String expected = "was >= zero";
    assertMutantCallableReturns(new HasIFLT(1), mutant, expected);
    assertMutantCallableReturns(new HasIFLT(0), mutant, expected);
  }

  @Test
  public void shouldReplaceIFLT_ORDER_T_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, false));
    final Mutant mutant = getFirstMutant(HasIFLT.class);
    final String expected = "was < zero";
    assertMutantCallableReturns(new HasIFLT(1), mutant, expected);
    assertMutantCallableReturns(new HasIFLT(0), mutant, expected);
  }

  static class HasIF_ICMPLE implements Callable<String> {
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
  public void shouldNotReplaceIF_ICMPLE_EQUAL_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, true));
    assertNoMutants(HasIF_ICMPLE.class);
  }

  @Test
  public void shouldNotReplaceIF_ICMPLE_EQUAL_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, false));
    assertNoMutants(HasIF_ICMPLE.class);
  }

  @Test
  public void shouldReplaceIF_ICMPLE_ORDER_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, true));
    final Mutant mutant = getFirstMutant(HasIF_ICMPLE.class);
    final String expected = "was > zero";
    assertMutantCallableReturns(new HasIF_ICMPLE(1), mutant, expected);
    assertMutantCallableReturns(new HasIF_ICMPLE(0), mutant, expected);
  }

  @Test
  public void shouldReplaceIF_ICMPLE_ORDER_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, false));
    final Mutant mutant = getFirstMutant(HasIF_ICMPLE.class);
    final String expected = "was <= zero";
    assertMutantCallableReturns(new HasIF_ICMPLE(1), mutant, expected);
    assertMutantCallableReturns(new HasIF_ICMPLE(0), mutant, expected);
  }

  static class HasIF_ICMPGE implements Callable<String> {
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
  public void shouldNotReplaceIF_ICMPGE_EQUAL_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, true));
    assertNoMutants(HasIF_ICMPGE.class);
  }

  @Test
  public void shouldNotReplaceIF_ICMPGE_EQUAL_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, false));
    assertNoMutants(HasIF_ICMPGE.class);
  }

  @Test
  public void shouldReplaceIF_ICMPGE_ORDER_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, true));
    final Mutant mutant = getFirstMutant(HasIF_ICMPGE.class);
    final String expected = "was < zero";
    assertMutantCallableReturns(new HasIF_ICMPGE(1), mutant, expected);
    assertMutantCallableReturns(new HasIF_ICMPGE(0), mutant, expected);
  }

  @Test
  public void shouldReplaceIF_ICMPGE_ORDER_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, false));
    final Mutant mutant = getFirstMutant(HasIF_ICMPGE.class);
    final String expected = "was >= zero";
    assertMutantCallableReturns(new HasIF_ICMPGE(1), mutant, expected);
    assertMutantCallableReturns(new HasIF_ICMPGE(0), mutant, expected);
  }

  static class HasIF_ICMPGT implements Callable<String> {
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
  public void shouldNotReplaceIF_ICMPGT_EQUAL_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, true));
    assertNoMutants(HasIF_ICMPGT.class);
  }

  @Test
  public void shouldNotReplaceIF_ICMPGT_EQUAL_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, false));
    assertNoMutants(HasIF_ICMPGT.class);
  }

  @Test
  public void shouldReplaceIF_ICMPGT_ORDER_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, true));
    final Mutant mutant = getFirstMutant(HasIF_ICMPGT.class);
    final String expected = "was <= zero";
    assertMutantCallableReturns(new HasIF_ICMPGT(1), mutant, expected);
    assertMutantCallableReturns(new HasIF_ICMPGT(0), mutant, expected);
  }

  @Test
  public void shouldReplaceIF_ICMPGT_ORDER_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, false));
    final Mutant mutant = getFirstMutant(HasIF_ICMPGT.class);
    final String expected = "was > zero";
    assertMutantCallableReturns(new HasIF_ICMPGT(1), mutant, expected);
    assertMutantCallableReturns(new HasIF_ICMPGT(0), mutant, expected);
  }

  static class HasIF_ICMPLT implements Callable<String> {
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
  public void shouldNotReplaceIF_ICMPLT_EQUAL_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, true));
    assertNoMutants(HasIF_ICMPLT.class);
  }

  @Test
  public void shouldNotReplaceIF_ICMPLT_EQUAL_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.EQUAL, false));
    assertNoMutants(HasIF_ICMPLT.class);
  }

  @Test
  public void shouldReplaceIF_ICMPLT_ORDER_T() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, true));
    final Mutant mutant = getFirstMutant(HasIF_ICMPLT.class);
    final String expected = "was >= zero";
    assertMutantCallableReturns(new HasIF_ICMPLT(1), mutant, expected);
    assertMutantCallableReturns(new HasIF_ICMPLT(0), mutant, expected);
  }

  @Test
  public void shouldReplaceIF_ICMPLT_ORDER_F() throws Exception {
    createTesteeWith(new RemoveConditionalMutator(Choice.ORDER, false));
    final Mutant mutant = getFirstMutant(HasIF_ICMPLT.class);
    final String expected = "was < zero";
    assertMutantCallableReturns(new HasIF_ICMPLT(1), mutant, expected);
    assertMutantCallableReturns(new HasIF_ICMPLT(0), mutant, expected);
  }
}
