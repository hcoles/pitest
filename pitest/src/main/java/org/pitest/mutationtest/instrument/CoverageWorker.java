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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pitest.containers.UnContainer;
import org.pitest.coverage.CodeCoverageStore;
import org.pitest.coverage.CoverageStatistics;
import org.pitest.coverage.InvokeQueue;
import org.pitest.coverage.codeassist.CoverageTransformation;
import org.pitest.extension.Container;
import org.pitest.extension.TestUnit;
import org.pitest.extension.Transformation;
import org.pitest.extension.common.CompoundTransformation;
import org.pitest.functional.F;
import org.pitest.functional.F2;
import org.pitest.functional.FCollection;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.SideEffect1;
import org.pitest.internal.transformation.IdentityTransformation;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.instrument.ResultsReader.DetectionStatus;
import org.pitest.mutationtest.loopbreak.LoopBreakTransformation;
import org.pitest.mutationtest.loopbreak.PerProcessTimelimitCheck;
import org.pitest.util.Unchecked;

public class CoverageWorker extends AbstractWorker {

  private final List<TestUnit> tests;

  public CoverageWorker(final List<TestUnit> tests,
      final F2<Class<?>, byte[], Boolean> hotswap, final Mutater mutater,
      final ClassLoader loader) {
    super(hotswap, mutater, loader);
    this.tests = tests;
  }

  public Statistics gatherStatistics(final Collection<String> classNames,
      final Reporter r) {

    final FunctionalList<Mutant> unmutatedClasses = FCollection.map(classNames,
        nameToUnmodifiedClass());

    final CoverageStatistics invokeStatistics = new CoverageStatistics();
    final InvokeQueue invokeQueue = new InvokeQueue();
    CodeCoverageStore.init(invokeQueue, invokeStatistics);
    instrumentClassesForCodeCoverage(unmutatedClasses);

    final Map<ClassLine, List<TestUnit>> lineToTestUnitsMap = new HashMap<ClassLine, List<TestUnit>>();

    final List<TestUnit> decoratedTests = decorateForCoverage(this.tests,
        invokeStatistics, invokeQueue, lineToTestUnitsMap, classNames);

    final Container c = new UnContainer();

    final DetectionStatus status = doTestsDetectMutation(c, decoratedTests);

    removeInstrumentationFromClasses(unmutatedClasses);

    final Map<TestUnit, Long> executionTimes = new HashMap<TestUnit, Long>();
    for (final TestUnit each : decoratedTests) {
      final CoverageDecorator cd = (CoverageDecorator) each;
      executionTimes.put(cd.child(), cd.getExecutionTime());
    }

    return new Statistics(!status.isDetected(), executionTimes,
        lineToTestUnitsMap);
  }

  private void instrumentClassesForCodeCoverage(
      final FunctionalList<Mutant> unmutatedClasses) {
    final CoverageTransformation cf = new CoverageTransformation();
    // include loopbreaking as it may affect timing
    final Transformation transformForLineCoverage = new CompoundTransformation(
        cf, new LoopBreakTransformation());
    PerProcessTimelimitCheck.disableLoopBreaking();
    unmutatedClasses
        .forEach(hotswapClassWithTransformation(transformForLineCoverage));
  }

  private void removeInstrumentationFromClasses(
      final FunctionalList<Mutant> unmutatedClasses) {
    final IdentityTransformation backToUnmutated = new IdentityTransformation();
    unmutatedClasses.forEach(hotswapClassWithTransformation(backToUnmutated));
  }

  private SideEffect1<Mutant> hotswapClassWithTransformation(
      final Transformation t) {
    return new SideEffect1<Mutant>() {

      public void apply(final Mutant mutant) {
        try {
          final Class<?> testee = Class.forName(mutant.getDetails().getClazz(),
              false, CoverageWorker.this.loader);
          final byte[] instrumentedClass = t.transform(mutant.getDetails()
              .getClazz(), mutant.getBytes());
          CoverageWorker.this.hotswap.apply(testee, instrumentedClass);
        } catch (final ClassNotFoundException ex) {
          throw Unchecked.translateCheckedException(ex);
        }
      }

    };
  }

  private F<String, Mutant> nameToUnmodifiedClass() {
    return new F<String, Mutant>() {

      public Mutant apply(final String clazz) {
        return CoverageWorker.this.mutater.getUnmodifiedClass(clazz);
      }

    };
  }

  private List<TestUnit> decorateForCoverage(final List<TestUnit> plainTests,
      final CoverageStatistics stats, final InvokeQueue queue,
      final Map<ClassLine, List<TestUnit>> lineMapping,
      final Collection<String> classNames) {
    final List<TestUnit> decorated = new ArrayList<TestUnit>(plainTests.size());
    for (final TestUnit each : plainTests) {
      decorated.add(new CoverageDecorator(classNames, queue, stats,
          lineMapping, each));
    }
    return decorated;
  }

}
