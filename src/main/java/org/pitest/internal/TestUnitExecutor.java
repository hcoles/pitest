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
package org.pitest.internal;

import java.util.concurrent.BlockingQueue;

import org.pitest.TestGroup;
import org.pitest.TestResult;
import org.pitest.extension.ClassLoaderFactory;
import org.pitest.extension.TestUnit;

/**
 * @author henry
 * 
 */
public class TestUnitExecutor implements Runnable {

  private final TestGroup                 group;
  private final BlockingQueue<TestResult> feedback;
  private final ClassLoaderFactory        loaderFactory;

  public TestUnitExecutor(final ClassLoaderFactory loaderFactory,
      final TestGroup group, final BlockingQueue<TestResult> feedbackQueue) {
    this.feedback = feedbackQueue;
    this.group = group;
    this.loaderFactory = loaderFactory;
  }

  public void run() {
    final ConcreteResultCollector rc = new ConcreteResultCollector(
        this.feedback);

    for (final TestUnit each : this.group) {
      each.execute(this.loaderFactory.get(), rc);
    }

  }

}
