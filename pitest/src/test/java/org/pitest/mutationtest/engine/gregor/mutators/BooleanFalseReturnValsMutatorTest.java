package org.pitest.mutationtest.engine.gregor.mutators;

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;

public class BooleanFalseReturnValsMutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToMutateOnlyReturnVals() {
    createTesteeWith(BooleanFalseReturnValsMutator.BOOLEAN_FALSE_RETURN);
  }

  @Test
  public void mutatesReturnTrueToReturnFalse() throws Exception {
    assertMutantCallableReturns(new BooleanReturn(),
        createFirstMutant(BooleanReturn.class), "false");
  }

  @Test
  public void describesMutationsToPrimitiveBooleans() {
    assertMutantDescriptionIncludes("replaced boolean return with false", BooleanReturn.class);
    assertMutantDescriptionIncludes("BooleanReturn::mutable", BooleanReturn.class);
  }

  @Test
  public void doesNotMutatePrimitiveIntReturns() throws Exception {
    this.assertNoMutants(IntegerReturn.class);
  }

  @Test
  public void mutatesBoxedTrueToFalse() throws Exception {
    assertMutantCallableReturns(new BoxedTrue(),
        createFirstMutant(BoxedTrue.class), false);
  }

  @Test
  public void describesMutationsToBoxedBooleans() {
    assertMutantDescriptionIncludes("replaced Boolean return with False", BoxedTrue.class);
    assertMutantDescriptionIncludes("BoxedTrue::call", BoxedTrue.class);
  }

  @Test
  public void doesNotMutateBoxedIntegerReturns() throws Exception {
    this.assertNoMutants(BoxedInteger.class);
  }

  private static class BooleanReturn implements Callable<String> {
    public boolean mutable() {
      return true;
    }

    @Override
    public String call() throws Exception {
      return "" + mutable();
    }
  }

  private static class IntegerReturn implements Callable<String> {
    public int mutable() {
      return 42;
    }

    @Override
    public String call() throws Exception {
      return "" + mutable();
    }
  }


  private static class BoxedTrue implements Callable<Boolean> {
    @Override
    public Boolean call() {
      return Boolean.TRUE;
    }
  }

  private static class BoxedInteger implements Callable<Integer> {
    @Override
    public Integer call() {
      return 42;
    }
  }


}
