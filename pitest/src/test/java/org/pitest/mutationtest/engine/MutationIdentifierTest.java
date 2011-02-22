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
package org.pitest.mutationtest.engine;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MutationIdentifierTest {

  @Test
  public void isMutatedShouldReturnFalseWhenNoMutationPresent() {
    assertFalse(MutationIdentifier.unmutated("foo").isMutated());
  }

  @Test
  public void isMutatedShouldReturnTrueWhenIdIsForAMutation() {
    final MutationIdentifier testee = new MutationIdentifier("foo", 42,
        "unique name");
    assertTrue(testee.isMutated());
  }
}
