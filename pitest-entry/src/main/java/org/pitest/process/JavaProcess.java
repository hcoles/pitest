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
package org.pitest.process;

import org.pitest.functional.SideEffect1;
import org.pitest.util.Monitor;
import org.pitest.util.StreamMonitor;

public class JavaProcess {

  private final Process process;
  private final Monitor out;
  private final Monitor err;

  public JavaProcess(Process process, SideEffect1<String> sysoutHandler,
      SideEffect1<String> syserrHandler) {
    this.process = process;

    this.out = new StreamMonitor(process.getInputStream(), sysoutHandler);
    this.err = new StreamMonitor(process.getErrorStream(), syserrHandler);

    this.out.requestStart();
    this.err.requestStart();
  }

  public void destroy() {
    this.out.requestStop();
    this.err.requestStop();
    this.process.destroy();
  }

  public int waitToDie() throws InterruptedException {
    final int exitVal = this.process.waitFor();
    this.out.requestStop();
    this.err.requestStop();
    return exitVal;
  }

  public boolean isAlive() {
    try {
      this.process.exitValue();
      return false;
    } catch (final IllegalThreadStateException e) {
      return true;
    }
  }

}
