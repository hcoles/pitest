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
package org.pitest.mutationtest.engine.gregor.mutators;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;

public class IncrementsMutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToMutateOnlyIncrements() {
    createTesteeWith(IncrementsMutator.INCREMENTS_MUTATOR);
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
  public void shouldNegateArgumentsToIInc() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(HasIncrement.class);
    assertEquals(1, actual.size());
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new HasIncrement(), mutant, "0");
  }

  @Test
  public void shouldRecordCorrectLineNumberForMutations() {
    final Collection<MutationDetails> actual = findMutationsFor(HasIncrement.class);
    assertEquals(1, actual.size());
    final MutationDetails first = actual.iterator().next();
    assertEquals(37, first.getLineNumber());
  }

}
