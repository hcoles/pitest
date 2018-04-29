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
import java.util.Collections;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;

public class InvertNegsMutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToMutateOnlyNegs() {
    createTesteeWith(InvertNegsMutator.INVERT_NEGS_MUTATOR);
  }

  private static class NothingToMutate {

  }

  @Test
  public void shouldFindNoMutationsWhenNonePossible() {
    final Collection<MutationDetails> actual = findMutationsFor(NothingToMutate.class);
    assertEquals(Collections.emptyList(), actual);
  }

  private static class HasINeg implements Callable<String> {
    public int containsINeg(final int i) {
      return -i;
    }

    @Override
    public String call() throws Exception {
      return "" + containsINeg(1);
    }

  }

  @Test
  public void shouldInvertINegs() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(HasINeg.class);
    assertEquals(1, actual.size());
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new HasINeg(), mutant, "1");
  }

  private static class HasFNeg implements Callable<String> {
    public float containsFNeg(final float i) {
      return -i;
    }

    @Override
    public String call() throws Exception {
      return "" + containsFNeg(1f);
    }

  }

  @Test
  public void shouldInvertFNegs() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(HasFNeg.class);
    assertEquals(1, actual.size());
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new HasFNeg(), mutant, "1.0");
  }

  private static class HasLNeg implements Callable<String> {
    public int containsLNeg(final int i) {
      return -i;
    }

    @Override
    public String call() throws Exception {
      return "" + containsLNeg(1);
    }

  }

  @Test
  public void shouldInvertLNegs() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(HasLNeg.class);
    assertEquals(1, actual.size());
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new HasLNeg(), mutant, "1");
  }

}
