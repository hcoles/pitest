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
package org.pitest.execute.containers;

import java.util.concurrent.BlockingQueue;

import org.pitest.testapi.Description;
import org.pitest.testapi.MetaData;
import org.pitest.testapi.ResultCollector;
import org.pitest.testapi.TestResult;
import org.pitest.testapi.TestUnitState;
import org.pitest.util.Unchecked;

public final class ConcreteResultCollector implements ResultCollector {

  private final BlockingQueue<TestResult> feedback;

  public ConcreteResultCollector(final BlockingQueue<TestResult> feedback) {
    this.feedback = feedback;
  }

  public void notifyStart(final Description tu) {
    put(new TestResult(tu, null, TestUnitState.STARTED));
  }

  public void notifySkipped(final Description tu) {
    put(new TestResult(tu, null, TestUnitState.NOT_RUN));
  }

  public void notifyEnd(final Description description, final Throwable t,
      final MetaData... data) {
    if ((data != null) && (data.length > 0)) {
      put(new ExtendedTestResult(description, t, data));
    } else {
      put(new TestResult(description, t));
    }
  }

  public void notifyEnd(final Description description, final MetaData... data) {
    if ((data != null) && (data.length > 0)) {
      put(new ExtendedTestResult(description, null, data));
    } else {
      put(new TestResult(description, null));
    }
  }

  private void put(final TestResult tr) {
    try {
      this.feedback.put(tr);
    } catch (final InterruptedException e) {
      Unchecked.translateCheckedException(e);
    }

  }

  public boolean shouldExit() {
    return false;
  }

}
