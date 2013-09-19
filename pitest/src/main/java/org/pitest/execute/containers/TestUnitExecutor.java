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

import org.pitest.testapi.TestResult;
import org.pitest.testapi.TestUnit;

public class TestUnitExecutor implements Runnable {

  private final TestUnit                  group;
  private final BlockingQueue<TestResult> feedback;
  private final ClassLoaderFactory        loaderFactory;

  public TestUnitExecutor(final ClassLoaderFactory loaderFactory,
      final TestUnit group, final BlockingQueue<TestResult> feedbackQueue) {
    this.feedback = feedbackQueue;
    this.group = group;
    this.loaderFactory = loaderFactory;
  }

  public void run() {
    final ConcreteResultCollector rc = new ConcreteResultCollector(
        this.feedback);

    this.group.execute(this.loaderFactory.get(), rc);

  }

}
