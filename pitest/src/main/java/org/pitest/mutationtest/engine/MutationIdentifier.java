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

import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class MutationIdentifier {

  private final String className;
  private final SortedSet<Integer>    indexes;
  private final String mutator;

  public MutationIdentifier(final String className, int index,
      final String mutatorUniqueId) {
    this(className, Collections.singleton(index), mutatorUniqueId);
  }
  
  public MutationIdentifier(final String className, Set<Integer> indexes,
      final String mutatorUniqueId) {
    this.className = className;
    this.indexes = new TreeSet<Integer>(indexes);
    this.mutator = mutatorUniqueId;
  }

  public String getClazz() {
    return this.className;
  }

  public String getMutator() {
    return this.mutator;
  }


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((className == null) ? 0 : className.hashCode());
    result = prime * result + ((indexes == null) ? 0 : indexes.hashCode());
    result = prime * result + ((mutator == null) ? 0 : mutator.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    MutationIdentifier other = (MutationIdentifier) obj;
    if (className == null) {
      if (other.className != null)
        return false;
    } else if (!className.equals(other.className))
      return false;
    if (indexes == null) {
      if (other.indexes != null)
        return false;
    } else if (!indexes.equals(other.indexes))
      return false;
    if (mutator == null) {
      if (other.mutator != null)
        return false;
    } else if (!mutator.equals(other.mutator))
      return false;
    return true;
  }

  public int getFirstIndex() {
    return this.indexes.iterator().next();
  }

  @Override
  public String toString() {
    return "Mutation -> className=" + this.className 
        + ", mutator=" + this.mutator;
  }

  public boolean matches(MutationIdentifier newId) {
    return className.equals(newId.className) && mutator.equals(newId.mutator) && indexes.contains(newId.getFirstIndex());
    
  }

}
