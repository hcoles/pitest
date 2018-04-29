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

import static org.junit.Assert.assertTrue;

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;
import org.pitest.mutationtest.engine.gregor.mutators.ConstructorCallMutatorTest.HasConstructorCall;
import org.pitest.mutationtest.engine.gregor.mutators.NonVoidMethodCallMutatorTest.HasIntMethodCall;

public class VoidMethodCallMutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToRemoveVoidMethods() {
    createTesteeWith(mutateOnlyCallMethod(),
        VoidMethodCallMutator.VOID_METHOD_CALL_MUTATOR);
  }

  static class HasVoidMethodCall implements Callable<String> {

    private int i = 0;

    public void set(final int i) {
      this.i = i;
    }

    @Override
    public String call() throws Exception {
      set(1);
      return "" + this.i;
    }

  }

  @Test
  public void shouldRemoveVoidMethodCalls() throws Exception {
    final Mutant mutant = getFirstMutant(HasVoidMethodCall.class);
    assertMutantCallableReturns(new HasVoidMethodCall(), mutant, "0");
  }

  @Test
  public void shouldNotRemoveConstructorCalls() throws Exception {
    assertTrue(findMutationsFor(HasConstructorCall.class).isEmpty());
  }

  @Test
  public void shouldNotRemoveNonVoidMethods() throws Exception {
    assertTrue(findMutationsFor(HasIntMethodCall.class).isEmpty());
  }

  private static class HasVoidMethodCallWithFinallyBlock implements
  Callable<String> {

    private int i = 0;

    public void set(final int i, final long k, final double l,
        final HasVoidMethodCallWithFinallyBlock m, final String n) {
      this.i = i;
    }

    @Override
    @SuppressWarnings("finally")
    public String call() throws Exception {
      final double a = 1;
      final String b = "foo";
      try {
        set(1, 2l, a, this, b);
      } finally {
        return "" + this.i;
      }
    }

  }

  @Test
  public void shouldMaintainStack() throws Exception {
    final Mutant mutant = getFirstMutant(HasVoidMethodCallWithFinallyBlock.class);
    assertMutantCallableReturns(new HasVoidMethodCallWithFinallyBlock(),
        mutant, "0");
  }

  private static class HasVoidStaticMethodCall implements Callable<String> {

    private static int i = 0;

    public static void set(final int newVal, final long k, final double l,
        final HasVoidStaticMethodCall m, final String n) {
      i = newVal;
    }

    @Override
    @SuppressWarnings("finally")
    public String call() throws Exception {
      final double a = 1;
      final String b = "foo";
      try {
        set(1, 2l, a, this, b);
      } finally {
        return "" + i;
      }
    }

  }

  @Test
  public void shouldMaintainStackWhenCallIsStatic() throws Exception {
    final Mutant mutant = getFirstMutant(HasVoidStaticMethodCall.class);
    assertMutantCallableReturns(new HasVoidStaticMethodCall(), mutant, "0");
  }

}
