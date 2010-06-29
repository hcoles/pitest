package org.pitest.mutationtest;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.reeltwo.jumble.mutation.Mutater;

public class MutationTest {

  @Mock
  private Mutater mutator;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testReturnValsFunctionCallsCorrectMethod() {
    callConfigFunction(Mutation.RETURN_VALS);
    verify(this.mutator).setMutateReturnValues(true);
    verify(this.mutator).setMutateReturnValues(false);
  }

  @Test
  public void testCPoolFunctionCallsCorrectMethod() {
    callConfigFunction(Mutation.CPOOL);
    verify(this.mutator).setMutateCPool(true);
    verify(this.mutator).setMutateCPool(false);
  }

  @Test
  public void testInlineConstsFunctionCallsCorrectMethod() {
    callConfigFunction(Mutation.INLINE_CONSTS);
    verify(this.mutator).setMutateInlineConstants(true);
    verify(this.mutator).setMutateInlineConstants(false);
  }

  @Test
  public void testStoresFunctionCallsCorrectMethod() {
    callConfigFunction(Mutation.STORES);
    verify(this.mutator).setMutateStores(true);
    verify(this.mutator).setMutateStores(false);
  }

  @Test
  public void testIncrementsFunctionCallsCorrectMethod() {
    callConfigFunction(Mutation.INCREMENTS);
    verify(this.mutator).setMutateIncrements(true);
    verify(this.mutator).setMutateIncrements(false);
  }

  @Test
  public void testSwitchesFunctionCallsCorrectMethod() {
    callConfigFunction(Mutation.SWITCHES);
    verify(this.mutator).setMutateSwitch(true);
    verify(this.mutator).setMutateSwitch(false);
  }

  @Test
  public void testAllFunctionCallsAllMethods() {
    callConfigFunction(Mutation.ALL);
    verify(this.mutator).setMutateSwitch(true);
    verify(this.mutator).setMutateSwitch(false);
    verify(this.mutator).setMutateIncrements(true);
    verify(this.mutator).setMutateIncrements(false);
    verify(this.mutator).setMutateStores(true);
    verify(this.mutator).setMutateStores(false);
    verify(this.mutator).setMutateInlineConstants(true);
    verify(this.mutator).setMutateInlineConstants(false);
    verify(this.mutator).setMutateCPool(true);
    verify(this.mutator).setMutateCPool(false);
    verify(this.mutator).setMutateReturnValues(true);
    verify(this.mutator).setMutateReturnValues(false);
  }

  private void callConfigFunction(final Mutation mutation) {
    mutation.apply(this.mutator, true);
    mutation.apply(this.mutator, false);
  }

}
