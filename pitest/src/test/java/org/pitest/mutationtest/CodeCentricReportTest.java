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

import static org.junit.Assert.assertEquals;
import static org.pitest.mutationtest.instrument.ResultsReader.DetectionStatus.KILLED;
import static org.pitest.mutationtest.instrument.ResultsReader.DetectionStatus.SURVIVED;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pitest.extension.TestListener;
import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.TestMutationTesting.MetaDataExtractor;
import org.pitest.mutationtest.instrument.JavaAgentJarFinder;
import org.pitest.mutationtest.instrument.ResultsReader.DetectionStatus;
import org.pitest.util.Glob;

public class CodeCentricReportTest {

  private MetaDataExtractor metaDataExtractor;
  private ReportOptions     data;

  @Before
  public void setUp() {
    this.metaDataExtractor = new MetaDataExtractor();
    this.data = new ReportOptions();
    this.data.setSourceDirs(Collections.<File> emptyList());
    this.data.setMutators(DefaultMutationConfigFactory.DEFAULT_MUTATORS);
    this.data.setClassesInScope(predicateFor("com.example.*"));
  }

  @Test
  public void shouldPickRelevantTestsAndKillMutationsBasedOnCoverageData() {
    this.data.setTargetClasses(predicateFor("com.example.FullyCovered*"));
    createAndRun();
    verifyResults(KILLED);
  }

  @Test
  public void shouldReportSurvivingMutations() {
    this.data.setTargetClasses(predicateFor("com.example.PartiallyCovered*"));
    createAndRun();
    verifyResults(KILLED, SURVIVED);
  }

  @Test
  public void shouldKillMutationsInStaticInitializersWhenThereIsCoverageAndMutateStaticFlagIsSet() {
    this.data.setMutateStaticInitializers(true);
    this.data
        .setTargetClasses(predicateFor("com.example.HasMutableStaticInitializer*"));
    createAndRun();
    verifyResults(KILLED);
  }

  @Test
  public void shouldNotCreateMutationsInStaticInitializersWhenFlagNotSet() {
    this.data.setMutateStaticInitializers(false);
    this.data
        .setTargetClasses(predicateFor("com.example.HasMutableStaticInitializer*"));
    createAndRun();
    verifyResults();
  }

  private void createAndRun() {
    final CodeCentricReport testee = new CodeCentricReport(this.data,
        new JavaAgentJarFinder(), listenerFactory(), false);

    testee.run();
  }

  private Collection<Predicate<String>> predicateFor(final String glob) {
    return Glob.toGlobPredicates(Arrays.asList(glob));
  }

  private ListenerFactory listenerFactory() {
    return new ListenerFactory() {

      public TestListener getListener(final ReportOptions data) {
        return CodeCentricReportTest.this.metaDataExtractor;
      }

    };
  }

  protected void verifyResults(final DetectionStatus... detectionStatus) {
    final List<DetectionStatus> expected = Arrays.asList(detectionStatus);
    final List<DetectionStatus> actual = this.metaDataExtractor
        .getDetectionStatus();

    Collections.sort(expected);
    Collections.sort(actual);

    assertEquals(expected, actual);
  }

}
