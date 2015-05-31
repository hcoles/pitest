/*
 * Copyright 2015 Urs Metz
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

import org.junit.Before;
import org.junit.Test;
import org.pitest.functional.predicate.True;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;

import java.util.concurrent.Callable;

import static org.pitest.mutationtest.engine.gregor.mutators.NakedReceiverMutator.NAKED_RECEIVER;

public class NakedReceiverMutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToUseReplaceMethodWithArgumentOfSameTypeAsReturnValueMutator() {
    createTesteeWith(True.<MethodInfo> all(), NAKED_RECEIVER);
  }

  @Test
  public void shouldReplaceMethodCallOnString() throws Exception {
    final Mutant mutant = getFirstMutant(HasStringMethodCall.class);
    assertMutantCallableReturns(new HasStringMethodCall("EXAMPLE"), mutant,
        "EXAMPLE");
  }

  @Test
  public void shouldNotReplaceMethodCallWhenDifferentReturnType()
      throws Exception {
    assertNoMutants(HasMethodWithDifferentReturnType.class);
  }

  @Test
  public void shouldNotReplaceVoidMethodCall()
      throws Exception {
    assertNoMutants(HasVoidMethodCall.class);
  }

  private static class HasStringMethodCall implements Callable<String> {
    private String arg;

    public HasStringMethodCall(String arg) {
      this.arg = arg;
    }

    public String call() throws Exception {
      return arg.toLowerCase();
    }
  }

  private static class HasMethodWithDifferentReturnType {
    public int call() throws Exception {
      return "".length();
    }
  }

  private static class HasVoidMethodCall {
    public void call() throws Exception {
    }
  }

}
