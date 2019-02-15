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

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import org.pitest.functional.SideEffect1;

public class StreamMonitor extends Thread implements Monitor {
  private static final Logger       LOG = Log.getLogger();

  private final byte[]              buf = new byte[256];
  private final InputStream         in;
  private final SideEffect1<String> inputHandler;

  public StreamMonitor(final InputStream in,
      final SideEffect1<String> inputHandler) {
    super("PIT Stream Monitor");
    this.in = in;
    this.inputHandler = inputHandler;
    setDaemon(true);
  }

  @Override
  public void requestStart() {
    start();
  }

  @Override
  public void run() {
    while (!this.isInterrupted()) {
      readFromStream();
    }
  }

  private void readFromStream() {
    try {

      // If child JVM crashes reading stdout/stderr seems to sometimes
      // block and consume 100% cpu, so check stream is available first.
      // May still be an issue if child crashes during later read . . .
      if (this.in.available() == 0) {
        Thread.sleep(100);
        return;
      }

      int i;
      while ((i = this.in.read(this.buf, 0, this.buf.length)) != -1) {
        final String output = new String(this.buf, 0, i);
        this.inputHandler.apply(output);
      }

    } catch (final IOException e) {
      requestStop();
      LOG.fine("No longer able to read stream.");
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public void requestStop() {
    this.interrupt();
  }

}
