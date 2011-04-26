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
package org.pitest.mutationtest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.pitest.Description;
import org.pitest.coverage.domain.TestInfo;
import org.pitest.extension.Configuration;
import org.pitest.extension.TestUnit;
import org.pitest.internal.IsolationUtils;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.instrument.InstrumentedMutationTestUnit;
import org.pitest.mutationtest.instrument.PercentAndConstantTimeoutStrategy;
import org.pitest.util.JavaAgent;

public class MutationTestBuilder {

  private final ReportOptions  data;
  private final JavaAgent      javaAgentFinder;
  private final MutationConfig mutationConfig;

  private final Configuration  initialConfig;

  public MutationTestBuilder(final MutationConfig mutationConfig,
      final Configuration initialConfig, final ReportOptions data,
      final JavaAgent javaAgentFinder) {
    this.data = data;
    this.javaAgentFinder = javaAgentFinder;
    this.mutationConfig = mutationConfig;
    this.initialConfig = initialConfig;
  }

  public List<TestUnit> createMutationTestUnits(
      final Map<ClassGrouping, List<String>> groupedClassesToTests,
      final Configuration pitConfig, final CoverageDatabase coverageDatabase) {
    final List<TestUnit> tus = new ArrayList<TestUnit>();

    for (final Entry<ClassGrouping, List<String>> codeToTests : groupedClassesToTests
        .entrySet()) {

      final Collection<MutationDetails> mutationsForClasses = createMutations(
          coverageDatabase, this.mutationConfig, codeToTests.getKey(),
          codeToTests.getValue());

      tus.add(createMutationTestUnit(this.mutationConfig, mutationsForClasses,
          codeToTests.getKey(), codeToTests.getValue()));

    }
    return tus;
  }

  private Collection<MutationDetails> createMutations(
      final CoverageDatabase coverageDatabase,
      final MutationConfig mutationConfig, final ClassGrouping classesToMutate,
      final List<String> relevantTestClasses) {
    mutationConfig.createMutator(IsolationUtils.getContextClassLoader());

    final Mutater m = mutationConfig.createMutator(IsolationUtils
        .getContextClassLoader());

    final Collection<MutationDetails> availableMutations = m
        .findMutations(classesToMutate);

    assignTestsToMutations(availableMutations, coverageDatabase);

    return availableMutations;

  }

  private void assignTestsToMutations(
      final Collection<MutationDetails> availableMutations,
      final CoverageDatabase coverageDatabase) {
    for (final MutationDetails mutation : availableMutations) {
      final Collection<TestInfo> testDetails = coverageDatabase
          .getTestsForMutant(mutation);

      mutation.addTestsInOrder(testDetails);
    }
  }

  private TestUnit createMutationTestUnit(final MutationConfig mutationConfig,
      final Collection<MutationDetails> mutationsForClasses,
      final ClassGrouping classGrouping, final List<String> testClasses) {

    final Description d = new Description("mutation test of "
        + classGrouping.getParent(), MutationCoverageReport.class, null);

    return new InstrumentedMutationTestUnit(mutationsForClasses, testClasses,
        this.initialConfig, mutationConfig, d, this.javaAgentFinder,
        new PercentAndConstantTimeoutStrategy(this.data.getTimeoutFactor(),
            this.data.getTimeoutConstant()));
  }

}
