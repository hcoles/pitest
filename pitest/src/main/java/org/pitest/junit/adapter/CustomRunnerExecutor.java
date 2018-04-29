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

import org.junit.runner.Runner;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.pitest.testapi.ResultCollector;

public class CustomRunnerExecutor {

  private final org.pitest.testapi.Description description;
  private final Runner                         runner;
  private final ResultCollector                rc;

  public CustomRunnerExecutor(final org.pitest.testapi.Description description,
      final Runner runner, final ResultCollector rc) {
    this.runner = runner;
    this.rc = rc;
    this.description = description;
  }

  public void run() {

    final RunNotifier rn = new RunNotifier();
    final RunListener listener = new AdaptingRunListener(this.description,
        this.rc);

    rn.addFirstListener(listener);
    this.runner.run(rn);

  }

}
