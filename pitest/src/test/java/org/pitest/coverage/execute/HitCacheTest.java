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
package org.pitest.coverage.execute;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;


public class HitCacheTest {

  private HitCache testee;

  @Before
  public void setup() {
    this.testee = new HitCache();
  }


  @Test
  public void resetShouldCleanCache() {
    this.testee.add(0, 1);
    this.testee.reset();
    assertTrue(this.testee.values().isEmpty());
  }

  @Test
  public void shouldSupportMaxIntClassesAndMaxIntLinesPerClass() {
    testee.add(Integer.MAX_VALUE, Integer.MAX_VALUE);
    long encoded = testee.values().iterator().next();
    assertEquals(Integer.MAX_VALUE, HitCache.decodeClassId(encoded));
    assertEquals(Integer.MAX_VALUE, HitCache.decodeLineId(encoded));
  }


}
