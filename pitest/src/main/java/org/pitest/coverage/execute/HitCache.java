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

import java.util.Set;
import java.util.TreeSet;

class HitCache {

  private Set<Long> cache = new TreeSet<Long>();

  public boolean checkHit(final int classId, final int lineNumber) {
    final long id = ((long) classId << 32) | lineNumber;
    if (this.cache.contains(id)) {
      return true;
    } else {
      this.cache.add(id);
      return false;
    }
  }

  public void reset() {
    this.cache = new TreeSet<Long>();
  }
}