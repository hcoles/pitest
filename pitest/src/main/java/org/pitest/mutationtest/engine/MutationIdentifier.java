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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.pitest.classinfo.ClassName;

/**
 * Uniquely identifies a mutation
 */
public final class MutationIdentifier implements Comparable<MutationIdentifier>, Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * The location at which the mutation occurs
   */
  private final Location      location;

  /**
   * The indexes to the instructions within the method at which the mutation
   * occurs.
   *
   * Usually this will be a single instruction, but may be multiple if the
   * mutation has been inlined by the compiler to implement a finally block
   */
  private final List<Integer> indexes;

  /**
   * Name of the mutation operator that created this mutation
   */
  private final String        mutator;

  public MutationIdentifier(final Location location, final int index,
      final String mutatorUniqueId) {
    this(location, Collections.singleton(index), mutatorUniqueId);
  }

  public MutationIdentifier(final Location location,
      final Collection<Integer> indexes, final String mutatorUniqueId) {
    this.location = location;
    this.indexes = new ArrayList<>(indexes);
    this.mutator = mutatorUniqueId;
  }

  /**
   * Returns the location of the mutations
   *
   * @return the location of the mutation
   */
  public Location getLocation() {
    return this.location;
  }

  /**
   * Returns the name of the mutator that created this mutation
   *
   * @return the mutator name
   */
  public String getMutator() {
    return this.mutator;
  }

  /**
   * Returns the list of instruction indexes to which this mutation applies
   *
   * @return the instruction indexes of the mutation
   */
  public List<Integer> getIndexes() {
    return indexes;
  }

  /**
   * Returns the index to the first instruction on which this mutation occurs.
   * This index is specific to how ASM represents the bytecode.
   *
   * @return the zero based index to the instruction
   */
  public int getFirstIndex() {
    return this.indexes.iterator().next();
  }

  @Override
  public String toString() {
    return "MutationIdentifier [location=" + this.location + ", indexes="
        + this.indexes + ", mutator=" + this.mutator + "]";
  }

  /**
   * Returns true if this mutation has a matching identifier
   *
   * @param id
   *          the MutationIdentifier to match
   * @return true if the MutationIdentifier matches otherwise false
   */
  public boolean matches(final MutationIdentifier id) {
    return this.location.equals(id.location) && this.mutator.equals(id.mutator)
        && this.indexes.contains(id.getFirstIndex());
  }

  /**
   * Returns the class in which this mutation is located
   *
   * @return class in which mutation is located
   */
  public ClassName getClassName() {
    return this.location.getClassName();
  }

  @Override
  public int hashCode() {
    return Objects.hash(location, indexes, mutator);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final MutationIdentifier other = (MutationIdentifier) obj;
    return Objects.equals(location, other.location)
            && Objects.equals(indexes, other.indexes)
            && Objects.equals(mutator, other.mutator);
  }

  @Override
  public int compareTo(final MutationIdentifier other) {
    int comp = this.location.compareTo(other.getLocation());
    if (comp != 0) {
      return comp;
    }
    comp = this.mutator.compareTo(other.getMutator());
    if (comp != 0) {
      return comp;
    }
    return this.indexes.get(0).compareTo(other.indexes.get(0));
  }

}
