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

public class MutationIdentifier {

  private static final int UNMUTATED = -1;

  private final String     className;
  private final int        index;
  private final String     mutator;

  public MutationIdentifier(final String className, final int index,
      final String mutatorUniqueId) {
    this.className = className;
    this.index = index;
    this.mutator = mutatorUniqueId;
  }

  public String getClazz() {
    return this.className;
  }

  public int getIndex() {
    return this.index;
  }

  public boolean isMutated() {
    return this.index != UNMUTATED;
  }

  public static MutationIdentifier unmutated(final String clazz) {
    return new MutationIdentifier(clazz, UNMUTATED, "NoMutation");
  }

  public String getMutator() {
    return this.mutator;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((this.className == null) ? 0 : this.className.hashCode());
    result = prime * result + this.index;
    result = prime * result
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
    if (this.index != other.index) {
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

  @Override
  public String toString() {
    return "MutationIdentifier [className=" + this.className + ", index="
        + this.index + ", mutator=" + this.mutator + "]";
  }

}
