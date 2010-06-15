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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.pitest.TestGroup;
import org.pitest.TestResult;
import org.pitest.extension.Container;
import org.pitest.extension.TestUnit;
import org.pitest.internal.ConcreteResultCollector;

public class UnContainer implements Container {

  private final BlockingQueue<TestResult> feedbackQueue = new LinkedBlockingQueue<TestResult>();

  public boolean awaitTermination(final int i, final TimeUnit milliseconds)
      throws InterruptedException {
    return true;
  }

  public void setMaxThreads(final int maxThreads) {
    // ignore
  }

  public void shutdown() {
    // ignore
  }

  public void submit(final TestGroup group) {
    final ConcreteResultCollector rc = new ConcreteResultCollector(
        this.feedbackQueue);
    for (final TestUnit each : group) {
      each.execute(this.getClass().getClassLoader(), rc);
    }
  }

  public BlockingQueue<TestResult> feedbackQueue() {
    return this.feedbackQueue;
  }

}
