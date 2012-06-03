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

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;

import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;

public class SwitchMutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToMutateOnlySwitchStatements() {
    createTesteeWith(new SwitchMutator());
  }

  private static class HasIntSwitchWithDefault implements Callable<Integer> {

    private int value;

      private HasIntSwitchWithDefault(int value) {
          this.value = value;
      }

      public Integer call() throws Exception {
      switch (value) {
          case 0:
              return 1;
          default:
              return 0;
      }
    }
  }

  @Test
  public void shouldProvideAMeaningfulName() {
    assertEquals("EXPERIMENTAL_SWITCH_MUTATOR",
        new SwitchMutator().getName());
  }

  @Test
  public void shouldSwapFirstCaseWithDefault() throws Exception {
    final Mutant mutant = getFirstMutant(HasIntSwitchWithDefault.class);
      assertMutantCallableReturns(new HasIntSwitchWithDefault(0), mutant, 0);
      assertMutantCallableReturns(new HasIntSwitchWithDefault(1), mutant, 1);
  }


}