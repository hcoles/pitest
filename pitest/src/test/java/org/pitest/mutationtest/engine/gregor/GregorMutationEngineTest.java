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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;
import org.pitest.functional.predicate.True;
import org.pitest.mutationtest.Mutator;
import org.pitest.mutationtest.config.DefaultMutationEngineConfiguration;


public class GregorMutationEngineTest {

  private GregorMutationEngine testee;

  @Test
  public void shouldReportNamesOfSuppliedMutators() {
    Collection<MethodMutatorFactory> mutators = new ArrayList<MethodMutatorFactory>();
    mutators.add(Mutator.MATH);
    mutators.add(Mutator.CONDITIONALS_BOUNDARY);
    DefaultMutationEngineConfiguration config = new DefaultMutationEngineConfiguration(True.<MethodInfo>all(), Collections.<String>emptyList(), mutators);
    this.testee = new GregorMutationEngine(config);
    assertEquals(Arrays.asList(Mutator.CONDITIONALS_BOUNDARY.getName(), Mutator.MATH.getName()),this.testee.getMutatorNames());

  }

}
