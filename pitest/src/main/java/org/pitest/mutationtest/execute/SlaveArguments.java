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
import java.util.List;
import java.util.Properties;

import org.pitest.extension.TestUnit;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.mutationtest.MutationDetails;
import org.pitest.mutationtest.instrument.TimeoutLengthStrategy;

public class SlaveArguments {

  final Collection<MutationDetails> mutations;
  final List<TestUnit>              tests;
  final MutationConfig              config;
  final Properties                  systemProperties;
  final TimeoutLengthStrategy       timeoutStrategy;
  final String                      outputFileName;
  final boolean                     verbose;

  public SlaveArguments(final String outputFileName,
      final Collection<MutationDetails> mutations, final List<TestUnit> tests,
      final MutationConfig config, final Properties systemProperties,
      final TimeoutLengthStrategy timeoutStrategy, final boolean verbose) {
    this.outputFileName = outputFileName;
    this.mutations = mutations;
    this.tests = tests;
    this.config = config;
    this.systemProperties = systemProperties;
    this.timeoutStrategy = timeoutStrategy;
    this.verbose = verbose;
  }

  public boolean isVerbose() {
    return this.verbose;
  }

}
