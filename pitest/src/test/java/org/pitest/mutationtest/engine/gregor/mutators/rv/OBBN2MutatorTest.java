package org.pitest.mutationtest.engine.gregor.mutators.rv;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;
import org.pitest.mutationtest.engine.gregor.mutators.rv.OBBN2Mutator;

import java.util.concurrent.Callable;

public class OBBN2MutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToMutateOnlyMathFunctions() {
    createTesteeWith(OBBN2Mutator.OBBN_2_MUTATOR);
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
  public void shouldReplaceIntegerBitwiseOrsWithFirstMember() throws Exception {
    final Mutant mutant = getFirstMutant(HasIOr.class);
    assertMutantCallableReturns(new HasIOr(1), mutant, "1");
    assertMutantCallableReturns(new HasIOr(4), mutant, "4");
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
  public void shouldReplaceIntegerBitwiseAndsWithFirstMember() throws Exception {
    final Mutant mutant = getFirstMutant(HasIAnd.class);
    assertMutantCallableReturns(new HasIAnd(1), mutant, "1");
    assertMutantCallableReturns(new HasIAnd(4), mutant, "4");
  }


  // LONGS

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
  public void shouldReplaceLongBitwiseOrsWithFirstMember() throws Exception {
    final Mutant mutant = getFirstMutant(HasLOr.class);
    assertMutantCallableReturns(new HasLOr(1), mutant, "1");
    assertMutantCallableReturns(new HasLOr(4), mutant, "4");
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
  public void shouldReplaceLongBitwiseAndsWithFirstMember() throws Exception {
    final Mutant mutant = getFirstMutant(HasLAnd.class);
    assertMutantCallableReturns(new HasLAnd(1), mutant, "1");
    assertMutantCallableReturns(new HasLAnd(4), mutant, "4");
  }
}
