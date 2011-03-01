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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.pitest.functional.FunctionalList;
import org.pitest.mutationtest.MutationDetails;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;
import org.pitest.mutationtest.engine.gregor.mutators.MethodCallMutatorTest.HasConstructorCall;
import org.pitest.mutationtest.engine.gregor.mutators.MethodCallMutatorTest.HasIntMethodCall;

public class NonVoidMethodCallMutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToRemoveVoidMethods() {
    createTesteeWith(mutateOnlyCallMethod(),
        NonVoidMethodCallMutator.NON_VOID_METHOD_CALL_MUTATOR);
  }

  @Test
  public void shouldRemoveNonVoidMethods() throws Exception {
    final Mutant mutant = getFirstMutant(HasIntMethodCall.class);
    assertMutantCallableReturns(new HasIntMethodCall(), mutant, "0");
  }

  private static class HasVoidMethodCall implements Callable<String> {

    private String s = "";

    public void set(final int i) {
      this.s = this.s + i;
    }

    public String call() throws Exception {
      set(1);
      return this.s;
    }

  }

  @Test
  public void shouldNotRemoveVoidMethodCalls() throws Exception {
    assertTrue(findMutationsFor(HasVoidMethodCall.class).isEmpty());
  }

  @Test
  public void shouldNotRemoveConstructorCalls() throws Exception {
    final FunctionalList<MutationDetails> actual = findMutationsFor(HasConstructorCall.class);
    assertFalse(actual.contains(descriptionContaining("Integer")));
  }

}
