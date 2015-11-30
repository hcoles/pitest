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

import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.pitest.functional.SideEffect2;
import org.pitest.testapi.ResultCollector;
import org.pitest.testapi.foreignclassloader.Fail;
import org.pitest.testapi.foreignclassloader.Skipped;
import org.pitest.testapi.foreignclassloader.Start;
import org.pitest.testapi.foreignclassloader.Success;
import org.pitest.util.IsolationUtils;

class ForeignClassLoaderAdaptingRunListener extends RunListener {

  private final List<String> events;
  private boolean            finished = false;

  ForeignClassLoaderAdaptingRunListener(final List<String> queue) {
    this.events = queue;
  }

  @Override
  public void testFailure(final Failure failure) throws Exception {
    storeAsString(new Fail(failure.getException()));
    this.finished = true;
  }

  @Override
  public void testAssumptionFailure(final Failure failure) {
    // do nothing == success
  }

  @Override
  public void testIgnored(final Description description) throws Exception {
    storeAsString(new Skipped());
    this.finished = true;
  }

  @Override
  public void testStarted(final Description description) throws Exception {
    storeAsString(new Start());
  }

  @Override
  public void testFinished(final Description description) throws Exception {
    if (!this.finished) {
      storeAsString(new Success());
    }
  }

  private void storeAsString(
      final SideEffect2<ResultCollector, org.pitest.testapi.Description> result) {
    this.events.add(IsolationUtils.toXml(result));
  }

}
