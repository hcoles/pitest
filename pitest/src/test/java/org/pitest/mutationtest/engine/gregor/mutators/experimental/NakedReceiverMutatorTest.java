/*
 * Copyright 2015 Urs Metz
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

package org.pitest.mutationtest.engine.gregor.mutators.experimental;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.pitest.mutationtest.engine.gregor.mutators.experimental.NakedReceiverMutator.NAKED_RECEIVER;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.pitest.functional.FunctionalList;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;

public class NakedReceiverMutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToUseReplaceMethodWithArgumentOfSameTypeAsReturnValueMutator() {
    createTesteeWith(mutateOnlyCallMethod(), NAKED_RECEIVER);
  }

  @Test
  public void shouldReplaceMethodCallOnString() throws Exception {
    final Mutant mutant = getFirstMutant(HasStringMethodCall.class);
    assertMutantCallableReturns(new HasStringMethodCall("EXAMPLE"), mutant,
        "EXAMPLE");
  }

  @Test
  public void shouldNotReplaceMethodCallWhenDifferentReturnType()
      throws Exception {
    assertNoMutants(HasMethodWithDifferentReturnType.class);
  }

  @Test
  public void shouldNotMutateVoidMethodCall() throws Exception {
    assertNoMutants(HasVoidMethodCall.class);
  }
  
  @Test
  public void shouldNotMutateStaticMethodCall() {
    assertNoMutants(HasStaticMethodCallWithSameType.class);
  }

  @Test
  public void willReplaceCallToMethodWithDifferentGenericTypeDueToTypeErasure()
      throws Exception {
    Mutant mutant = getFirstMutant(CallsMethodsWithGenericTypes.class);
    Foo<String> receiver = new Foo<String>("3");
    assertMutantCallableReturns(new CallsMethodsWithGenericTypes(receiver),
        mutant, receiver);
  }

  @Test
  public void shouldRemoveDslMethods() throws Exception {
    FunctionalList<MutationDetails> mutations = findMutationsFor(
        HasDslMethodCall.class);
    // stringbuilder matches pattern for a dsl
    assertThat(mutations).hasSize(3);

    final Mutant mutant = getFirstMutant(HasDslMethodCall.class);
    assertMutantCallableReturns(new HasDslMethodCall(), mutant, "HasDslMethodCall [i=3]");
  }

  private static class HasStringMethodCall implements Callable<String> {
    private String arg;

    public HasStringMethodCall(String arg) {
      this.arg = arg;
    }

    public String call() throws Exception {
      return arg.toLowerCase();
    }
  }

  static class HasMethodWithDifferentReturnType {
    public int call() throws Exception {
      return "".length();
    }
  }

  static class HasVoidMethodCall {
    public void call() throws Exception {
    }
  }

  private static class CallsMethodsWithGenericTypes
      implements Callable<Foo<?>> {
    private Foo<String> myArg;

    public CallsMethodsWithGenericTypes(Foo<String> myArg) {
      this.myArg = myArg;
    }

    public Foo<?> call() {
      return myArg.returnsFooInteger();
    }
  }

  static class HasStaticMethodCallWithSameType {
    public void call() {
      HasStaticMethodCallWithSameType.instance();
    }

    private static HasStaticMethodCallWithSameType instance() {
      return new HasStaticMethodCallWithSameType();
    }
  }
  
  static class Foo<T> extends ArrayList<T> {

    public Foo(T arg) {
      super(singletonList(arg));
    }

    public Foo<Integer> returnsFooInteger() {
      return new Foo<Integer>(2);
    }
  }

  static class HasDslMethodCall implements Callable<String> {

    private int i = 0;

    public HasDslMethodCall chain(final int newVal) {
      this.i += newVal;
      return this;
    }

    public void voidNonDsl(final int newVal) {
      this.i += newVal;
    }

    public int nonDsl(final int newVal) {
      this.i += newVal;
      return i;
    }

    public String call() throws Exception {
      HasDslMethodCall dsl = this;
      dsl.chain(1).nonDsl(3);
      return "" + dsl;
    }

    @Override
    public String toString() {
      return "HasDslMethodCall [i=" + i + "]";
    }

  }
}
