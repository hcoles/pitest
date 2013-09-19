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
package org.pitest.junit.adapter.foreignclassloader;

import java.util.List;

import org.junit.runner.Runner;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.pitest.functional.SideEffect2;
import org.pitest.testapi.Description;
import org.pitest.testapi.ResultCollector;
import org.pitest.util.IsolationUtils;

public class ForeignClassLoaderCustomRunnerExecutor {

  private final Runner runner;
  private List<String> queue;

  public ForeignClassLoaderCustomRunnerExecutor(final Runner runner) {
    this.runner = runner;
  }

  public void run() { // NO_UCD

    final RunNotifier rn = new RunNotifier();
    final RunListener listener = new ForeignClassLoaderAdaptingRunListener(
        this.queue);
    rn.addFirstListener(listener);
    this.runner.run(rn);

  }

  public List<String> getQueue() { // NO_UCD
    return this.queue;
  }

  public void setQueue(final List<String> queue) { // NO_UCD
    this.queue = queue;
  }

  public static void applyEvents(final List<String> encodedEvents,
      final ResultCollector rc, final Description description) {
    for (final String each : encodedEvents) {
      @SuppressWarnings("unchecked")
      final SideEffect2<ResultCollector, org.pitest.testapi.Description> event = (SideEffect2<ResultCollector, org.pitest.testapi.Description>) IsolationUtils
          .fromXml(each);
      event.apply(rc, description);
    }

  }

}
