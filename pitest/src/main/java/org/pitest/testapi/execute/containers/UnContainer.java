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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.pitest.testapi.TestResult;
import org.pitest.testapi.TestUnit;
import org.pitest.testapi.execute.Container;
import org.pitest.testapi.execute.ResultSource;
import org.pitest.util.IsolationUtils;

public class UnContainer implements Container {

  protected final BlockingQueue<TestResult> feedbackQueue = new ArrayBlockingQueue<TestResult>(
                                                              BUFFER_SIZE);

  public void setMaxThreads(final int maxThreads) {
    // ignore
  }

  public void shutdownWhenProcessingComplete() {
    // ignore
  }

  public void submit(final TestUnit group) {
    final ConcreteResultCollector rc = new ConcreteResultCollector(
        this.feedbackQueue);
    group.execute(IsolationUtils.getContextClassLoader(), rc);
  }

  public boolean awaitCompletion() {
    return true;
  }

  public ResultSource getResultSource() {
    return new ResultSource() {

      public List<TestResult> getAvailableResults() {
        final List<TestResult> results = new ArrayList<TestResult>();
        UnContainer.this.feedbackQueue.drainTo(results);
        return results;
      }

      public boolean resultsAvailable() {
        return !UnContainer.this.feedbackQueue.isEmpty();
      }

    };
  }

}
