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
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.DefaultStaticConfig;
import org.pitest.Description;
import org.pitest.Pitest;
import org.pitest.TestResult;
import org.pitest.annotations.PITSuite;
import org.pitest.extension.Container;
import org.pitest.extension.IsolationStrategy;
import org.pitest.extension.StaticConfiguration;
import org.pitest.extension.TestListener;
import org.pitest.extension.Transformation;
import org.pitest.extension.common.AllwaysIsolateStrategy;
import org.pitest.internal.TransformingClassLoaderFactory;
import org.pitest.internal.transformation.IdentityTransformation;
import org.pitest.testutil.ConfigurationForTesting;
import org.pitest.testutil.TestAnnotationForTesting;

@RunWith(Parameterized.class)
public class TestContainersSendCorrectNotifications {

  private static interface ContainerFactory {

    Container getContainer();

  }

  private final ContainerFactory containerFactory;
  private StaticConfiguration    staticConfig;
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
        { isolatedThreadPoolContainerFactory(2) },
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

  private static Object isolatedThreadPoolContainerFactory(final int threads) {
    return new ContainerFactory() {
      public Container getContainer() {
        final IsolationStrategy i = new AllwaysIsolateStrategy();
        final Transformation t = new IdentityTransformation();
        return new BaseThreadPoolContainer(threads,
            new TransformingClassLoaderFactory(t, i), Executors
                .defaultThreadFactory());
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
    final ConfigurationForTesting c = new ConfigurationForTesting();
    this.staticConfig = new DefaultStaticConfig();
    this.staticConfig.getTestListeners().add(this.listener);
    this.pit = new Pitest(this.staticConfig, c);
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
    @PITSuite
    public static List<Class<?>> suite() {
      return Arrays.asList(OnePassingTest.class, OneFailingTest.class);
    }
  }

  public static class LargeSuite {

    public final static int SUITE_SIZE = 300;

    @PITSuite
    public static List<Class<?>> suite() {
      return Collections.<Class<?>> nCopies(SUITE_SIZE, SmallSuite.class);
    }
  }

  @Test
  public void testNotificationsReceivedForSinglePassingTest() {
    run(OnePassingTest.class);
    verify(this.listener).onTestStart(any(Description.class));
    verify(this.listener).onTestSuccess(any(TestResult.class));
  }

  @Test
  public void testNotificationsReceivedForSingleFailingTest() {
    run(OneFailingTest.class);
    verify(this.listener).onTestStart(any(Description.class));
    verify(this.listener).onTestFailure(any(TestResult.class));
  }

  @Test
  public void testNotificationsReceivedForParallizableSuite() {
    run(LargeSuite.class);
    verify(this.listener, times(LargeSuite.SUITE_SIZE * 2)).onTestStart(
        any(Description.class));
    verify(this.listener, times(LargeSuite.SUITE_SIZE)).onTestSuccess(
        any(TestResult.class));
    verify(this.listener, times(LargeSuite.SUITE_SIZE)).onTestFailure(
        any(TestResult.class));
  }

  private void run(final Class<?> clazz) {
    this.pit.run(this.containerFactory.getContainer(), clazz);
  }

}