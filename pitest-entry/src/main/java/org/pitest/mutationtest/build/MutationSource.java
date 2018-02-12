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
package org.pitest.mutationtest.build;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classinfo.CachingByteArraySource;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.coverage.TestInfo;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.util.Log;

public class MutationSource {

  private static final Logger        LOG = Log.getLogger();

  private final MutationConfig       mutationConfig;
  private final TestPrioritiser      testPrioritiser;
  private final ClassByteArraySource source;
  private final MutationInterceptor interceptor;

  public MutationSource(final MutationConfig mutationConfig,
      final TestPrioritiser testPrioritiser,
      final ClassByteArraySource source,
      final MutationInterceptor interceptor) {
    this.mutationConfig = mutationConfig;
    this.testPrioritiser = testPrioritiser;
    this.source = new CachingByteArraySource(source, 200);
    this.interceptor = interceptor;
  }

  public Collection<MutationDetails> createMutations(final ClassName clazz) {

    final Mutater m = this.mutationConfig.createMutator(this.source);

    final Collection<MutationDetails> availableMutations = m
        .findMutations(clazz);

    if (availableMutations.isEmpty()) {
      return availableMutations;
    } else {
      final ClassTree tree = ClassTree
          .fromBytes(this.source.getBytes(clazz.asJavaName()).get());

      this.interceptor.begin(tree);
      final Collection<MutationDetails> updatedMutations = this.interceptor
          .intercept(availableMutations, m);
      this.interceptor.end();

      assignTestsToMutations(updatedMutations);

      return updatedMutations;
    }
  }

  private void assignTestsToMutations(
      final Collection<MutationDetails> availableMutations) {
    for (final MutationDetails mutation : availableMutations) {
      final List<TestInfo> testDetails = this.testPrioritiser
          .assignTests(mutation);
      if (testDetails.isEmpty()) {
        LOG.fine("According to coverage no tests hit the mutation " + mutation);
      }
      mutation.addTestsInOrder(testDetails);
    }
  }

}
