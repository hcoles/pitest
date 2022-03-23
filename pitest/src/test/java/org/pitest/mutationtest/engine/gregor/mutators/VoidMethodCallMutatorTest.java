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

import org.junit.Test;
import org.pitest.mutationtest.engine.gregor.mutators.ConstructorCallMutatorTest.HasConstructorCall;
import org.pitest.mutationtest.engine.gregor.mutators.NonVoidMethodCallMutatorTest.HasIntMethodCall;
import org.pitest.verifier.mutants.MutatorVerifierStart;

import java.util.concurrent.Callable;

import static org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator.VOID_METHOD_CALLS;

public class VoidMethodCallMutatorTest {

  MutatorVerifierStart v = MutatorVerifierStart.forMutator(VOID_METHOD_CALLS);

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
  public void shouldRemoveVoidMethodCalls() {
    v.forCallableClass(HasVoidMethodCall.class)
            .firstMutantShouldReturn("0");
  }

  @Test
  public void shouldNotRemoveConstructorCalls() {
    v.forClass(HasConstructorCall.class)
                    .noMutantsCreated();
  }

  @Test
  public void shouldNotRemoveNonVoidMethods() {
    v.forClass(HasIntMethodCall.class)
            .noMutantsCreated();
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
        set(1, 2L, a, this, b);
      } finally {
        return "" + this.i;
      }
    }

  }

  @Test
  public void shouldMaintainStack() {
    v.forCallableClass(HasVoidMethodCallWithFinallyBlock.class)
            .firstMutantShouldReturn("0");
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
        set(1, 2L, a, this, b);
      } finally {
        return "" + i;
      }
    }

  }

  @Test
  public void shouldMaintainStackWhenCallIsStatic() throws Exception {
    v.forCallableClass(HasVoidStaticMethodCall.class)
            .firstMutantShouldReturn("0");
  }

}
