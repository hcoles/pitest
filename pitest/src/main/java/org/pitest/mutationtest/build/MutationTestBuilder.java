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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.pitest.classinfo.ClassName;
import org.pitest.coverage.TestInfo;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationAnalyser;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.testapi.Configuration;
import org.pitest.util.Log;

public class MutationTestBuilder {

  private final static Logger    LOG = Log.getLogger();

  private final MutationSource   mutationSource;
  private final MutationAnalyser analyser;
  private final ReportOptions    data;
  private final MutationConfig   mutationConfig;
  private final Configuration    configuration;
  private final File             baseDir;

  public MutationTestBuilder(final File baseDir,
      final MutationConfig mutationConfig, final MutationAnalyser analyser,
      final MutationSource mutationSource, final ReportOptions data,
      final Configuration configuration) {
    this.data = data;
    this.mutationConfig = mutationConfig;
    this.mutationSource = mutationSource;
    this.analyser = analyser;
    this.configuration = configuration;
    this.baseDir = baseDir;
  }

  public List<MutationAnalysisUnit> createMutationTestUnits(
      final Collection<ClassName> codeClasses) {
    final List<MutationAnalysisUnit> tus = new ArrayList<MutationAnalysisUnit>();

    for (final ClassName clazz : codeClasses) {
      final Collection<MutationDetails> mutationsForClasses = this.mutationSource
          .createMutations(clazz);
      if (mutationsForClasses.isEmpty()) {
        LOG.fine("No mutations found for " + clazz);
      } else {
        createMutationAnalysisUnits(tus, clazz, mutationsForClasses);
      }
    }

    Collections.sort(tus, new AnalysisPriorityComparator());
    return tus;
  }

  private void createMutationAnalysisUnits(
      final List<MutationAnalysisUnit> tus, final ClassName clazz,
      final Collection<MutationDetails> mutationsForClasses) {
    if (this.data.getMutationUnitSize() > 0) {
      final FunctionalList<List<MutationDetails>> groupedMutations = FCollection
          .splitToLength(this.data.getMutationUnitSize(), mutationsForClasses);
      FCollection
          .mapTo(groupedMutations, mutationDetailsToTestUnit(clazz), tus);
    } else {
      tus.add(createMutationTestUnit(mutationsForClasses));
    }
  }

  private F<List<MutationDetails>, MutationAnalysisUnit> mutationDetailsToTestUnit(
      final ClassName clazz) {
    return new F<List<MutationDetails>, MutationAnalysisUnit>() {
      public MutationAnalysisUnit apply(final List<MutationDetails> mutations) {
        return createMutationTestUnit(mutations);
      }
    };
  }

  private MutationAnalysisUnit createMutationTestUnit(
      final Collection<MutationDetails> mutationsForClasses) {

    final Collection<MutationResult> analysedMutations = this.analyser
        .analyse(mutationsForClasses);

    final Collection<MutationDetails> needAnalysis = FCollection.filter(
        analysedMutations, statusNotKnown()).map(resultToDetails());
    final Collection<MutationResult> analysed = FCollection.filter(
        analysedMutations, Prelude.not(statusNotKnown()));

    if (needAnalysis.isEmpty()) {
      return makePreAnalysedUnit(analysed);
    }

    if (analysed.isEmpty()) {
      return makeUnanalysedUnit(needAnalysis);
    }

    return new MixedAnalysisUnit(Arrays.asList(makePreAnalysedUnit(analysed),
        makeUnanalysedUnit(needAnalysis)));

  }

  private MutationAnalysisUnit makePreAnalysedUnit(
      final Collection<MutationResult> analysed) {
    return new KnownStatusMutationTestUnit(
        this.mutationConfig.getMutatorNames(), analysed);
  }

  private MutationAnalysisUnit makeUnanalysedUnit(
      final Collection<MutationDetails> needAnalysis) {
    final Set<ClassName> uniqueTestClasses = new HashSet<ClassName>();
    FCollection.flatMapTo(needAnalysis, mutationDetailsToTestClass(),
        uniqueTestClasses);

    return new MutationTestUnit(this.baseDir, needAnalysis, uniqueTestClasses,
        this.configuration, this.mutationConfig,
        new PercentAndConstantTimeoutStrategy(this.data.getTimeoutFactor(),
            this.data.getTimeoutConstant()), this.data.isVerbose(), this.data
            .getClassPath().getLocalClassPath());
  }

  private static F<MutationResult, MutationDetails> resultToDetails() {
    return new F<MutationResult, MutationDetails>() {
      public MutationDetails apply(final MutationResult a) {
        return a.getDetails();
      }
    };
  }

  private static F<MutationResult, Boolean> statusNotKnown() {
    return new F<MutationResult, Boolean>() {
      public Boolean apply(final MutationResult a) {
        return a.getStatus() == DetectionStatus.NOT_STARTED;
      }
    };
  }

  private static F<MutationDetails, Iterable<ClassName>> mutationDetailsToTestClass() {
    return new F<MutationDetails, Iterable<ClassName>>() {
      public Iterable<ClassName> apply(final MutationDetails a) {
        return FCollection.map(a.getTestsInOrder(),
            TestInfo.toDefiningClassName());
      }
    };
  }

}
