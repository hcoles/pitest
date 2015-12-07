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
package org.pitest.mutationtest.engine.gregor.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.mutators.ArgumentPropagationMutator;
import org.pitest.mutationtest.engine.gregor.mutators.InvertNegsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.MathMutator;

public class MutatorTest {

  @SuppressWarnings("unchecked")
  @Test
  public void shouldReturnRequestedMutators() {
    assertThat(parseStrings("MATH", "INVERT_NEGS")).containsAll(
        Arrays.asList(MathMutator.MATH_MUTATOR,
            InvertNegsMutator.INVERT_NEGS_MUTATOR));
  }

  @Test
  public void shouldNotCreateDuplicatesWhenRequestedDirectly() {
    assertThat(parseStrings("MATH", "MATH")).hasSize(1);
  }

  @Test
  public void shouldNotCreateDuplicatesWhenRequestedViaGroup() {
    assertThat(parseStrings("MATH", "DEFAULTS")).hasSameSizeAs(
        parseStrings("DEFAULTS"));
  }

  private Collection<MethodMutatorFactory> parseStrings(final String... s) {
    return Mutator.fromStrings(Arrays.asList(s));
  }

  @Test
  public void allContainsReplaceMethodMutator() throws Exception {
    assertThat(Mutator.all()).contains(
        ArgumentPropagationMutator.ARGUMENT_PROPAGATION_MUTATOR);
  }
}
