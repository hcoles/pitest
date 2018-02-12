/*
 * Copyright 2011 Henry Coles and Stefan Penndorf
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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pitest.mutationtest.engine.gregor.mutators.experimental;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;

/**
 *
 *
 * @author Stefan Penndorf <stefan.penndorf@gmail.com>
 */
public class MemberVariableMutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToMutateOnlyMemberVariables() {
    createTesteeWith(new MemberVariableMutator());
  }

  @Test
  public void shouldProvideAMeaningfulName() {
    assertEquals("EXPERIMENTAL_MEMBER_VARIABLE_MUTATOR",
        new MemberVariableMutator().getName());
  }

  private static final Object TEST_OBJECT = new Object();

  private static class HasMemberVariable implements Callable<Object> {

    private Object member;

    @Override
    public Object call() throws Exception {
      this.member = TEST_OBJECT;
      return this.member;
    }

  }

  @Test
  public void shouldRemoveAssignmentToMemberVariable() throws Exception {
    final Mutant mutant = getFirstMutant(HasMemberVariable.class);
    assertMutantCallableReturns(new HasMemberVariable(), mutant, null);
  }

  static class HasFinalMemberVariable implements Callable<Integer> {

    private final Integer member2 = 5;

    @Override
    public Integer call() throws Exception {
      return this.member2;
    }
  }

  @Test
  public void shouldRemoveAssignmentToFinalMemberVariable() throws Exception {
    final Mutant mutant = getFirstMutant(HasFinalMemberVariable.class);
    assertMutantCallableReturns(new MutantStarter<>(
        HasFinalMemberVariable.class), mutant, null);
  }

  static class HasFinalPrimitiveMemberVariable implements Callable<Integer> {

    private final int member2;

    public HasFinalPrimitiveMemberVariable(final int i) {
      this.member2 = i;
    }

    @Override
    public Integer call() throws Exception {
      return this.member2;
    }
  }

  static class HasFinalPrimitiveMemberVariableStarter extends
  MutantStarter<Integer> {
    @Override
    protected Callable<Integer> constructMutee() throws Exception {
      return new HasFinalPrimitiveMemberVariable(5);
    }
  }

  @Test
  public void shouldRemoveAssignmentToFinalPrimitiveMemberVariable()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasFinalPrimitiveMemberVariable.class);
    assertMutantCallableReturns(new HasFinalPrimitiveMemberVariableStarter(),
        mutant, 0);
  }

  static class NoInit implements Callable<Integer> {
    private int x;

    @Override
    public Integer call() throws Exception {
      return this.x;
    }
  }

  @Test
  public void shouldNotCreateMutationForNonInitializedVariable()
      throws Exception {
    final List<MutationDetails> mutations = findMutationsFor(NoInit.class);
    assertTrue("Expected no mutant created/available.", mutations.isEmpty());
  }

  static class HasConstantFinalPrimitiveMemberVariable implements
  Callable<String> {

    public final int member2 = 42;

    @Override
    public String call() throws Exception {
      final Class<?> c = getClass();
      final Integer i = (Integer) c.getField("member2").get(this);
      return "" + this.member2 + "-" + i; // will be optimized by compiler to
      // "42-" + i;
    }
  }

  @Test
  public void isUnableToCreateConsistentMutationForConstantFinalPrimitiveMember()
      throws Exception {
    // Attention: Mutant is created but return value ist still 42!!!
    // Property member2 is mutated (as shown by the reflection stuff, but
    // constant will be inlined by the compiler!
    final Mutant mutant = getFirstMutant(HasConstantFinalPrimitiveMemberVariable.class);
    assertMutantCallableReturns(new MutantStarter<>(
        HasConstantFinalPrimitiveMemberVariable.class), mutant, "42-0");
  }

  static class AssignmentAfterSomeValuesOnStack_SingleWord implements
  Callable<String> {

    private int member = 1;

    @Override
    public String call() throws Exception {
      // bipush 100
      // aload_0
      // iconst_2
      // dup_x1
      // -> stack: 100, 2, this, 2
      // putfield
      // -> stack: 100, 2
      // iadd
      // istore_1
      final int i = 100 + (this.member = 2);
      return "" + this.member + " " + i;
    }
  }

  @Test
  public void consumesFromStackTheSameValuesAsPutfieldWouldConsume_SingleWord()
      throws Exception {
    createTesteeWith(mutateOnlyCallMethod(), new MemberVariableMutator());
    final Mutant mutant = getFirstMutant(AssignmentAfterSomeValuesOnStack_SingleWord.class);
    assertMutantCallableReturns(
        new AssignmentAfterSomeValuesOnStack_SingleWord(), mutant, "1 102");
  }

  static class AssignmentAfterSomeValuesOnStack_TwoWord implements
  Callable<String> {

    private long member = 1;

    @Override
    public String call() throws Exception {
      // ldc2_w 100
      // aload_0
      // ldc2_w 2
      // dup2_x1
      // -> stack: 0 100, 0 2, this, 0 2
      // putfield
      // -> stack: 0 100, 0 2
      // ladd
      // lstore_1
      final long i = 100 + (this.member = 2);
      return "" + this.member + " " + i;
    }
  }

  @Test
  public void consumesFromStackTheSameValuesAsPutfieldWouldConsume_TwoWord()
      throws Exception {
    createTesteeWith(mutateOnlyCallMethod(), new MemberVariableMutator());
    final Mutant mutant = getFirstMutant(AssignmentAfterSomeValuesOnStack_TwoWord.class);
    assertMutantCallableReturns(new AssignmentAfterSomeValuesOnStack_TwoWord(),
        mutant, "1 102");
  }
}
