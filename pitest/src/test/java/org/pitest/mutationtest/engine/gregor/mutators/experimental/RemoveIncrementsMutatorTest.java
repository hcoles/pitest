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
package org.pitest.mutationtest.engine.gregor.mutators.experimental;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;

public class RemoveIncrementsMutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToMutateOnlyIncrements() {
    createTesteeWith(RemoveIncrementsMutator.REMOVE_INCREMENTS_MUTATOR);
  }

  private static class HasIncrement implements Callable<String> {
    public int containsIincInstructions(int i) {
      return ++i;
    }

    @Override
    public String call() throws Exception {
      return "" + containsIincInstructions(1);
    }

  }

  @Test
  public void shouldProvideAMeaningfulName() {
    assertEquals("REMOVE_INCREMENTS_MUTATOR",
        RemoveIncrementsMutator.REMOVE_INCREMENTS_MUTATOR.getName());
  }

  @Test
  public void shouldRemoveArgumentsToIInc() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(HasIncrement.class);
    assertEquals(1, actual.size());
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new HasIncrement(), mutant, "1");
  }

  private static class HasNoIncrements implements Callable<String> {

    @Override
    public String call() throws Exception {
      return "foo";
    }

  }

  @Test
  public void shouldCreateNoMutationsWhenNoIncrementsPresent() {
    final Collection<MutationDetails> actual = findMutationsFor(HasNoIncrements.class);
    assertThat(actual).isEmpty();
  }

}
