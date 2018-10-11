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

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;

public class RemoveSwitchMutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToMutateOnlySwitchStatements() {
    createTesteeWith(new RemoveSwitchMutator(2));
  }

  @Test
  public void shouldProvideAMeaningfulName() {
    assertEquals("EXPERIMENTAL_REMOVE_SWITCH_MUTATOR_[0-99]",
        new RemoveSwitchMutator(2).getName());
  }

  @Test
  public void shouldProvideAMeaningfulAggregatedName() {
    Iterable<MethodMutatorFactory> mutators = RemoveSwitchMutator.makeMutators();
    for (MethodMutatorFactory mutator : mutators) {
      assertEquals("EXPERIMENTAL_REMOVE_SWITCH_MUTATOR_[0-99]",
              mutator.getName());
    }
  }

  private static class HasIntSwitchWithDefault implements Callable<Integer> {

    private final int value;

    private HasIntSwitchWithDefault(final int value) {
      this.value = value;
    }

    @Override
    public Integer call() throws Exception {
      switch (this.value) {
      case 0:
        return 0;
      case 1:
        return 1;
      case 2:
        return 2;
      default:
        return -1;
      }
    }
  }

  @Test
  public void shouldChangeLabelInt() throws Exception {
    final Mutant mutant = getFirstMutant(HasIntSwitchWithDefault.class);
    assertMutantCallableReturns(new HasIntSwitchWithDefault(0), mutant, 0);
    assertMutantCallableReturns(new HasIntSwitchWithDefault(1), mutant, 1);
    assertMutantCallableReturns(new HasIntSwitchWithDefault(2), mutant, -1);
    assertMutantCallableReturns(new HasIntSwitchWithDefault(3), mutant, -1);
  }

  private static class HasCharSwitchWithDefault implements Callable<Character> {

    private final char value;

    private HasCharSwitchWithDefault(final char value) {
      this.value = value;
    }

    @Override
    public Character call() throws Exception {
      switch (this.value) {
      case 'a':
        return 'a';
      case 'b':
        return 'b';
      case 'c':
        return 'c';
      default:
        return 'z';
      }
    }
  }

  @Test
  public void shouldChangeLabelChar() throws Exception {
    final Mutant mutant = getFirstMutant(HasCharSwitchWithDefault.class);
    assertMutantCallableReturns(new HasCharSwitchWithDefault('a'), mutant, 'a');
    assertMutantCallableReturns(new HasCharSwitchWithDefault('b'), mutant, 'b');
    assertMutantCallableReturns(new HasCharSwitchWithDefault('c'), mutant, 'z');
    assertMutantCallableReturns(new HasCharSwitchWithDefault('z'), mutant, 'z');
  }

  private enum SwitchEnum {
    FIRST, SECOND, THIRD, DEFAULT
  }

  private static class HasEnumSwitchWithDefault implements Callable<Integer> {

    private final SwitchEnum value;

    private HasEnumSwitchWithDefault(final SwitchEnum value) {
      this.value = value;
    }

    @Override
    public Integer call() throws Exception {
      switch (this.value) {
      case FIRST:
        return 1;
      case SECOND:
        return 2;
      case THIRD:
        return 3;
      default:
        return -1;
      }
    }
  }

  @Test
  public void shouldChangeLabelEnum() throws Exception {
    final Mutant mutant = getFirstMutant(HasEnumSwitchWithDefault.class);
    assertMutantCallableReturns(new HasEnumSwitchWithDefault(SwitchEnum.FIRST),
        mutant, 1);
    assertMutantCallableReturns(
        new HasEnumSwitchWithDefault(SwitchEnum.SECOND), mutant, 2);
    assertMutantCallableReturns(new HasEnumSwitchWithDefault(SwitchEnum.THIRD),
        mutant, -1);
    assertMutantCallableReturns(
        new HasEnumSwitchWithDefault(SwitchEnum.DEFAULT), mutant, -1);
  }

  private static class HasMultipleArmIntSwitchWithDefault implements
  Callable<Integer> {

    private final int value;

    private HasMultipleArmIntSwitchWithDefault(final int value) {
      this.value = value;
    }

    @Override
    public Integer call() throws Exception {
      switch (this.value) {
      case 1:
        return 1;
      case 200:
        return 2;
      case 4000:
        return 4;
      case 800000:
        return 8;
      default:
        return 0;
      }
    }
  }

  @Test
  public void shouldReplaceTableLabelsInt() throws Exception {
    final Mutant mutant = getFirstMutant(HasMultipleArmIntSwitchWithDefault.class);
    assertMutantCallableReturns(new HasMultipleArmIntSwitchWithDefault(0),
        mutant, 0);
    assertMutantCallableReturns(new HasMultipleArmIntSwitchWithDefault(1),
        mutant, 1);
    assertMutantCallableReturns(new HasMultipleArmIntSwitchWithDefault(200),
        mutant, 2);
    assertMutantCallableReturns(new HasMultipleArmIntSwitchWithDefault(4000),
        mutant, 0);
    assertMutantCallableReturns(new HasMultipleArmIntSwitchWithDefault(800000),
        mutant, 8);
  }

  private static class HasMultipleArmIntSwitchWithoutDefault implements
  Callable<Integer> {

    private final int value;

    private HasMultipleArmIntSwitchWithoutDefault(final int value) {
      this.value = value;
    }

    @Override
    public Integer call() throws Exception {
      switch (this.value) {
      case 0:
        return 0;
      case 200:
        return 2;
      case 4000:
        return 4;
      case 800000:
        return 8;
      }
      return -1;
    }
  }

  @Test
  public void shouldReplaceOtherCasesWithoutDefaultForInt() throws Exception {
    final Mutant mutant = getFirstMutant(HasMultipleArmIntSwitchWithoutDefault.class);
    assertMutantCallableReturns(new HasMultipleArmIntSwitchWithoutDefault(0),
        mutant, 0);
    assertMutantCallableReturns(new HasMultipleArmIntSwitchWithoutDefault(200),
        mutant, 2);
    assertMutantCallableReturns(
        new HasMultipleArmIntSwitchWithoutDefault(4000), mutant, -1);
    assertMutantCallableReturns(new HasMultipleArmIntSwitchWithoutDefault(
        800000), mutant, 8);
    assertMutantCallableReturns(new HasMultipleArmIntSwitchWithoutDefault(8),
        mutant, -1);
  }

  private static class HasFewerLabelsWithDefault implements Callable<Integer> {

    private final int value;

    private HasFewerLabelsWithDefault(final int value) {
      this.value = value;
    }

    @Override
    public Integer call() throws Exception {
      switch (this.value) {
      case 1:
        return 1;
      case 200:
        return 2;
      default:
        return 3;
      }
    }
  }

  @Test
  public void shouldNotTouchIt() throws Exception {
    assertNoMutants(HasFewerLabelsWithDefault.class);
  }
}
