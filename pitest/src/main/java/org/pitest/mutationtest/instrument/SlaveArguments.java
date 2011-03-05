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
package org.pitest.mutationtest.instrument;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.pitest.extension.TestUnit;
import org.pitest.functional.Option;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.mutationtest.engine.MutationIdentifier;

public class SlaveArguments {

  final Collection<MutationIdentifier> mutations;
  final List<TestUnit>                 tests;
  final Option<Statistics>             stats;
  final MutationConfig                 config;
  final Properties                     systemProperties;
  final Collection<String>             classesToMutate;
  final TimeoutLengthStrategy          timeoutStrategy;

  public SlaveArguments(final Collection<MutationIdentifier> mutations,
      final List<TestUnit> tests, final Option<Statistics> stats,
      final MutationConfig config, final Properties systemProperties,
      final TimeoutLengthStrategy timeoutStrategy,
      final Collection<String> classes) {
    this.mutations = mutations;
    this.tests = tests;
    this.stats = stats;
    this.config = config;
    this.systemProperties = systemProperties;
    this.classesToMutate = classes;
    this.timeoutStrategy = timeoutStrategy;
  }

}
