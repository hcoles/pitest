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
package org.pitest.testapi.execute.containers;

import java.util.Collection;

import org.pitest.testapi.Description;
import org.pitest.testapi.ResultCollector;
import org.pitest.testapi.TestResult;
import org.pitest.testapi.TestUnitState;

public final class ConcreteResultCollector implements ResultCollector {

  private final Collection<TestResult> feedback;

  public ConcreteResultCollector(final Collection<TestResult> feedback) {
    this.feedback = feedback;
  }

  @Override
  public void notifyStart(final Description tu) {
    put(new TestResult(tu, null, TestUnitState.STARTED));
  }

  @Override
  public void notifySkipped(final Description tu) {
    put(new TestResult(tu, null, TestUnitState.NOT_RUN));
  }

  @Override
  public void notifyEnd(final Description description, final Throwable t) {
    put(new TestResult(description, t));
  }

  @Override
  public void notifyEnd(final Description description) {
    put(new TestResult(description, null));
  }

  private void put(final TestResult tr) {
    this.feedback.add(tr);
  }

  @Override
  public boolean shouldExit() {
    return false;
  }

}
