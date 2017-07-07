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
import static org.pitest.mutationtest.DetectionStatus.KILLED;
import static org.pitest.mutationtest.DetectionStatus.NO_COVERAGE;
import static org.pitest.mutationtest.DetectionStatus.RUN_ERROR;
import static org.pitest.mutationtest.DetectionStatus.SURVIVED;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pitest.SystemTest;
import org.pitest.classpath.ClassPath;
import org.pitest.help.PitHelpError;
import org.pitest.mutationtest.engine.gregor.Generated;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.testng.TestNGConfiguration;
import org.pitest.util.FileUtil;
import org.pitest.util.IsolationUtils;

import com.example.BeforeAfterClassTest;
import com.example.CoveredByABeforeAfterClassTest;
import com.example.CoveredByEasyMock;
import com.example.CoveredByJUnitThreeSuite;
import com.example.CrashesJVMWhenMutated;
import com.example.FailsTestWhenEnvVariableSetTestee;
import com.example.FullyCoveredTestee;
import com.example.FullyCoveredTesteeTest;
import com.example.HasMutationInFinallyBlockNonTest;
import com.example.HasMutationInFinallyBlockTest;
import com.example.HasMutationsInFinallyBlock;
import com.example.JUnitThreeSuite;
import com.example.KeepAliveThread;
import com.example.MultipleMutations;

@Category(SystemTest.class)
public class MutationCoverageReportSystemTest extends ReportTestBase {

  private static final int ONE_MINUTE = 60000;

  @Test
  public void shouldPickRelevantTestsAndKillMutationsBasedOnCoverageData() {
    this.data.setTargetClasses(predicateFor("com.example.FullyCovered*"));
    this.data.setVerbose(true);
    createAndRun();
    verifyResults(KILLED);
  }

  @Test
  public void shouldPickRelevantTestsAndKillMutationsBasedOnCoverageDataWhenLimitedByClassReach() {
    this.data.setDependencyAnalysisMaxDistance(2);
    this.data.setTargetTests(predicateFor("com.example.*FullyCovered*"));
    this.data.setTargetClasses(predicateFor("com.example.FullyCovered*"));
    createAndRun();
    verifyResults(KILLED);
  }

  @Test
  public void shouldReportUnCoveredMutations() {
    this.data.setTargetClasses(predicateFor("com.example.PartiallyCovered*"));
    createAndRun();
    verifyResults(KILLED, NO_COVERAGE);
  }

  @Test
  public void shouldReportSurvivingMutations() {
    this.data
    .setTargetClasses(predicateFor("com.example.CoveredButOnlyPartiallyTested*"));
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

  @Test(expected = PitHelpError.class)
  public void shouldFailRunWithHelpfulMessageIfTestsNotGreen() {
    setMutators("MATH");
    this.data
    .setTargetClasses(predicateFor("com.example.FailsTestWhenEnvVariableSet*"));
    this.data.addChildJVMArgs(Arrays.asList("-D"
        + FailsTestWhenEnvVariableSetTestee.class.getName() + "=true"));
    createAndRun();
    // should not get here
  }

  @Test
  public void shouldOnlyRunTestsMathchingSuppliedFilter() {
    this.data.setMutateStaticInitializers(true);
    this.data
    .setTargetClasses(predicateFor(com.example.HasMutableStaticInitializer.class));
    this.data
    .setTargetTests(predicateFor(com.example.HasMutableStaticInitializerTest.class));
    createAndRun();
    verifyResults(KILLED);
  }

  @Test
  public void shouldLoadResoucesOffClassPathFromFolderWithSpaces() {
    setMutators("RETURN_VALS");
    this.data
    .setTargetClasses(predicateFor("com.example.LoadsResourcesFromClassPath*"));
    createAndRun();
    verifyResults(KILLED);
  }

  @Test
  public void shouldPickRelevantTestsFromSuppliedTestSuites() {
    this.data.setTargetClasses(predicateFor("com.example.FullyCovered*"));
    this.data
    .setTargetTests(predicateFor(com.example.SuiteForFullyCovered.class));
    createAndRun();
    verifyResults(KILLED);
  }

  @Test
  public void shouldNotMutateMethodsMatchingExclusionPredicate() {
    this.data.setTargetClasses(predicateFor("com.example.HasExcludedMethods*"));
    this.data.setExcludedMethods(predicateFor("excludeMe"));
    createAndRun();
    verifyResults();
  }

  @Test
  public void shouldLimitNumberOfMutationsPerClass() {
    this.data.setTargetClasses(predicateFor(MultipleMutations.class));
    this.data
    .setTargetTests(predicateFor(com.example.FullyCoveredTesteeTest.class));
    this.data.setFeatures(Collections.singletonList("+CLASSLIMIT(limit[1])"));
    createAndRun();
    verifyResults(NO_COVERAGE);
  }

  @Test
  public void shouldWorkWithEasyMock() {
    this.data.setTargetClasses(predicateFor(CoveredByEasyMock.class));
    this.data.setTargetTests(predicateFor(com.example.EasyMockTest.class));
    createAndRun();
    verifyResults(KILLED, KILLED, KILLED);
  }

  @Test
  public void shouldWorkWithMockitoJUnitRunner() {
    this.data.setTargetClasses(predicateFor("com.example.MockitoCallFoo"));
    this.data.setTargetTests(predicateFor(com.example.MockitoRunnerTest.class));
    this.data.setVerbose(true);
    createAndRun();
    verifyResults(KILLED);
  }

  @Test(expected = PitHelpError.class)
  public void shouldReportHelpfulErrorIfNoMutationsFounds() {
    this.data.setFailWhenNoMutations(true);
    this.data.setTargetClasses(predicateFor("foo"));
    createAndRun();
  }

  @Test
  public void shouldExcludeFilteredTests() {
    this.data.setTargetTests(predicateFor("com.example.*FullyCoveredTestee*"));
    this.data.setTargetClasses(predicateFor("com.example.FullyCovered*"));
    this.data.setExcludedClasses(predicateFor(FullyCoveredTesteeTest.class));
    createAndRun();
    verifyResults(NO_COVERAGE);
  }

  @Test
  public void willAllowExcludedClassesToBeReIncludedViaSuite() {
    this.data
    .setTargetTests(predicateFor("com.example.*SuiteForFullyCovered*"));
    this.data.setTargetClasses(predicateFor("com.example.FullyCovered*"));
    this.data.setExcludedClasses(predicateFor(FullyCoveredTesteeTest.class));
    createAndRun();
    verifyResults(KILLED);
  }

  @Test(expected = PitHelpError.class)
  public void shouldExcludeFilteredClasses() {
    this.data.setFailWhenNoMutations(true);
    this.data.setTargetClasses(predicateFor(FullyCoveredTestee.class));
    this.data.setExcludedClasses(predicateFor(FullyCoveredTestee.class));
    createAndRun();
  }

  @Test
  public void shouldMutateClassesSuppliedToAlternateClassPath()
      throws IOException {
    // yes, this is horrid
    final String location = FileUtil.randomFilename() + ".jar";
    try {
      final FileOutputStream fos = new FileOutputStream(location);
      final InputStream stream = IsolationUtils.getContextClassLoader()
          .getResourceAsStream("outofcp.jar");
      copy(stream, fos);
      fos.close();

      this.data.setTargetClasses(predicateFor("com.outofclasspath.*Mutee*"));
      this.data.setTargetTests(predicateFor("com.outofclasspath.*"));

      List<String> cp = new ArrayList<String>();
      cp.addAll(ClassPath.getClassPathElementsAsPaths());
      cp.add(location);

      this.data.setClassPathElements(cp);
      this.data.setDependencyAnalysisMaxDistance(-1);
      this.data.setExcludedClasses(predicateFor("*Power*", "*JMockit*"));
      createAndRun();
      verifyResults(KILLED);
    } finally {
      new File(location).delete();
    }
  }

  @Test
  public void shouldSupportTestNG() {
    this.data
    .setTargetClasses(predicateFor("com.example.testng.FullyCovered*"));
    this.data.setVerbose(true);
    createAndRun(new TestNGConfiguration(new TestGroupConfig(
        Collections.<String> emptyList(), Collections.<String> emptyList())));
    verifyResults(KILLED);
  }

  @Test(timeout = ONE_MINUTE)
  public void shouldTerminateWhenThreadpoolCreated() {
    this.data.setTargetClasses(predicateFor(KeepAliveThread.class));
    this.data
    .setTargetTests(predicateFor(com.example.KeepAliveThreadTest.class));
    createAndRun();
    verifyResults(SURVIVED);
  }

  @Test
  public void shouldMarkChildJVMCrashesAsRunErrors() {
    setMutators("NEGATE_CONDITIONALS");
    this.data.setTargetClasses(predicateFor(CrashesJVMWhenMutated.class));
    this.data
    .setTargetTests(predicateFor(com.example.TestCrashesJVMWhenMutated.class));
    createAndRun();

    verifyResults(RUN_ERROR);

  }

  @Test
  public void shouldCombineAndKillInlinedMutationsInFinallyBlocks() {
    setMutators("INCREMENTS");
    this.data.setTargetClasses(predicateFor(HasMutationsInFinallyBlock.class));
    this.data.setTargetTests(predicateFor(HasMutationInFinallyBlockTest.class));
    this.data.setDetectInlinedCode(true);
    createAndRun();

    verifyResults(KILLED);
  }

  @Test
  public void shouldUseTestsDefinedInASuppliedJUnitThreeSuite() {
    setMutators("RETURN_VALS");
    this.data.setTargetClasses(predicateFor(CoveredByJUnitThreeSuite.class));
    this.data.setTargetTests(predicateFor(JUnitThreeSuite.class));
    this.data.setVerbose(true);
    createAndRun();

    verifyResults(KILLED);
  }

  @Test
  public void shouldReportCombinedCoveredButNotTestedMutationsInFinallyBlocksAsSurvived() {
    setMutators("INCREMENTS");
    this.data.setTargetClasses(predicateFor(HasMutationsInFinallyBlock.class));
    this.data
    .setTargetTests(predicateFor(HasMutationInFinallyBlockNonTest.class));
    this.data.setDetectInlinedCode(true);
    createAndRun();

    verifyResults(SURVIVED);
  }

  @Test
  public void shouldExitAfterFirstFailureWhenTestClassAnnotatedWithBeforeClass() {
    setMutators("RETURN_VALS");
    this.data
    .setTargetClasses(predicateFor(CoveredByABeforeAfterClassTest.class));
    this.data.setTargetTests(predicateFor(BeforeAfterClassTest.class));

    createAndRun();

    verifyResults(KILLED);
    assertEquals(1, this.metaDataExtractor.getNumberOfTestsRun());
  }

  @Test
  public void shouldKillMutationsWhenMutationsPreventsConstructionOfTestClass() {
    setMutators("RETURN_VALS");

    this.data
    .setTargetClasses(predicateFor(com.example.mutatablecodeintest.Mutee.class));
    this.data
    .setTargetTests(predicateFor(com.example.mutatablecodeintest.MuteeTest.class));

    createAndRun();

    verifyResults(KILLED);
  }

  @Test
  public void shouldKillMutationsWhenKillingTestClassContainsAnIgnoreOnAnotherMethod() {
    setMutators("RETURN_VALS");

    this.data
    .setTargetClasses(predicateFor(com.example.testhasignores.Mutee.class));
    this.data
    .setTargetTests(predicateFor(com.example.testhasignores.MuteeTest.class));

    createAndRun();

    verifyResults(KILLED);
  }

  
  @Test
  public void shouldNotMutateStaticMethodsOnlyCalledFromInitializer() {
    setMutators("VOID_METHOD_CALLS");
    
    this.data
    .setTargetClasses(predicateFor(com.example.staticinitializers.MethodsCalledOnlyFromInitializer.class));

    this.data.setMutateStaticInitializers(false);
    
    createAndRun();

    verifyResults();
  }
  
  @Test
  public void willMutateStaticMethodsCalledFromInitializerAndElsewhere() {
    setMutators("VOID_METHOD_CALLS");
    
    this.data
    .setTargetClasses(predicateFor(com.example.staticinitializers.MethodsCalledFromInitializerAndElseWhere.class));
    this.data.setMutateStaticInitializers(false);
    
    createAndRun();

    // would prefer NO_COVERAGE here
    verifyResults();
  }
  
  @Test
  public void shouldMutateNonPrivateStaticMethodsCalledFromInitializerOnly() {
    setMutators("VOID_METHOD_CALLS");
    
    this.data
    .setTargetClasses(predicateFor(com.example.staticinitializers.NonPrivateMethodsCalledFromInitializerOnly.class));
    this.data.setMutateStaticInitializers(false);
    
    createAndRun();

    verifyResults(NO_COVERAGE,NO_COVERAGE,NO_COVERAGE);
  }
  
  @Test
  public void willMutatePriveMethodsCalledInChainFromInitializer() {
    setMutators("VOID_METHOD_CALLS");
    
    this.data
    .setTargetClasses(predicateFor(com.example.staticinitializers.MethodsCalledInChainFromStaticInitializer.class));
    this.data.setMutateStaticInitializers(false);
    
    createAndRun();

    // would prefer removed here
    verifyResults(NO_COVERAGE);
  }
  
  @Test
  public void shouldNotMutateClassesAnnotatedWithGenerated() {
    setMutators("RETURN_VALS");
    this.data
    .setTargetClasses(predicateFor(AnnotatedToAvoidAtClassLevel.class));
    
    createAndRun();

    verifyResults();
  }
    
  @Generated
  public static class AnnotatedToAvoidAtClassLevel {
    public int mutateMe() {
      return 42;
    }
  }
  
  
  private static void copy(final InputStream in, final OutputStream out)
      throws IOException {
    // Read bytes and write to destination until eof

    final byte[] buf = new byte[1024];
    int len = 0;
    while ((len = in.read(buf)) >= 0) {
      out.write(buf, 0, len);
    }
  }

}
