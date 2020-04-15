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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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
    this.mutations.sort(comparator());
    final List<ClassMutationResults> cmrs = new ArrayList<>();
    final List<MutationResult> buffer = new ArrayList<>();
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
    return Comparator.comparing(arg0 -> arg0.getDetails().getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(mutations);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final MutationMetaData other = (MutationMetaData) obj;
    return Objects.equals(mutations, other.mutations);
  }
}
