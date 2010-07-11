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
package org.pitest.containers;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.Description;
import org.pitest.Pitest;
import org.pitest.TestResult;
import org.pitest.annotations.PITSuiteMethod;
import org.pitest.extension.Container;
import org.pitest.extension.TestListener;
import org.pitest.testutil.ConfigurationForTesting;
import org.pitest.testutil.TestAnnotationForTesting;

@RunWith(Parameterized.class)
public class TestContainersSendCorrectNotifications {

  private static interface ContainerFactory {

    Container getContainer();

  }

  private final ContainerFactory containerFactory;
  private Pitest                 pit;

  @Mock
  private TestListener           listener;

  public TestContainersSendCorrectNotifications(
      final ContainerFactory containerFactory) {
    this.containerFactory = containerFactory;
  }

  @Parameters
  public static Collection<Object[]> containers() {
    return Arrays.asList(new Object[][] { { uncontainerFactory() },
        { unisolatedThreadPoolContainerFactory(1) },
        { unisolatedThreadPoolContainerFactory(3) } });

  }

  private static Object unisolatedThreadPoolContainerFactory(final int threads) {
    return new ContainerFactory() {
      public Container getContainer() {
        return new UnisolatedThreadPoolContainer(threads);
      }

    };
  }

  private static Object uncontainerFactory() {
    return new ContainerFactory() {
      public Container getContainer() {
        return new UnContainer();
      }

    };
  }

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.pit = new Pitest(this.containerFactory.getContainer(),
        new ConfigurationForTesting());
    this.pit.addListener(this.listener);
  }

  public static class OnePassingTest {
    @TestAnnotationForTesting
    public void one() {

    }
  };

  public static class OneFailingTest {
    @TestAnnotationForTesting
    public void one() {
      throw new AssertionError();
    }
  };

  public static class SmallSuite {
    @SuppressWarnings("unchecked")
    @PITSuiteMethod
    public static List<Class<?>> suite() {
      return Arrays.asList(OnePassingTest.class, OneFailingTest.class);
    }
  }

  public static class LargeSuite {

    public static int SUITE_SIZE = 300;

    @PITSuiteMethod
    public static List<Class<?>> suite() {
      return Collections.<Class<?>> nCopies(SUITE_SIZE, SmallSuite.class);
    }
  }

  @Test
  public void testNotificationsReceivedForSinglePassingTest() {
    this.pit.run(OnePassingTest.class);
    verify(this.listener).onTestStart(any(Description.class));
    verify(this.listener).onTestSuccess(any(TestResult.class));
  }

  @Test
  public void testNotificationsReceivedForSingleFailingTest() {
    this.pit.run(OneFailingTest.class);
    verify(this.listener).onTestStart(any(Description.class));
    verify(this.listener).onTestFailure(any(TestResult.class));
  }

  @Test
  public void testNotificationsReceivedForParallizableSuite() {
    this.pit.run(LargeSuite.class);
    verify(this.listener, times(LargeSuite.SUITE_SIZE * 2)).onTestStart(
        any(Description.class));
    verify(this.listener, times(LargeSuite.SUITE_SIZE)).onTestSuccess(
        any(TestResult.class));
    verify(this.listener, times(LargeSuite.SUITE_SIZE)).onTestFailure(
        any(TestResult.class));
  }

}
