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

package org.pitest.junit.adapter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Optional;
import org.pitest.testapi.ResultCollector;

public class AdaptedJUnitTestUnitTest {

  private AdaptedJUnitTestUnit testee;

  @Mock
  ResultCollector              rc;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  private static class HideFromJUnit {

    public static class JUnit4Test {

      @Test
      public void pass() {

      }

      @Test
      public void fail() {
        org.junit.Assert.fail();
      }

    }

  }

  @Test
  public void shouldCallNotifyStartWhenExecutingInOwnClassLoader() {
    createTestee(HideFromJUnit.JUnit4Test.class,
        "pass");
    this.testee.execute(this.rc);
    verify(this.rc).notifyStart(this.testee.getDescription());
  }

  @Test
  public void shouldCallNotifyEndOnSuccessWhenExecutingInOwnClassLoader() {
    createTestee(HideFromJUnit.JUnit4Test.class,
        "pass");
    this.testee.execute(this.rc);
    verify(this.rc).notifyEnd(this.testee.getDescription());
  }

  @Test
  public void shouldCallNotifyEndWithThrowableOnFailureWhenExecutingInOwnClassLoader() {
    createTestee(HideFromJUnit.JUnit4Test.class,
        "fail");
    this.testee.execute(this.rc);
    verify(this.rc).notifyEnd(eq(this.testee.getDescription()),
        any(Throwable.class));
  }

  private void createTestee(
      final Class<?> clazz, final String method) {
    this.testee = new AdaptedJUnitTestUnit(clazz,
        createFilter(clazz, method));
  }

  private Optional<Filter> createFilter(final Class<?> clazz, final String method) {
    final Description d = Description.createTestDescription(clazz, method);
    final Filter f = new Filter() {

      @Override
      public boolean shouldRun(final Description description) {
        return d.toString().equals(description.toString());
      }

      @Override
      public String describe() {
        return null;
      }

    };
    return Optional.ofNullable(f);
  }

}
