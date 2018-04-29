package org.pitest.mutationtest.engine.gregor.mutators;

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;

public class BooleanTrueReturnValsMutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToMutateOnlyReturnVals() {
    createTesteeWith(BooleanTrueReturnValsMutator.BOOLEAN_TRUE_RETURN);
  }

  @Test
  public void mutatesReturnFalseToReturnTrue() throws Exception {
    assertMutantCallableReturns(new BooleanReturn(),
        createFirstMutant(BooleanReturn.class), "true");
  }

  @Test
  public void describesMutationsToPrimitiveBooleans() {
    assertMutantDescriptionIncludes("replaced boolean return with true", BooleanReturn.class);
    assertMutantDescriptionIncludes("BooleanReturn::mutable", BooleanReturn.class);
  }

  @Test
  public void doesNotMutatePrimitiveIntReturns() throws Exception {
    this.assertNoMutants(IntegerReturn.class);
  }

  @Test
  public void mutatesBoxedFalseToTrue() throws Exception {
    assertMutantCallableReturns(new BoxedFalse(),
        createFirstMutant(BoxedFalse.class), true);
  }

  @Test
  public void describesMutationsToBoxedBooleans() {
    assertMutantDescriptionIncludes("replaced Boolean return with True", BoxedFalse.class);
    assertMutantDescriptionIncludes("BoxedFalse::call", BoxedFalse.class);
  }

  @Test
  public void doesNotMutateBoxedIntegerReturns() throws Exception {
    this.assertNoMutants(BoxedInteger.class);
  }

  private static class BooleanReturn implements Callable<String> {
    public boolean mutable() {
      return false;
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


  private static class BoxedFalse implements Callable<Boolean> {
    @Override
    public Boolean call() {
      return false;
    }
  }

  private static class BoxedInteger implements Callable<Integer> {
    @Override
    public Integer call() {
      return 42;
    }
  }


}
