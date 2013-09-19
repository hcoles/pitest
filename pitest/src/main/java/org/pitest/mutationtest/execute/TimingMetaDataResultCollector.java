/*
 * Copyright 2011 Henry Coles
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
package org.pitest.mutationtest.execute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.pitest.testapi.Description;
import org.pitest.testapi.MetaData;
import org.pitest.testapi.ResultCollector;

class TimingMetaDataResultCollector implements ResultCollector {

  private final ResultCollector child;
  private final long            time;

  TimingMetaDataResultCollector(final ResultCollector child,
      final long executionTime) {
    this.child = child;
    this.time = executionTime;
  }

  public void notifyEnd(final Description description, final Throwable t,
      final MetaData... data) {
    this.child.notifyEnd(description, t, addTimingToMetaData(data));
  }

  private MetaData[] addTimingToMetaData(final MetaData... data) {
    final List<MetaData> md = new ArrayList<MetaData>(Arrays.asList(data));
    md.add(new TimingMetaData(this.time));
    return md.toArray(new MetaData[] {});
  }

  public void notifyEnd(final Description description, final MetaData... data) {
    this.child.notifyEnd(description, addTimingToMetaData(data));
  }

  public void notifyStart(final Description description) {
    this.child.notifyStart(description);
  }

  public void notifySkipped(final Description description) {
    this.child.notifySkipped(description);
  }

  public boolean shouldExit() {
    return this.child.shouldExit();
  }

}
