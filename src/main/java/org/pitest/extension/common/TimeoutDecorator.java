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
package org.pitest.extension.common;

import org.pitest.TimeoutException;
import org.pitest.extension.ResultCollector;
import org.pitest.extension.TestUnit;

public final class TimeoutDecorator extends TestUnitDecorator {

  private static final long serialVersionUID = 1L;

  private final long        timeout;
  private boolean           run              = false;

  public TimeoutDecorator(final TestUnit child, final long timeout) {
    super(child);
    this.timeout = timeout;
  }

  static class ResultCollectorWrapper implements ResultCollector {

    private final ResultCollector child;
    private boolean               reportResults = true;

    public ResultCollectorWrapper(final ResultCollector child) {
      this.child = child;
    }

    public void notifySkipped(final TestUnit tu) {
      if (this.reportResults) {
        this.child.notifySkipped(tu);
      }
    }

    public void notifyStart(final TestUnit tu) {
      if (this.reportResults) {
        this.child.notifyStart(tu);
      }
    }

    public void stopReporting() {
      this.reportResults = false;
    }

    public void notifyEnd(final TestUnit tu, final Throwable t) {
      if (this.reportResults) {
        this.child.notifyEnd(tu, t);
      }

    }

    public void notifyEnd(final TestUnit tu) {
      if (this.reportResults) {
        this.child.notifyEnd(tu);
      }
    }

  }

  public void execute(final ClassLoader loader, final ResultCollector rc) {

    final ResultCollectorWrapper wrc = new ResultCollectorWrapper(rc);
    final Thread thread = new Thread() {
      @Override
      public void run() {
        TimeoutDecorator.this.child().execute(loader, rc);
        TimeoutDecorator.this.run = true;
      }
    };
    thread.start();
    try {
      thread.join(this.timeout);
    } catch (final InterruptedException e) {
      // swallow
    }
    if (!this.run) {
      wrc.stopReporting();
      rc.notifyEnd(this, createTimeoutError());
    }

  }

  private Throwable createTimeoutError() {
    return new TimeoutException(String.format(
        "Test timed out after %d milliseconds", this.timeout));
  }

}
