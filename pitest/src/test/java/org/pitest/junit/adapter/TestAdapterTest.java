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

import org.jmock.MockObjectTestCase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.extension.ResultCollector;
import org.pitest.internal.IsolationUtils;

public class TestAdapterTest {

  @Mock
  ResultCollector rc;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  private static class HideFromJUnit {

    public static class JMockTest extends MockObjectTestCase {
      org.jmock.Mock mock;
      Runnable       r;

      public JMockTest(final String name) {
        super(name);
      }

      @Override
      public void setUp() {
        this.mock = mock(Runnable.class);
        this.mock.expects(once()).method("run");
        this.r = (Runnable) this.mock.proxy();
      }

      @SuppressWarnings("unused")
      public void testFails() {

      }

      @SuppressWarnings("unused")
      public void testPasses() {
        this.r.run();
      }
    }
  }

  @Test
  public void shouldCallNotifyStart() {
    final TestAdapter testee = new TestAdapter(new HideFromJUnit.JMockTest(
        "testPasses"));
    testee.execute(IsolationUtils.getContextClassLoader(), this.rc);
    verify(this.rc).notifyStart(testee.getDescription());
  }

  @Test
  public void shouldCallNotifyEndOnSuccess() {
    final TestAdapter testee = new TestAdapter(new HideFromJUnit.JMockTest(
        "testPasses"));
    testee.execute(IsolationUtils.getContextClassLoader(), this.rc);
    verify(this.rc).notifyEnd(testee.getDescription());
  }

  @Test
  public void shouldCallNotifyEndWithThrowableOnFailure() {
    final TestAdapter testee = new TestAdapter(new HideFromJUnit.JMockTest(
        "testFails"));
    testee.execute(IsolationUtils.getContextClassLoader(), this.rc);
    verify(this.rc)
        .notifyEnd(eq(testee.getDescription()), any(Throwable.class));
  }

  @Test
  public void shouldSerializeAndDeserializeWhenAdaptingAJMockTest() {
    final TestAdapter testee = new TestAdapter(new HideFromJUnit.JMockTest(
        "testPasses"));
    final String serialized = IsolationUtils.toTransportString(testee);
    final TestAdapter actual = (TestAdapter) IsolationUtils
        .fromTransportString(serialized);
    actual.execute(IsolationUtils.getContextClassLoader(), this.rc);
    verify(this.rc).notifyEnd(actual.getDescription());
  }

}
