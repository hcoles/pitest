/*
 * Copyright 2011 Henry Coles
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
package org.pitest.mutationtest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.mutators.InvertNegsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.MathMutator;

public class MutatorTest {

  @SuppressWarnings("unchecked")
  @Test
  public void shouldFlattenToGroupingsToCollectionsOfMethodMutatorFactories() {
    assertEquals(Arrays.asList(MathMutator.MATH_MUTATOR,
        InvertNegsMutator.INVERT_NEGS_MUTATOR), Mutator.asCollection(
        Mutator.MATH, Mutator.INVERT_NEGS));
  }

  @Test
  public void shouldIncludeAllMutatorsWhenAllRequested() {
    final Set<MethodMutatorFactory> expected = new HashSet<MethodMutatorFactory>();
    for (final Mutator each : Mutator.values()) {
      expected.addAll(Mutator.asCollection(each));
    }
    assertTrue(expected.containsAll(Mutator.asCollection(Mutator.ALL)));
    assertTrue(Mutator.asCollection(Mutator.ALL).containsAll(expected));
  }

}
