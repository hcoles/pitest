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

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.pitest.functional.FunctionalList;
import org.pitest.mutationtest.MutationDetails;
import org.pitest.mutationtest.engine.Mutant;
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
    
    public Object call() throws Exception {
      member = TEST_OBJECT;
      return member;
    }

  }
  
  @Test
  public void shouldRemoveAssignmentToMemberVariable() throws Exception {
    final Mutant mutant = getFirstMutant(HasMemberVariable.class);
    assertMutantCallableReturns(new HasMemberVariable(), mutant, null);
  }

  static class HasFinalMemberVariable implements Callable<Integer> {

    private final Integer member2 = 5;
    
    public Integer call() throws Exception {
      return member2;
    }
  }
  
  @Test
  public void shouldRemoveAssignmentToFinalMemberVariable() throws Exception {
    final Mutant mutant = getFirstMutant(HasFinalMemberVariable.class);
    assertMutantCallableReturns(new MutantStarter<Integer>(
        HasFinalMemberVariable.class), mutant, null);
  }

  static class HasFinalPrimitiveMemberVariable implements Callable<Integer> {

    private final int member2;
    
    public HasFinalPrimitiveMemberVariable(int i) {
      this.member2 = i;
    }
    
    public Integer call() throws Exception {
      return member2;
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
    public Integer call() throws Exception {
      return x;
    }
  }

  @Test
  public void shouldNotCreateMutationForNonInitializedVariable()
      throws Exception {
    final FunctionalList<MutationDetails> mutations = findMutationsFor(NoInit.class);
    assertTrue("Expected no mutant created/available.", mutations.isEmpty());
  }

  static class HasConstantFinalPrimitiveMemberVariable implements Callable<String> {

    public final int member2 = 42;
    
    public String call() throws Exception {
      Class<?> c = getClass();
      Integer i = (Integer)c.getField("member2").get(this);
      return "" + member2 + "-" + i; // will be optimized by compiler to "42-" + i;
    }
  }
  
  @Test
  public void isUnableToCreateConsistentMutationForConstantFinalPrimitiveMember()
      throws Exception {
    // Attention: Mutant is created but return value ist still 42!!!
    // Property member2 is mutated (as shown by the reflection stuff, but
    // constant will be inlined by the compiler!
    final Mutant mutant = getFirstMutant(HasConstantFinalPrimitiveMemberVariable.class);
    assertMutantCallableReturns(new MutantStarter<String>(
        HasConstantFinalPrimitiveMemberVariable.class), mutant, "42-0");
  }
  
}
