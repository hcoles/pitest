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
package org.pitest.mutationtest.hotswap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.Description;
import org.pitest.extension.ResultCollector;
import org.pitest.internal.IsolationUtils;
import org.pitest.junit.JUnitCompatibleConfiguration;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.mutationtest.Mutator;

public class HotSwapMutationTestUnitTest {

  private HotSwapMutationTestUnit testee;

  @Mock
  private ResultCollector         rc;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  public static class One {
    public static int increment() {
      int i = 0;
      i++;
      return i;
    }

    public static int incrementAgain() {
      int i = 0;
      i++;
      return i;
    }

    public static int moreIncrements() {
      int i = 0;
      i++;
      return i;
    }

  }

  // @MutationTest(threshold = 66, mutators = Mutator.INCREMENTS)
  public static class TestOne {
    @Test
    public void test() {
      final int i = One.increment();
      System.err.println("Running test 1 = " + i);
      System.err.flush();
      assertEquals(1, i);
    }

    @Test
    public void test2() {
      final int i = One.incrementAgain();
      System.err.println("Running test 2 = " + i);
      System.err.flush();
      assertEquals(1, i);
    }

    @Test
    public void test3() {

    }

    @Test
    public void test4() throws InterruptedException {

    }

  }

  @Test
  public void testReportsSuccessIfMoreThanThresholdLevelOfMutationsDetected() {
    this.testee = new HotSwapMutationTestUnit(TestOne.class, One.class,
        new MutationConfig(66, Mutator.INCREMENTS),
        new JUnitCompatibleConfiguration(), null);
    this.testee.execute(IsolationUtils.getContextClassLoader(), this.rc);
    verify(this.rc).notifyEnd(any(Description.class));
  }

  @Test
  public void testReportsFailureIfLessThanThresholdLevelOfMutationsDetected() {
    this.testee = new HotSwapMutationTestUnit(TestOne.class, One.class,
        new MutationConfig(100, Mutator.INCREMENTS),
        new JUnitCompatibleConfiguration(), null);
    this.testee.execute(IsolationUtils.getContextClassLoader(), this.rc);
    verify(this.rc).notifyEnd(any(Description.class), any(Throwable.class));
  }

  @Test
  public void testReportsSkippedIfNoMutationsDetected() {
    this.testee = new HotSwapMutationTestUnit(TestOne.class, One.class,
        new MutationConfig(66, Mutator.SWITCHES),
        new JUnitCompatibleConfiguration(), null);
    this.testee.execute(IsolationUtils.getContextClassLoader(), this.rc);
    verify(this.rc).notifySkipped(any(Description.class));
  }

  static class HideFromJUnit {
    public static class FailingTest {
      @Test
      public void fail() {
        assertTrue(false);
      }
    }
  }

  @Test
  public void testReportsFailureIfUnmutatedTestsDoNotRunGreen() {
    this.testee = new HotSwapMutationTestUnit(HideFromJUnit.FailingTest.class,
        One.class, new MutationConfig(66, Mutator.INCREMENTS),
        new JUnitCompatibleConfiguration(), null);
    this.testee.execute(IsolationUtils.getContextClassLoader(), this.rc);
    verify(this.rc)
        .notifyEnd(any(Description.class), any(AssertionError.class));
  }

  public static class SingleMutation {
    public static int increment() {
      int i = 0;
      i++;
      return i;
    }
  }

  public static class SingleMutationTest {
    @Test
    public void test() {
      System.out.println("Expect SingleMutation to return 1. Is returning "
          + SingleMutation.increment());
      assertEquals(1, SingleMutation.increment());
    }
  }

  @Test
  public void testResetsClassToUnmutatedState() {
    this.testee = new HotSwapMutationTestUnit(SingleMutationTest.class,
        SingleMutation.class, new MutationConfig(66, Mutator.INCREMENTS),
        new JUnitCompatibleConfiguration(), null);
    this.testee.execute(IsolationUtils.getContextClassLoader(), this.rc);
    // run again with same test depending on originally mutated class, but
    // mutating class One. Expect it to therefore report 1 pass and 1 fail
    System.out.println("----------------- Run Two -----------------");
    this.testee = new HotSwapMutationTestUnit(SingleMutationTest.class,
        One.class, new MutationConfig(66, Mutator.INCREMENTS),
        new JUnitCompatibleConfiguration(), null);
    this.testee.execute(IsolationUtils.getContextClassLoader(), this.rc);
    verify(this.rc).notifyEnd(any(Description.class));
    verify(this.rc)
        .notifyEnd(any(Description.class), any(AssertionError.class));
  }

}
