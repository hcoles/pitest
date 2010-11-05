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
package org.pitest.util;

import java.io.InputStream;

import org.pitest.functional.SideEffect1;

public class StreamMonitor extends Thread {
  private final InputStream         in;
  private final SideEffect1<String> inputHandler;
  private volatile boolean          shouldRun = true;

  @Override
  public void run() {
    while (this.shouldRun) {
      readFromStream();
    }
  }

  public void requestStop() {
    this.shouldRun = false;
  }

  private void readFromStream() {
    try {
      int i;
      final byte[] buf = new byte[256];

      while ((i = this.in.read(buf, 0, buf.length)) >= 0) {
        final String output = new String(buf, 0, i);
        this.inputHandler.apply(output);
      }

    } catch (final Throwable t) {
      t.printStackTrace();
      throw Unchecked.translateCheckedException(t);
    }
  }

  public StreamMonitor(final InputStream in,
      final SideEffect1<String> inputHandler) {
    super();
    this.in = in;// new InputStreamReader(in);
    this.inputHandler = inputHandler;
    this.setName("PIT Stream Monitor");
    setDaemon(true);
    start();
  }
}
