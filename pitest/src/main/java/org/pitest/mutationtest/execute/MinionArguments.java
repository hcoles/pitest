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
package org.pitest.mutationtest.execute;

import java.util.Collection;

import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.TimeoutLengthStrategy;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.testapi.Configuration;

public class MinionArguments {

  final Collection<MutationDetails> mutations;
  final Collection<ClassName>       testClasses;
  final MutationEngine              engine;
  final TimeoutLengthStrategy       timeoutStrategy;
  final boolean                     verbose;
  final Configuration               pitConfig;

  public MinionArguments(final Collection<MutationDetails> mutations,
      final Collection<ClassName> tests, final MutationEngine engine,
      final TimeoutLengthStrategy timeoutStrategy, final boolean verbose,
      final Configuration pitConfig) {
    this.mutations = mutations;
    this.testClasses = tests;
    this.engine = engine;
    this.timeoutStrategy = timeoutStrategy;
    this.verbose = verbose;
    this.pitConfig = pitConfig;
  }

  public boolean isVerbose() {
    return this.verbose;
  }

}
