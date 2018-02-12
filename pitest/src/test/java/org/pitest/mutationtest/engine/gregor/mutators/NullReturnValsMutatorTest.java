package org.pitest.mutationtest.engine.gregor.mutators;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;

public class NullReturnValsMutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToMutateOnlyReturnVals() {
    createTesteeWith(NullReturnValsMutator.NULL_RETURN_VALUES);
  }

  @Test
  public void mutatesObjectReturnValuesToNull() throws Exception {
    assertMutantCallableReturns(new ObjectReturn(),
        createFirstMutant(ObjectReturn.class), null);
  }

  @Test
  public void doesNotMutateMethodsAnnotatedWithNotNull() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(
        AnnotatedObjectReturn.class);
    assertThat(actual).isEmpty();
  }

  @Test
  public void mutatesMethodsWithOtherAnnoations() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(
        HasOtherAnnotation.class);
    assertThat(actual).hasSize(1);
  }


  @Test
  public void doesNotMutateMethodsAnnotatedWithNotNullAndOthers() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(
        MultipleAnnotatedObjectReturn.class);
    assertThat(actual).isEmpty();
  }


  @Test
  public void describesMutationsToObject() {
    assertMutantDescriptionIncludes("replaced return value with null", ObjectReturn.class);
    assertMutantDescriptionIncludes("ObjectReturn::call", ObjectReturn.class);
  }

  private static class ObjectReturn implements Callable<Object> {
    @Override
    public Object call() throws Exception {
      return "";
    }
  }

  private static class AnnotatedObjectReturn implements Callable<Object> {
    @Override
    @NotNull
    public Object call() throws Exception {
      return "";
    }
  }

  private static class HasOtherAnnotation implements Callable<Object> {
    @Override
    @SomethingElse
    public Object call() throws Exception {
      return "";
    }
  }

  private static class MultipleAnnotatedObjectReturn implements Callable<Object> {
    @Override
    @NotNull
    @SomethingElse
    public Object call() throws Exception {
      return "";
    }
  }
}

@interface SomethingElse {

}

@interface NotNull {

}
