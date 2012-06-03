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

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;

import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;

public class SwitchMutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToMutateOnlySwitchStatements() {
    createTesteeWith(new SwitchMutator());
  }

  @Test
  public void shouldProvideAMeaningfulName() {
    assertEquals("EXPERIMENTAL_SWITCH_MUTATOR",
      new SwitchMutator().getName());
  }

  private static class HasIntSwitchWithDefault implements Callable<Integer> {

    private int value;

      private HasIntSwitchWithDefault(int value) {
          this.value = value;
      }
      public Integer call() throws Exception {
      switch (value) {
          case 0:
              return 1;
          default:
              return 0;
      }
    }
  }

  @Test
  public void shouldSwapFirstCaseWithDefaultForInt() throws Exception {
    final Mutant mutant = getFirstMutant(HasIntSwitchWithDefault.class);
      assertMutantCallableReturns(new HasIntSwitchWithDefault(0), mutant, 0);
      assertMutantCallableReturns(new HasIntSwitchWithDefault(1), mutant, 1);
  }

  private static class HasCharSwitchWithDefault implements Callable<Character> {

    private char value;

    private HasCharSwitchWithDefault(char value) {
      this.value = value;
    }

    public Character call() throws Exception {
      switch (value) {
        case 'a':
          return 'z';
        default:
          return 'a';
      }
    }
  }

  @Test
  public void shouldSwapFirstCaseWithDefaultForChar() throws Exception {
    final Mutant mutant = getFirstMutant(HasCharSwitchWithDefault.class);
    assertMutantCallableReturns(new HasCharSwitchWithDefault('a'), mutant, 'a');
    assertMutantCallableReturns(new HasCharSwitchWithDefault('z'), mutant, 'z');
  }

  private enum SwitchEnum {
      FIRST,
      SECOND
  }

  private static class HasEnumSwitchWithDefault implements Callable<Integer> {

    private SwitchEnum value;

    private HasEnumSwitchWithDefault(SwitchEnum value) {
      this.value = value;
    }

    public Integer call() throws Exception {
      switch (value) {
        case FIRST:
          return 2;
        default:
          return 1;
      }
    }
  }

  @Test
  public void shouldSwapFirstCaseWithDefaultForEnum() throws Exception {
    final Mutant mutant = getFirstMutant(HasEnumSwitchWithDefault.class);
    assertMutantCallableReturns(new HasEnumSwitchWithDefault(SwitchEnum.FIRST), mutant, 1);
    assertMutantCallableReturns(new HasEnumSwitchWithDefault(SwitchEnum.SECOND), mutant, 2);
  }

  private static class HasMultipleArmIntSwitchWithDefault implements Callable<Integer> {

    private int value;

    private HasMultipleArmIntSwitchWithDefault(int value) {
      this.value = value;
    }

    public Integer call() throws Exception {
      switch (value) {
        case 0:
          return 1;
        case 2:
          return 2;
        case 4:
          return 3;
        default:
          return 0;
        }
    }
  }

  @Test
  public void shouldReplaceOtherCasesWithDefaultForInt() throws Exception {
    final Mutant mutant = getFirstMutant(HasMultipleArmIntSwitchWithDefault.class);
    assertMutantCallableReturns(new HasMultipleArmIntSwitchWithDefault(-8), mutant, 1);
    assertMutantCallableReturns(new HasMultipleArmIntSwitchWithDefault(0), mutant, 0);
    assertMutantCallableReturns(new HasMultipleArmIntSwitchWithDefault(1), mutant, 1);
    assertMutantCallableReturns(new HasMultipleArmIntSwitchWithDefault(2), mutant, 0);
    assertMutantCallableReturns(new HasMultipleArmIntSwitchWithDefault(3), mutant, 1);
    assertMutantCallableReturns(new HasMultipleArmIntSwitchWithDefault(4), mutant, 0);
    assertMutantCallableReturns(new HasMultipleArmIntSwitchWithDefault(8), mutant, 1);
  }

  private static class HasMultipleArmIntSwitchWithoutDefault implements Callable<Integer> {

    private int value;

    private HasMultipleArmIntSwitchWithoutDefault(int value) {
      this.value = value;
    }

    public Integer call() throws Exception {
      switch (value) {
        case 0:
          return 1;
        case 200:
          return 2;
        case 40000:
          return 3;
      }
      return 0;
    }
  }

  @Test
  public void shouldReplaceOtherCasesWithoutDefaultForInt() throws Exception {
    final Mutant mutant = getFirstMutant(HasMultipleArmIntSwitchWithoutDefault.class);
    assertMutantCallableReturns(new HasMultipleArmIntSwitchWithoutDefault(-1), mutant, 1);
    assertMutantCallableReturns(new HasMultipleArmIntSwitchWithoutDefault(0), mutant, 0);
    assertMutantCallableReturns(new HasMultipleArmIntSwitchWithoutDefault(8), mutant, 1);
    assertMutantCallableReturns(new HasMultipleArmIntSwitchWithoutDefault(200), mutant, 0);
    assertMutantCallableReturns(new HasMultipleArmIntSwitchWithoutDefault(400), mutant, 1);
    assertMutantCallableReturns(new HasMultipleArmIntSwitchWithoutDefault(40000), mutant, 0);
    assertMutantCallableReturns(new HasMultipleArmIntSwitchWithoutDefault(45000), mutant, 1);
  }
}