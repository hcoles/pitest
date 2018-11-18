package org.pitest.blockcoverage.verification;

import static java.util.Arrays.asList;
import static org.pitest.mutationtest.DetectionStatus.NO_COVERAGE;
import static org.pitest.mutationtest.DetectionStatus.SURVIVED;

import com.example.blockcoverage.HasExceptionsTest;
import com.example.blockcoverage.HasFinallyTest;
import com.example.blockcoverage.HasTernaryTest;
import org.junit.Assert;
import org.junit.Test;
import org.pitest.mutationtest.ReportTestBase;

public class VerifyBlockCoverageIT extends ReportTestBase {

  @Test
  public void shouldNotRunMutantsOnCoveredLineButNotCovered() {
    this.data.setTargetTests(predicateFor(HasTernaryTest.class));
    this.data.setTargetClasses(asList("com.example.blockcoverage.HasTernaryTestee"));
    setMutators("INCREMENTS");
    createAndRun();
    verifyResults(SURVIVED, NO_COVERAGE);
  }

  @Test
  public void shouldNotRunMutantsOnLinesUncoveredByExceptions(){
    this.data.setTargetTests(predicateFor(HasExceptionsTest.class));
    this.data.setTargetClasses(asList("com.example.blockcoverage.HasExceptionsTestee"));
    setMutators("INCREMENTS");
    createAndRun();
    verifyResults(NO_COVERAGE);
  }

  @Test
  public void shouldDetectInlinedFinallyCodeAndOnlyMakeOneMutant(){
    this.data.setTargetTests(predicateFor(HasFinallyTest.class));
    this.data.setTargetClasses(asList("com.example.blockcoverage.HasFinallyTestee"));
    this.data.setDetectInlinedCode(true);
    setMutators("INCREMENTS");
    createAndRun();

    //Check that exactly 2 mutants were created and that BOTH tests were run on both
    //(even though the tests hit on different instructions)
    Assert.assertEquals(4, this.metaDataExtractor.getNumberOfTestsRun());
    verifyResults(SURVIVED, SURVIVED);
  }
}
