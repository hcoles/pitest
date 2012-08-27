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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.pitest.classinfo.ClassName;

public class MutationIdentifier {

  private final String              className;
  private final Collection<Integer> indexes;
  private final String              mutator;

  public MutationIdentifier(final String className, final int index,
      final String mutatorUniqueId) {
    this(className, Collections.singleton(index), mutatorUniqueId);
  }

  public MutationIdentifier(final String className, final Set<Integer> indexes,
      final String mutatorUniqueId) {
    this.className = className.intern();
    this.indexes = new ArrayList<Integer>(indexes);
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
    result = (prime * result)
        + ((this.className == null) ? 0 : this.className.hashCode());
    result = (prime * result)
        + ((this.indexes == null) ? 0 : this.indexes.hashCode());
    result = (prime * result)
        + ((this.mutator == null) ? 0 : this.mutator.hashCode());
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
    final MutationIdentifier other = (MutationIdentifier) obj;
    if (this.className == null) {
      if (other.className != null) {
        return false;
      }
    } else if (!this.className.equals(other.className)) {
      return false;
    }
    if (this.indexes == null) {
      if (other.indexes != null) {
        return false;
      }
    } else if (!this.indexes.equals(other.indexes)) {
      return false;
    }
    if (this.mutator == null) {
      if (other.mutator != null) {
        return false;
      }
    } else if (!this.mutator.equals(other.mutator)) {
      return false;
    }
    return true;
  }

  public int getFirstIndex() {
    return this.indexes.iterator().next();
  }

  @Override
  public String toString() {
    return "Mutation -> className=" + this.className + ", mutator="
        + this.mutator;
  }

  public boolean matches(final MutationIdentifier newId) {
    return this.className.equals(newId.className)
        && this.mutator.equals(newId.mutator)
        && this.indexes.contains(newId.getFirstIndex());
  }

  public ClassName getClassName() {
    return ClassName.fromString(this.className);
  }

}
