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
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.simpletest.ConfigurationForTesting;
import org.pitest.simpletest.TestAnnotationForTesting;
import org.pitest.testapi.Description;
import org.pitest.testapi.TestListener;
import org.pitest.testapi.TestResult;
import org.pitest.testapi.execute.Container;
import org.pitest.testapi.execute.Pitest;
import org.pitest.testapi.execute.containers.UnContainer;

@RunWith(Parameterized.class)
public class TestContainersSendCorrectNotifications {

  private static interface ContainerFactory {

    Container getContainer();

  }

  private final ContainerFactory  containerFactory;
  private Pitest                  pit;

  @Mock
  private TestListener            listener;
  private ConfigurationForTesting config;

  public TestContainersSendCorrectNotifications(
      final ContainerFactory containerFactory) {
    this.containerFactory = containerFactory;
  }

  @Parameters
  public static Collection<Object[]> containers() {
    return Arrays.asList(new Object[][] { { uncontainerFactory() } });

  }

  private static Object uncontainerFactory() {
    return new ContainerFactory() {
      @Override
      public Container getContainer() {
        return new UnContainer();
      }

    };
  }

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.config = new ConfigurationForTesting();
    this.pit = new Pitest(this.listener);
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

  @Test
  public void shouldSendCorrectNotificationsForSinglePassingTest() {
    run(OnePassingTest.class);
    verify(this.listener).onTestStart(any(Description.class));
    verify(this.listener).onTestSuccess(any(TestResult.class));
  }

  @Test
  public void shouldSendCorrectNotificationsForSingleFailingTest() {
    run(OneFailingTest.class);
    verify(this.listener).onTestStart(any(Description.class));
    verify(this.listener).onTestFailure(any(TestResult.class));
  }

  private void run(final Class<?> clazz) {
    this.pit.run(this.containerFactory.getContainer(), this.config, clazz);
  }

}