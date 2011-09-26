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

import static org.junit.Assert.assertFalse;
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
  public void shouldDetectThatLineHasBeenVisited() {
    assertFalse(this.testee.checkHit(0, 1));
    assertTrue(this.testee.checkHit(0, 1));
  }

  @Test
  public void shouldDistinguishBetweenDifferentLines() {
    final int clazz = Integer.MAX_VALUE;
    for (int line = Integer.MAX_VALUE - 100000; line != Integer.MAX_VALUE; line++) {
      assertFalse("should not have previous visit for line " + line,
          this.testee.checkHit(clazz, line));
      assertTrue("should have visit for line " + line,
          this.testee.checkHit(clazz, line));
    }
  }

  @Test
  public void shouldDistinguishBetweenDifferentClasses() {
    final int line = Integer.MAX_VALUE;
    for (int clazz = Integer.MAX_VALUE - 1000000; clazz != Integer.MAX_VALUE; clazz++) {
      assertFalse("should not have previous visit for class " + clazz,
          this.testee.checkHit(clazz, line));
      assertTrue("should have visit for class " + clazz,
          this.testee.checkHit(clazz, line));
    }
  }

  @Test
  public void resetShouldCleanCache() {
    this.testee.checkHit(0, 1);
    this.testee.reset();
    assertFalse(this.testee.checkHit(0, 1));
  }

  @Test
  public void shouldSupportMaxIntClassesAndMaxIntLinesPerClass() {
    assertFalse(this.testee.checkHit(Integer.MAX_VALUE, Integer.MAX_VALUE));
    assertTrue(this.testee.checkHit(Integer.MAX_VALUE, Integer.MAX_VALUE));
  }

}
