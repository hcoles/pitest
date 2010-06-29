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

public class MutationConfig {

  private final Set<Mutation> mutations = new LinkedHashSet<Mutation>();

  public MutationConfig(final Mutation... mutations) {
    this.mutations.addAll(Arrays.asList(mutations));
  }

  public Boolean has(final Mutation mutation) {
    return this.mutations.contains(mutation);
  }

  public Mutater createMutator() {
    final Mutater m = new Mutater(-1);
    for (final Mutation each : Mutation.values()) {
      final boolean enable = has(each);
      each.function().apply(m, enable);
    }
    return m;
  }

}
