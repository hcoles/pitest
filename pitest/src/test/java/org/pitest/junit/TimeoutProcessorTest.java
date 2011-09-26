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
package org.pitest.junit;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.pitest.Description;
import org.pitest.TestMethod;
import org.pitest.extension.ResultCollector;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.TimeoutDecorator;
import org.pitest.reflection.Reflection;
import org.pitest.testunit.AbstractTestUnit;

public class TimeoutProcessorTest {

  private TimeoutProcessor testee;

  @Before
  public void makeTestee() {
    this.testee = new TimeoutProcessor();
  }

  @Test
  public void shouldReturnUnmodifiedTestUnitIfDescriptionHasNoTestMethod() {
    final TestUnit tu = createTestUnit(null);
    assertSame(tu, this.testee.apply(tu));
  }

  private static class NoAnnotation {
    @SuppressWarnings("unused")
    public void noAnnotation() {

    }
  }

  @Test
  public void shouldReturnUnmodifiedTestUnitIfTestMethodHasNoAnnotation() {
    final TestUnit tu = createTestUnit(new TestMethod(Reflection.publicMethod(
        NoAnnotation.class, "noAnnotation")));
    assertSame(tu, this.testee.apply(tu));
  }

  private static class ZeroTimeout {
    @SuppressWarnings("unused")
    @Test
    public void noTimeOut() {

    }
  }

  @Test
  public void shouldReturnUnmodifiedTestUnitIfTestMethodHasTimeoutOfZero() {
    final TestUnit tu = createTestUnit(new TestMethod(Reflection.publicMethod(
        ZeroTimeout.class, "noTimeOut")));
    assertSame(tu, this.testee.apply(tu));
  }

  private static class Timeout {
    @SuppressWarnings("unused")
    @Test(timeout = 100)
    public void timeOut() {

    }
  }

  @Test
  public void shouldReturnTimeoutDecoratorIfTestMethodHasNonZeroTimeout() {
    final TestUnit tu = createTestUnit(new TestMethod(Reflection.publicMethod(
        Timeout.class, "timeOut")));
    assertTrue(this.testee.apply(tu) instanceof TimeoutDecorator);
  }

  private TestUnit createTestUnit(final TestMethod method) {
    final TestUnit tu = new AbstractTestUnit(new Description("foo",
        TimeoutProcessorTest.class, method)) {
      @Override
      public void execute(final ClassLoader loader, final ResultCollector rc) {

      }

    };
    return tu;
  }

}
