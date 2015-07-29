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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.runner.Runner;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

public class ForeignClassLoaderCustomRunnerExecutor implements
    Callable<List<String>> {

  private final Runner runner;

  public ForeignClassLoaderCustomRunnerExecutor(final Runner runner) {
    this.runner = runner;
  }

  @Override
  public List<String> call() { // NO_UCD
    List<String> queue = new ArrayList<String>();
    final RunNotifier rn = new RunNotifier();
    final RunListener listener = new ForeignClassLoaderAdaptingRunListener(
        queue);
    rn.addFirstListener(listener);
    this.runner.run(rn);
    return queue;

  }

}
