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

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;
import org.pitest.mutationtest.engine.gregor.mutators.MethodCallMutatorTest.HasConstructorCall;
import org.pitest.mutationtest.engine.gregor.mutators.MethodCallMutatorTest.HasIntMethodCall;
import org.pitest.mutationtest.engine.gregor.mutators.MethodCallMutatorTest.HasVoidMethodCall;

public class ConstructorCallMutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToRemoveVoidMethods() {
    createTesteeWith(mutateOnlyCallMethod(),
        ConstructorCallMutator.CONSTRUCTOR_CALL_MUTATOR);
  }

  @Test
  public void shouldReplaceConstructorCallsWithNullValue() throws Exception {
    final Mutant mutant = getFirstMutant(HasConstructorCall.class);
    assertMutantCallableReturns(new HasConstructorCall(), mutant, "true");
  }

  @Test
  public void shouldNotRemoveVoidMethodCalls() throws Exception {
    assertFalse(findMutationsFor(HasVoidMethodCall.class).contains(
        descriptionContaining("set")));
  }

  @Test
  public void shouldNotRemoveNonVoidMethods() throws Exception {
    assertFalse(findMutationsFor(HasIntMethodCall.class).contains(
        descriptionContaining("set")));
  }
}
