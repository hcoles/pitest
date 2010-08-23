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
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.pitest.functional.SideEffect1;

public class StreamMonitor extends Thread {
  private final InputStreamReader   in;
  private final SideEffect1<String> inputHandler;

  // private final OutputStreamWriter out;

  @Override
  public void run() {
    try {
      int i;
      final char[] buf = new char[256];
      while ((i = this.in.read(buf, 0, buf.length)) >= 0) {
        final String output = new String(buf, 0, i);
        this.inputHandler.apply(output);
        // this.out.write(buf, 0, i);
        // this.out.flush();
      }

    } catch (final Throwable t) {
      t.printStackTrace();
    }
  }

  public StreamMonitor(final InputStream in, final PrintStream out) {
    this(in, new SideEffect1<String>() {
      public void apply(final String a) {
        out.print(a);
      }
    });
  }

  public StreamMonitor(final InputStream in,
      final SideEffect1<String> inputHandler) {
    super();
    this.in = new InputStreamReader(in);
    this.inputHandler = inputHandler;
    setDaemon(true);
    start();
  }
}
