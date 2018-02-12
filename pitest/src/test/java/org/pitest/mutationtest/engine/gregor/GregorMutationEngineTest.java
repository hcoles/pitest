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
package org.pitest.mutationtest.engine.gregor;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.pitest.mutationtest.engine.gregor.config.DefaultMutationEngineConfiguration;
import org.pitest.mutationtest.engine.gregor.config.Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.ConditionalsBoundaryMutator;
import org.pitest.mutationtest.engine.gregor.mutators.MathMutator;

public class GregorMutationEngineTest {

  private GregorMutationEngine testee;

  @Test
  public void shouldReportNamesOfSuppliedMutators() {
    final Collection<MethodMutatorFactory> mutators = Mutator
        .fromStrings(Arrays.asList("CONDITIONALS_BOUNDARY", "MATH"));
    final DefaultMutationEngineConfiguration config = new DefaultMutationEngineConfiguration(
        i -> true, mutators);
    this.testee = new GregorMutationEngine(config);
    assertEquals(Arrays.asList(
        ConditionalsBoundaryMutator.CONDITIONALS_BOUNDARY_MUTATOR.getName(),
        MathMutator.MATH_MUTATOR.getName()), this.testee.getMutatorNames());

  }

}
