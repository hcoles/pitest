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

import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.IntFunction;

import org.junit.Test;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.verifier.mutants.IntMutantVerifier;
import org.pitest.verifier.mutants.MutantVerifier;
import org.pitest.verifier.mutants.MutatorVerifierStart;

public class RemoveSwitchMutatorTest {

  MutatorVerifierStart v = MutatorVerifierStart.forMutator(new RemoveSwitchMutator(2))
          .notCheckingUnMutatedValues();
  
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

  private static class HasIntSwitchWithDefault implements IntFunction<Integer> {
    @Override
    public Integer apply(int value) {
      switch (value) {
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

  private static class HasContinuousIntSwitchWithDefault implements IntFunction<Integer> {
    @Override
    public Integer apply(int value) {
      switch (value) {
        case 10:
          return 0;
        case 11:
          return 1;
        case 12:
          return 2;
        default:
          return -1;
      }
    }
  }

  @Test
  public void shouldChangeLabelInt() {
    IntMutantVerifier<Integer> v2 = v.forIntFunctionClass(HasIntSwitchWithDefault.class);

    v2.firstMutantShouldReturn(0, 0);
    v2.firstMutantShouldReturn(1, 1);
    v2.firstMutantShouldReturn(2, -1);
    v2.firstMutantShouldReturn(3, -1);
  }

  @Test
  public void includesValuesInDescriptionForTableSwitchMutations() {
    MutatorVerifierStart.forMutator(new RemoveSwitchMutator(0))
            .forIntFunctionClass(HasContinuousIntSwitchWithDefault.class)
            .firstMutantDescription()
            .isEqualTo("RemoveSwitch 0 (case value 10)");

    MutatorVerifierStart.forMutator(new RemoveSwitchMutator(2))
            .forIntFunctionClass(HasContinuousIntSwitchWithDefault.class)
            .firstMutantDescription()
            .isEqualTo("RemoveSwitch 2 (case value 12)");

  }

  private static class HasCharSwitchWithDefault implements Function<Character,Character> {

    @Override
    public Character apply(Character c) {
      char value = c.charValue();
      switch (value) {
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
  public void shouldChangeLabelChar() {
    MutantVerifier<Character, Character> v2 = v.forFunctionClass(HasCharSwitchWithDefault.class);
    v2.firstMutantShouldReturn('a', 'a');
    v2.firstMutantShouldReturn('b', 'b');
    v2.firstMutantShouldReturn('c', 'z');
    v2.firstMutantShouldReturn('z', 'z');
  }

  private static class HasEnumSwitchWithDefault implements Function<TimeUnit,Integer> {

    @Override
    public Integer apply(TimeUnit value) {
      switch (value) {
        case NANOSECONDS:
        return 1;
        case MICROSECONDS:
        return 2;
        case MILLISECONDS:
        return 3;
      default:
        return -1;
      }
    }
  }

  @Test
  public void shouldChangeLabelEnum() {
    MutantVerifier<TimeUnit, Integer> v2 = v.forFunctionClass(HasEnumSwitchWithDefault.class);
    v2.firstMutantShouldReturn(NANOSECONDS, 1);
    v2.firstMutantShouldReturn(MICROSECONDS, 2);
    v2.firstMutantShouldReturn(MILLISECONDS, -1);
    v2.firstMutantShouldReturn(SECONDS, -1);
    v2.firstMutantShouldReturn(MINUTES, -1);
  }

  private static class HasMultipleArmIntSwitchWithDefault implements IntFunction<Integer> {

    @Override
    public Integer apply(int value) {
      switch (value) {
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
  public void shouldReplaceTableLabelsInt() {
    IntMutantVerifier<Integer> v2 = v.forIntFunctionClass(HasMultipleArmIntSwitchWithDefault.class);
    v2.firstMutantShouldReturn(0,
        0);
    v2.firstMutantShouldReturn(1,
        1);
    v2.firstMutantShouldReturn(200,
        2);
    v2.firstMutantShouldReturn(4000,
        0);
    v2.firstMutantShouldReturn(800000,
        8);
  }

  @Test
  public void includesValueInLookupSwitchDescription() {
      IntMutantVerifier<Integer> v2 = v.forIntFunctionClass(HasMultipleArmIntSwitchWithDefault.class);
      v2.firstMutantDescription().isEqualTo("RemoveSwitch 2 (case value 4000)");
  }

  private static class HasMultipleArmIntSwitchWithoutDefault implements IntFunction<Integer> {

    @Override
    public Integer apply(int value) {
      switch (value) {
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
  public void shouldReplaceOtherCasesWithoutDefaultForInt() {
    IntMutantVerifier<Integer> v2 = v.forIntFunctionClass(HasMultipleArmIntSwitchWithoutDefault.class);

    v2.firstMutantShouldReturn(0,
        0);
    v2.firstMutantShouldReturn(200,
        2);
    v2.firstMutantShouldReturn(4000, -1);
    v2.firstMutantShouldReturn(
        800000, 8);
    v2.firstMutantShouldReturn(8,
        -1);
  }

  private static class HasFewerLabelsWithDefault implements IntFunction<Integer> {

    @Override
    public Integer apply(int value) {
      switch (value) {
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
  public void shouldNotTouchIt() {
    v.forClass(HasFewerLabelsWithDefault.class)
                    .noMutantsCreated();
  }


  private static class FallThroughLookupSwitch implements IntFunction<Integer> {

    @Override
    public Integer apply(int value) {
      int i = 0;
      switch (value) {
        case 1:
          i = 1;
          break;
        case 200:
          i = 4;
          break;
        case 300:
        case 400:
        default:
          i = 0;
      }
      return i;
    }
  }

  @Test
  public void doesNotReplaceLookupSwitchCasesThatFallThroughToDefault() {
    v.forClass(FallThroughLookupSwitch.class)
            .noMutantsCreated();
  }

  private static class FallThroughTableSwitch implements Function<TimeUnit,Integer> {

    @Override
    public Integer apply(TimeUnit value) {
      switch (value) {
        case NANOSECONDS:
          return 1;
        case MICROSECONDS:
          return 2;
        case MILLISECONDS:
        case SECONDS:
        default:
          return -1;
      }
    }
  }

  @Test
  public void doesNotReplaceTableSwitchCasesThatFallThroughToDefault() {
    v.forClass(FallThroughTableSwitch.class)
            .noMutantsCreated();
  }


}


