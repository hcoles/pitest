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
package org.pitest.execute;

import org.pitest.testapi.Description;
import org.pitest.testapi.MetaData;
import org.pitest.testapi.ResultCollector;

public class ExitingResultCollector implements ResultCollector {

  private final ResultCollector child;
  private boolean               hadFailure = false;

  public ExitingResultCollector(final ResultCollector child) {
    this.child = child;
  }

  public void notifySkipped(final Description description) {
    this.child.notifySkipped(description);
  }

  public void notifyStart(final Description description) {
    this.child.notifyStart(description);
  }

  public boolean shouldExit() {
    return this.hadFailure;
  }

  public void notifyEnd(final Description description, final Throwable t,
      final MetaData... data) {
    this.child.notifyEnd(description, t, data);
    if (t != null) {
      this.hadFailure = true;
    }

  }

  public void notifyEnd(final Description description, final MetaData... data) {
    this.child.notifyEnd(description, data);

  }

}