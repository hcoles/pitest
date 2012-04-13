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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class HitCache {

  private Set<Long> cache = new HashSet<Long>();

  public void add(final int classId, final int lineNumber) {
    final long id = ((long) classId << 32) | lineNumber;
    this.cache.add(id);
  }

  public void reset() {
    this.cache = new HashSet<Long>();
  }
  
  public Collection<Long> values() {
    return this.cache;
  }
  
  public long size() {
    return this.cache.size();
  }
  
  public static int decodeClassId(long value) {
    return (int) (value >> 32);
  }

  public static int decodeLineId(long value) {
    return (int) (value & 0xFFFFFFFF);
  }

}