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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Callable;

import org.junit.Test;

/**
 * Unit Test für {@link MutantStarter} helper class.
 *
 * @author Stefan Penndorf <stefan.penndorf@gmail.com>
 */
public class MutantStarterTest {

  static final class TestMutee implements Callable<TestMutee> {

    private static boolean muteeCreated = false;

    TestMutee() {
      super();
      TestMutee.muteeCreated = true;
    }

    @Override
    public TestMutee call() throws Exception {
      return this;
    }
  }

  @Test
  public void shouldConstructMuteeUsingNoArgConstructor() throws Exception {
    final TestMutee createdMutant = new MutantStarter<>(
        TestMutee.class).call();
    assertNotNull(createdMutant);
  }

  @Test
  public void shouldConstructMuteeInCallMethod() throws Exception {
    TestMutee.muteeCreated = false;
    final MutantStarter<TestMutee> starter = new MutantStarter<>(
        TestMutee.class);

    assertFalse("TestMutee has not been created yet.", TestMutee.muteeCreated);

    starter.call();

    assertTrue("TestMutee has been created in call() method",
        TestMutee.muteeCreated);
  }

  private static class MutantStarterSubclass extends MutantStarter<Integer> {
    private final int number;

    public MutantStarterSubclass(final int number) {
      super();
      this.number = number;
    }

    @Override
    protected Callable<Integer> constructMutee() throws Exception {
      return () -> MutantStarterSubclass.this.number;
    }
  }

  @Test
  public void canBeSubclassedToUseOwnConstructionLogic() throws Exception {
    assertEquals(42, (int) new MutantStarterSubclass(42).call());
  }

}
