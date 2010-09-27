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

import static org.pitest.util.Unchecked.translateCheckedException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.util.ClassLoaderRepository;
import org.pitest.Description;
import org.pitest.extension.ResultCollector;
import org.pitest.extension.TestUnit;
import org.pitest.functional.SideEffect1;
import org.pitest.internal.ClassPath;
import org.pitest.internal.IsolationUtils;
import org.pitest.util.HotSwap;
import org.pitest.util.JavaProcess;

import com.reeltwo.jumble.mutation.Mutater;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.VMStartException;
import com.thoughtworks.xstream.XStream;

public class MutationTestUnitWorker {
  
  private static final Logger  logger = Logger.getLogger(MutationTestUnitWorker.class
      .getName());
  
  static class ResultParser implements SideEffect1<String> {

    private CountDownLatch latch = new CountDownLatch(1);
    private boolean        testsReportedFailureOrError;

    public void apply(final String a) {
      System.out.println("(parent) The response was " + a);
      final boolean result = a.contains("true");

      System.out.println("(parent) We think that means " + result);
      this.testsReportedFailureOrError = result;
      this.latch.countDown();
    }

    public boolean mutationDetected() {
      return this.testsReportedFailureOrError;
    }

    public void setTestsFailed(final boolean testsFailed) {
      this.testsReportedFailureOrError = testsFailed;
    }

    public CountDownLatch getLatch() {
      return this.latch;
    }

    public CountDownLatch resetLatch() {
      this.latch = new CountDownLatch(1);
      return this.latch;
    }

  }

  
  private final ResultCollector rc;
  private final List<TestUnit> tests;
  private final Class<?>       classToMutate;
  private final MutationConfig config;
  private final Description description;


  public MutationTestUnitWorker(ResultCollector rc, final List<TestUnit> tests, final Class<?> classToMutate,
      final MutationConfig mutationConfig, Description description) {
    this.rc = rc;
    this.classToMutate = classToMutate;
    this.tests = tests;
    this.config = mutationConfig;
    this.description = description;
  }


  public void runTests(ClassLoader loader) {
    final Mutater m = this.config.createMutator();
    m.setRepository(new ClassLoaderRepository(loader));
    final String name = this.classToMutate.getName();
    final int mutationCount = m.countMutationPoints(name);

    try {
      if (mutationCount > 0) {
        performMutationTesting(rc, m, name, mutationCount, loader);
      } else {
        logger.info("Skipping test " + this.description
            + " as no mutations found");
        rc.notifySkipped(this.description);
      }
    } catch (final Exception ex) {
      throw translateCheckedException(ex);
    }
    
  }
  
  
  private void performMutationTesting(final ResultCollector rc,
      final Mutater m, final String name, final int mutationCount,
      final ClassLoader loader) throws IOException,
      IllegalConnectorArgumentsException, VMStartException,
      InterruptedException, ClassNotFoundException, Exception {

    final String testXML = listToXML(tests);

    final HotSwap hs = new HotSwap();
    final ResultParser rp = new ResultParser();

    final List<AssertionError> failures = new ArrayList<AssertionError>();

    final JavaProcess p = hs.launchVM(FastRunner.class, rp, "" + mutationCount);

    final CountDownLatch beforeUnmutatedTestLatch = hs.setBreakPoint(
        FastRunner.class, "pauseBeforeUnmutatedTest");
    CountDownLatch beforeMutatedTestLatch = hs.setBreakPoint(FastRunner.class,
        "pauseBeforeMutatedTestRun");
    hs.resume();

    final PrintWriter stdIn = new PrintWriter(p.stdIn());
    stdIn.append(IsolationUtils.toXml(ClassPath.createFrom(loader)).replace(
        "\n", "")
        + "\n");
    stdIn.append(testXML + "\n");
    stdIn.flush();

    final long t0 = System.currentTimeMillis();
    beforeUnmutatedTestLatch.await();
    hs.resume(); // start test run
    rp.latch.await(); // await end of test run
    final long unmutatedRunTime = System.currentTimeMillis() - t0;

    if (rp.testsReportedFailureOrError) {
      System.out.println("\nDid not run cleanly");
      final Throwable ae = new Exception(
          "Tests do not run green with no mutation");
      rc.notifyEnd(this.description, ae);
    } else {
      System.out.println("\nRun unmutated test suite ok");

      final long maxAllowedDuration = (unmutatedRunTime + 20) * 2;
      beforeMutatedTestLatch = testMutations(m, name, mutationCount, hs, rp,
          failures, beforeMutatedTestLatch, maxAllowedDuration);

      reportResults(mutationCount, failures, rc);

    }
  }

  private CountDownLatch testMutations(final Mutater m, final String name,
      final int mutationCount, final HotSwap hs, final ResultParser rp,
      final List<AssertionError> failures,
      CountDownLatch beforeMutatedTestLatch,
      final long maxAllowedDurationInMiliSecs) throws ClassNotFoundException,
      InterruptedException, Exception {
    for (int i = 0; i != mutationCount; i++) {

      rp.resetLatch();

      m.setMutationPoint(i);
      final JavaClass activeMutation = m.jumbler(name);

      final MutationDetails details = new MutationDetails(activeMutation
          .getClassName(), activeMutation.getFileName(), m.getModification(), m
          .getMutatedMethodName(this.classToMutate.getName()));

      beforeMutatedTestLatch.await();
      hs.replace(activeMutation.getBytes(), name);
      beforeMutatedTestLatch = hs.setBreakPoint(FastRunner.class,
          "pauseBeforeMutatedTestRun");

      hs.resume(); // start test run

      try {
        rp.latch.await(maxAllowedDurationInMiliSecs, TimeUnit.MILLISECONDS); // await
                                                                             // end
                                                                             // of
                                                                             // test
                                                                             // run

        if (!rp.testsReportedFailureOrError) {
          System.out.println("\nMutation not detected");
          final AssertionError ae = createAssertionError(details);
          failures.add(ae);
        } else {
          System.out.println("\nMutation detected");
        }

      } catch (final InterruptedException ex) {
        System.out.println("Infinite loop detected");
        hs.killProcess();
      }

    }
    return beforeMutatedTestLatch;
  }

 // move into testunit and can then ger rid of config form state and replace wiht the mutator
  private void reportResults(final int mutationCount,
      final List<AssertionError> failures, final ResultCollector rc) {
    final float percentageDetected = 100f - ((failures.size() / (float) mutationCount) * 100f);
    if (percentageDetected < this.config.getThreshold()) {

      final AssertionError ae = new AssertionError("Tests detected "
          + percentageDetected + "% of " + mutationCount
          + " mutations. Threshold was " + this.config.getThreshold());
      AssertionError last = ae;
      for (final AssertionError each : failures) {
        last.initCause(each);
        last = each;
      }
      rc.notifyEnd(this.description, ae);
    } else {
      rc.notifyEnd(this.description);
    }

  }

  private AssertionError createAssertionError(final MutationDetails md) {
    final AssertionError ae = new AssertionError("The mutation -> " + md
        + " did not result in any test failures");
    final StackTraceElement[] stackTrace = { md.stackTraceDescription() };
    ae.setStackTrace(stackTrace);
    return ae;
  }


  private String listToXML(final List<TestUnit> tus) {
    try {
      final XStream xstream = new XStream();

      return xstream.toXML(tus).replace("\n", "");

    } catch (final Exception ex) {
      throw translateCheckedException(ex);
    }
  }

}
