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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class StreamMonitor extends Thread implements Monitor {
  private static final Logger       LOG = Log.getLogger();

  private final InputStream         in;
  private final Consumer<String> inputHandler;

  /**
   * Constructor.
   * @param in stream to read from
   * @param inputHandler all characters read from {@code in} will be forwarded
   *                    line by line (without the newline) to this handler.
   */
  public StreamMonitor(final InputStream in,
      final Consumer<String> inputHandler) {
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
    BufferedReader reader = new BufferedReader(new InputStreamReader(this.in));
    while (!this.isInterrupted()) {
      readFromStream(reader);
    }
  }

  private void readFromStream(final BufferedReader reader) {
    try {

      // If child JVM crashes reading stdout/stderr seems to sometimes
      // block and consume 100% cpu, so check stream is available first.
      // May still be an issue if child crashes during later read . . .
      if (!reader.ready()) {
        Thread.sleep(100);
        return;
      }

      String output;
      while ( ( output = reader.readLine() ) != null ) {
        this.inputHandler.accept(output);
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
