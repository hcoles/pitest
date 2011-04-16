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
package org.pitest.mutationtest.engine.gregor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;
import org.pitest.functional.FunctionalList;
import org.pitest.mutationtest.MutationDetails;
import org.pitest.mutationtest.Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.IncrementsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.InvertNegsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.MathMutator;
import org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator;

public class TestGregorMutater extends MutatorTestBase {

  public static class HasMultipleMutations {
    public int mutable() {
      int j = 10;
      for (int i = 0; i != 10; i++) {
        j = j << 1;
      }

      return -j;
    }

  }

  @Test
  public void shouldFindMutationsFromAllSuppliedMutators() throws Exception {

    createTesteeWith(MathMutator.MATH_MUTATOR,
        ReturnValsMutator.RETURN_VALS_MUTATOR,
        InvertNegsMutator.INVERT_NEGS_MUTATOR,
        IncrementsMutator.INCREMENTS_MUTATOR);

    final FunctionalList<MutationDetails> actualDetails = findMutationsFor(HasMultipleMutations.class);

    assertTrue(actualDetails
        .contains(descriptionContaining("Replaced Shift Left with Shift Right")));
    assertTrue(actualDetails
        .contains(descriptionContaining("replaced return of integer")));
    assertTrue(actualDetails
        .contains(descriptionContaining("Changed increment")));
    assertTrue(actualDetails
        .contains(descriptionContaining("removed negation")));

  }

  @Test
  public void shouldFindNoMutationsWhenNoMutationOperatorsSupplied()
      throws Exception {
    class VeryMutable {
      @SuppressWarnings("unused")
      public int f(final int i) {
        switch (i) {
        case 0:
          return 1;
        }
        return 0;
      }
    }
    createTesteeWith();
    final FunctionalList<MutationDetails> actualDetails = findMutationsFor(VeryMutable.class);
    assertTrue(actualDetails.isEmpty());

  }

  static enum AnEnum {
    Foo, Bar;
  }

  @Test
  public void shouldNotMutateCodeGeneratedByCompilerToImplementEnums() {
    createTesteeWith(Mutator.values());
    final Collection<MutationDetails> actualDetails = findMutationsFor(AnEnum.class);
    assertTrue(actualDetails.isEmpty());
  }

  static enum EnumWithCustomConstructor {
    Foo, Bar;

    int i;

    EnumWithCustomConstructor() {
      this.i++;
    }

  }

  @Test
  public void shouldMutateCustomConstructorsAddedToEnums() {
    createTesteeWith(Mutator.values());
    final Collection<MutationDetails> actualDetails = findMutationsFor(EnumWithCustomConstructor.class);
    assertFalse(actualDetails.isEmpty());
  }

}
