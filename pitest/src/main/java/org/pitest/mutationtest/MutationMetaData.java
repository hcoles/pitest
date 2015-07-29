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
package org.pitest.mutationtest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.pitest.classinfo.ClassName;

public final class MutationMetaData {

  private final List<MutationResult> mutations;

  public MutationMetaData(final List<MutationResult> mutations) {
    this.mutations = mutations;
  }

  public Collection<MutationResult> getMutations() {
    return this.mutations;
  }

  public Collection<ClassMutationResults> toClassResults() {
    Collections.sort(this.mutations, comparator());
    final List<ClassMutationResults> cmrs = new ArrayList<ClassMutationResults>();
    final List<MutationResult> buffer = new ArrayList<MutationResult>();
    ClassName cn = null;
    for (final MutationResult each : this.mutations) {
      if ((cn != null) && !each.getDetails().getClassName().equals(cn)) {
        cmrs.add(new ClassMutationResults(buffer));
        buffer.clear();
      }
      cn = each.getDetails().getClassName();
      buffer.add(each);
    }
    if (!buffer.isEmpty()) {
      cmrs.add(new ClassMutationResults(buffer));
    }
    return cmrs;

  }

  private static Comparator<MutationResult> comparator() {
    return new Comparator<MutationResult>() {

      @Override
      public int compare(final MutationResult arg0, final MutationResult arg1) {
        return arg0.getDetails().getId().compareTo(arg1.getDetails().getId());
      }

    };
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result)
        + ((this.mutations == null) ? 0 : this.mutations.hashCode());
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
    final MutationMetaData other = (MutationMetaData) obj;
    if (this.mutations == null) {
      if (other.mutations != null) {
        return false;
      }
    } else if (!this.mutations.equals(other.mutations)) {
      return false;
    }
    return true;
  }

}
