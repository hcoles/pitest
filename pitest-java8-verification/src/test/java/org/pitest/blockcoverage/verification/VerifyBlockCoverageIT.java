package org.pitest.blockcoverage.verification;

import static java.util.Arrays.asList;
import static org.pitest.mutationtest.DetectionStatus.KILLED;
import static org.pitest.mutationtest.DetectionStatus.NO_COVERAGE;
import static org.pitest.mutationtest.DetectionStatus.SURVIVED;

import com.example.blockcoverage.HasExceptionsTest;
import com.example.blockcoverage.HasTernaryTest;
import com.example.java8.Java8ClassTest;
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
}
