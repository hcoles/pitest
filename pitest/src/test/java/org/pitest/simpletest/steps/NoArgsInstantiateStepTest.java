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
package org.pitest.simpletest.steps;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.pitest.util.XStreamCloning;

public class NoArgsInstantiateStepTest {

  @Test
  public void shouldConstructsAnObject() {
    final NoArgsInstantiateStep testee = new NoArgsInstantiateStep(
        NoArgsInstantiateStepTest.class);
    final NoArgsInstantiateStepTest actual = (NoArgsInstantiateStepTest) testee
        .execute(null, null);
    assertNotNull(actual);
  }

  @Test
  public void shouldSerializeAndDeserializeByXStreamWithoutError()
      throws Exception {
    try {
      final NoArgsInstantiateStep testee = new NoArgsInstantiateStep(
          NoArgsInstantiateStepTest.class);
      XStreamCloning.clone(testee);
    } catch (final Throwable t) {
      fail();
    }
  }

}
