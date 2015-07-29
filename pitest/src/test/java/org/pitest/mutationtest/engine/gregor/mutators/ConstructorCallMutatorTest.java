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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.pitest.functional.predicate.True;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;
import org.pitest.mutationtest.engine.gregor.mutators.NonVoidMethodCallMutatorTest.HasIntMethodCall;
import org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutatorTest.HasVoidMethodCall;

public class ConstructorCallMutatorTest extends MutatorTestBase {

  static class HasConstructorCall implements Callable<String> {
    @Override
    public String call() throws Exception {
      final Integer i = new Integer(12);
      return "" + (i == null);
    }
  }

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

  @Test
  public void shouldNotRemoveCallsToSuper() throws Exception {
    createTesteeWith(True.<MethodInfo> all(),
        ConstructorCallMutator.CONSTRUCTOR_CALL_MUTATOR);
    assertFalse(findMutationsFor(HasConstructorCall.class).contains(
        descriptionContaining("java/lang/Object::<init>")));
  }

  private static class HasDelegateConstructorCall implements Callable<String> {

    private final int i;

    @SuppressWarnings("unused")
    HasDelegateConstructorCall() {
      this(1);
    }

    HasDelegateConstructorCall(final int i) {
      this.i = i;
    }

    @Override
    public String call() throws Exception {
      return "" + this.i;
    }

  }

  @Test
  public void shouldNotRemoveCallsToDelegateContructor() throws Exception {
    createTesteeWith(True.<MethodInfo> all(),
        ConstructorCallMutator.CONSTRUCTOR_CALL_MUTATOR);
    assertFalse(findMutationsFor(HasDelegateConstructorCall.class).contains(
        descriptionContaining("HasDelegateConstructorCall::<init>")));
  }

  private static class HasArrayListConstructor implements Callable<String> {

    private List<String> list;

    @Override
    public String call() throws Exception {

      this.list = new ArrayList<String>();

      return "" + this.list;
    }
  }

  @Test
  public void shouldCreateViableClassWhenMutatingArrayListConstructor()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasArrayListConstructor.class);
    assertMutantCallableReturns(new HasArrayListConstructor(), mutant, "null");
  }

}
