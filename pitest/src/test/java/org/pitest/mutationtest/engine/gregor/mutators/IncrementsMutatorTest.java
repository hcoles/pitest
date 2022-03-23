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
import static org.pitest.mutationtest.engine.gregor.mutators.IncrementsMutator.INCREMENTS;

import java.util.concurrent.Callable;

import org.junit.Test;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.verifier.mutants.MutatorVerifierStart;

public class IncrementsMutatorTest {

  MutatorVerifierStart v = MutatorVerifierStart.forMutator(INCREMENTS);

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
  public void shouldNegateArgumentsToIInc()  {
    v.forCallableClass(HasIncrement.class)
            .firstMutantShouldReturn("0");
  }

  @Test
  public void shouldRecordCorrectLineNumberForMutations() {
    MutationDetails actual = v.forClass(HasIncrement.class)
            .firstMutant();
    assertEquals(32, actual.getLineNumber());
  }

}
