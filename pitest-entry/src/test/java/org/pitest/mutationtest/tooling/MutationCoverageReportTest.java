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
package org.pitest.mutationtest.tooling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pitest.mutationtest.LocationMother.aLocation;
import static org.pitest.mutationtest.LocationMother.aMutationId;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassIdentifier;
import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassInfoMother;
import org.pitest.classinfo.ClassName;
import org.pitest.classinfo.HierarchicalClassId;
import org.pitest.classpath.CodeSource;
import org.pitest.coverage.ClassLines;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.coverage.CoverageGenerator;
import org.pitest.help.Help;
import org.pitest.help.PitHelpError;
import org.pitest.mutationtest.History;
import org.pitest.mutationtest.EngineArguments;
import org.pitest.mutationtest.ListenerArguments;
import org.pitest.mutationtest.MutationEngineFactory;
import org.pitest.mutationtest.MutationResultListener;
import org.pitest.mutationtest.MutationResultListenerFactory;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.config.SettingsFactory;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationDetailsMother;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.engine.gregor.config.GregorEngineFactory;
import org.pitest.mutationtest.verify.BuildVerifier;
import org.pitest.util.ResultOutputStrategy;
import org.pitest.util.Timings;
import org.pitest.util.Unchecked;

public class MutationCoverageReportTest {

  private MutationCoverage              testee;

  private ReportOptions                 data;

  @Mock
  private MutationResultListenerFactory listenerFactory;

  @Mock
  private MutationResultListener        listener;

  @Mock
  private CoverageDatabase              coverageDb;

  @Mock
  private CoverageGenerator             coverage;

  @Mock
  private CodeSource                    code;

  @Mock
  private History history;

  @Mock
  private MutationEngineFactory         mutationFactory;

  @Mock
  private BuildVerifier                 verifier;

  @Mock
  private MutationEngine                engine;

  @Mock
  private Mutater                       mutater;

  @Mock
  private ResultOutputStrategy          output;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    this.data = new ReportOptions();
    this.data.setSourceDirs(Collections.emptyList());
    when(this.coverage.calculateCoverage(any(Predicate.class))).thenReturn(this.coverageDb);
    when(
        this.listenerFactory.getListener(any(),
            any(ListenerArguments.class))).thenReturn(this.listener);
    when(history.limitTests()).thenReturn(c -> true);
    mockMutationEngine();
  }

  private void mockMutationEngine() {
    when(
        this.mutationFactory.createEngine(any(EngineArguments.class))).thenReturn(
                this.engine);
    when(this.engine.createMutator(any(ClassByteArraySource.class)))
    .thenReturn(this.mutater);
  }

  @Test
  public void shouldReportErrorWhenNoMutationsFoundAndFlagSet() {
    try {
      this.data.setFailWhenNoMutations(true);
      createAndRunTestee();
    } catch (final PitHelpError phe) {
      assertEquals(Help.NO_MUTATIONS_FOUND.toString(), phe.getMessage());
    }
  }

  @Test
  public void shouldNotReportErrorWhenNoMutationsFoundAndFlagNotSet() {
    try {
      this.data.setFailWhenNoMutations(false);
      createAndRunTestee();
    } catch (final PitHelpError phe) {
      fail();
    }
  }

  @Test
  public void shouldRecordClassPath() {

    final ClassName clazz = ClassName.fromClass(Foo.class);

    final HierarchicalClassId fooId = new HierarchicalClassId(
        new ClassIdentifier(0, clazz), "0");
    final ClassInfo foo = ClassInfoMother.make(fooId.getId());

    when(this.mutater.findMutations(ClassName.fromClass(Foo.class))).thenReturn(aMutantIn(Foo.class));

    when(this.code.getCodeUnderTestNames()).thenReturn(
        Collections.singleton(clazz));
    when(this.code.fetchClassHashes(anyCollection())).thenReturn(
        Collections.singletonList(foo));
    when(this.coverageDb.getCodeLinesForClass(clazz)).thenReturn(new ClassLines(clazz, Collections.emptySet()));

    createAndRunTestee();

    verify(this.history).processCoverage(this.coverageDb);
  }

  @Test
  public void shouldCheckBuildSuitableForMutationTesting() {
    createAndRunTestee();
    verify(this.verifier).verifyBuild();
  }

  @Test
  public void shouldReportNoMutationsFoundWhenNoneDetected() {
    this.data.setFailWhenNoMutations(false);
    final CombinedStatistics actual = createAndRunTestee();
    assertEquals(0, actual.getMutationStatistics().getTotalMutations());
  }

  @Test
  public void shouldNotRunCoverageWhenNoMutationsFound() {
    this.data.setFailWhenNoMutations(false);
    createAndRunTestee();
    verify(coverage, never()).calculateCoverage(any(Predicate.class));
  }

  @Test
  public void shouldNotInitializeHistoryWhenNoMutationsFound() {
    this.data.setFailWhenNoMutations(false);
    createAndRunTestee();
    verify(history, never()).initialize();
  }

  @Test
  public void shouldReportMutationsFoundWhenSomeDetected() {
    this.data.setFailWhenNoMutations(false);
    final ClassName foo = ClassName.fromClass(Foo.class);
    when(this.mutater.findMutations(foo)).thenReturn(aMutantIn(Foo.class));
    when(this.code.getCodeUnderTestNames()).thenReturn(
        Collections.singleton(foo));
    when(this.coverageDb.getCodeLinesForClass(foo)).thenReturn(new ClassLines(foo, Collections.emptySet()));
    final CombinedStatistics actual = createAndRunTestee();
    assertEquals(1, actual.getMutationStatistics().getTotalMutations());
  }


  private List<MutationDetails> aMutantIn(Class<Foo> clazz) {
    return MutationDetailsMother.aMutationDetail()
            .withId(aMutationId().withLocation(aLocation()
                    .withClass(ClassName.fromClass(clazz))
                    .withMethod("method")
                    .withMethodDescription("()I")))
            .build(1);
  }

  private CombinedStatistics createAndRunTestee() {
    final MutationStrategies strategies = new MutationStrategies(
        new GregorEngineFactory(), this.history, this.coverage,
        this.listenerFactory, result -> result, cov -> cov, this.output, this.verifier).with(this.mutationFactory);

    this.testee = new MutationCoverage(strategies, null, this.code, this.data,
        new SettingsFactory(this.data, PluginServices.makeForContextLoader()),
        new Timings());
    try {
      return this.testee.runReport();
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

}

class Foo {

  int method() {
    return 1;
  }
}
