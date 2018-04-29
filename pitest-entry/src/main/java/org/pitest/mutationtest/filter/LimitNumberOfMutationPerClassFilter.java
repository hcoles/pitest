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
package org.pitest.mutationtest.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

public class LimitNumberOfMutationPerClassFilter implements MutationInterceptor {

  private final int maxMutationsPerClass;

  public LimitNumberOfMutationPerClassFilter(final int max) {
    this.maxMutationsPerClass = max;
  }

  @Override
  public Collection<MutationDetails> intercept(
      Collection<MutationDetails> mutations, Mutater m) {
    if (mutations.size() <= this.maxMutationsPerClass) {
      return mutations;
    } else {
      return createEvenlyDistributedSampling(mutations);
    }
  }

  private Collection<MutationDetails> createEvenlyDistributedSampling(
      final Collection<MutationDetails> mutations) {
    final Collection<MutationDetails> filtered = new ArrayList<>(
        this.maxMutationsPerClass);
    final int step = (mutations.size() / this.maxMutationsPerClass);
    final Iterator<MutationDetails> it = mutations.iterator();
    while (it.hasNext()) {
      int i = 0;
      MutationDetails value = null;
      while (it.hasNext() && (i != step)) {
        value = it.next();
        i++;
      }
      if (filtered.size() != this.maxMutationsPerClass) {
        filtered.add(value);
      }
    }

    return filtered;
  }

  @Override
  public InterceptorType type() {
    return InterceptorType.FILTER;
  }

  @Override
  public void begin(ClassTree clazz) {
    // noop
  }

  @Override
  public void end() {
    // noop
  }
}
