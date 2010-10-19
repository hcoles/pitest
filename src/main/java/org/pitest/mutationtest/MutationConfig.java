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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import com.reeltwo.jumble.mutation.Mutater;

public final class MutationConfig {

  private final boolean      useHotswap;

  private final int          threshold;
  private final Set<Mutator> mutations = new LinkedHashSet<Mutator>();

  public MutationConfig(final int threshold, final Mutator... mutations) {
    this(true, threshold, mutations);
  }

  public MutationConfig(final boolean useHotSwap, final int threshold,
      final Mutator... mutations) {
    this.useHotswap = useHotSwap;
    this.threshold = threshold;
    this.mutations.addAll(Arrays.asList(mutations));
  }

  public Boolean has(final Mutator mutation) {
    return this.mutations.contains(mutation);
  }

  public Mutater createMutator() {
    final Mutater m = new Mutater(-1);
    for (final Mutator each : Mutator.values()) {
      final boolean enable = has(each);
      each.function().apply(m, enable);
    }
    return m;
  }

  public int getThreshold() {
    return this.threshold;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((this.mutations == null) ? 0 : this.mutations.hashCode());
    result = prime * result + this.threshold;
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
    final MutationConfig other = (MutationConfig) obj;
    if (this.mutations == null) {
      if (other.mutations != null) {
        return false;
      }
    } else if (!this.mutations.equals(other.mutations)) {
      return false;
    }
    if (this.threshold != other.threshold) {
      return false;
    }
    return true;
  }

  public boolean isUseHotswap() {
    return this.useHotswap;
  }

}
