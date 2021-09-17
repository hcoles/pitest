package org.pitest.java7.verification;

import static org.pitest.mutationtest.DetectionStatus.KILLED;
import static java.util.Arrays.asList;

import org.junit.Test;
import org.pitest.mutationtest.ReportTestBase;
import org.pitest.util.Verbosity;

public class VerifyJava7IT extends ReportTestBase {

  @Test
  public void shouldMutateIfElseOnAStringEqualityWithREMOVE_CONDITIONALS() {
    setMutators("REMOVE_CONDITIONALS");
    this.data.setTargetClasses(
        asList("com.example.java7.HasIfOnAStringEqualityTestee*"));
    this.data.setVerbosity(Verbosity.VERBOSE);
    createAndRun();
    verifyResults(KILLED, KILLED, KILLED, KILLED, KILLED, KILLED);
  }

  @Test
  public void shouldMutateIfElseOnAStringEqualityWithREMOVE_CONDITIONALSWhenInsideSwitchOnString() {
    setMutators("REMOVE_CONDITIONALS");
    this.data.setTargetClasses(asList(
        "com.example.java7.HasIfOnAStringEqualityInsideSwitchTestee*"));
    this.data.setVerbosity(Verbosity.VERBOSE);
    createAndRun();
    verifyResults(KILLED, KILLED);
  }

  @Test
  public void shouldNotMutateSwitchOnAStringWithREMOVE_CONDITIONALS() {
    setMutators("REMOVE_CONDITIONALS");
    this.data.setTargetClasses(
        asList("com.example.java7.HasSwitchOnStringTestee*"));
    this.data.setVerbosity(Verbosity.VERBOSE);
    createAndRun();
    verifyResults();
  }
}
