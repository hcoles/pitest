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

import java.util.HashSet;
import java.util.Set;

class HitCache {

  private static class Pair {
    final int classId;
    final int lineNumber;

    Pair(final int classId, final int lineNumber) {
      this.classId = classId;
      this.lineNumber = lineNumber;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + this.classId;
      result = prime * result + this.lineNumber;
      return result;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final Pair other = (Pair) obj;
      if (this.classId != other.classId) {
        return false;
      }
      if (this.lineNumber != other.lineNumber) {
        return false;
      }
      return true;
    }

  }

  private Set<Pair> cache = new HashSet<Pair>();

  public boolean checkHit(final int classId, final int lineNumber) {
    final Pair id = new Pair(classId, lineNumber);
    if (this.cache.contains(id)) {
      return true;
    } else {
      this.cache.add(id);
      return false;
    }
  }

  public void reset() {
    this.cache = new HashSet<Pair>();
  }
}