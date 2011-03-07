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

import static org.pitest.mutationtest.instrument.ResultsReader.DetectionStatus.KILLED;
import static org.pitest.mutationtest.instrument.ResultsReader.DetectionStatus.SURVIVED;

import java.util.Collections;

import org.junit.Test;
import org.pitest.PitError;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.instrument.JavaAgentJarFinder;

import com.example.FailsTestWhenEnvVariableSetTestee;

public class CodeCentricReportTest extends ReportTestBase {

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

  @Test(expected = PitError.class)
  public void shouldFailRunIfTestsNotGreen() {
    this.data.setMutators(Collections
        .<MethodMutatorFactory> singletonList(Mutator.MATH));
    this.data
        .setTargetClasses(predicateFor("com.example.FailsTestWhenEnvVariableSet*"));
    try {
      System.setProperty(FailsTestWhenEnvVariableSetTestee.class.getName(),
          "true");
      createAndRun();
    } finally {
      System.setProperty(FailsTestWhenEnvVariableSetTestee.class.getName(),
          "false");
    }
    // should not get here
  }

  @Test
  public void shouldOnlyRunTestsMathchingSuppliedFilter() {
    this.data.setTargetClasses(predicateFor("com.example.*"));
    this.data
        .setTargetTests(predicateFor("com.example.HasMutableStaticInitializerTest"));
    createAndRun();
    verifyResults(KILLED);
  }

  private void createAndRun() {
    final CodeCentricReport testee = new CodeCentricReport(this.data,
        new JavaAgentJarFinder(), listenerFactory(), false);

    testee.run();
  }

}
