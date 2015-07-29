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
package org.pitest.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Logger;

public class SocketFinder {

  private static final Logger LOG             = Log.getLogger();

  private static final int    MIN_PORT_NUMBER = 8091;
  private static final int    MAX_PORT_NUMBER = 9000;

  private int                 lastPortNumber  = MIN_PORT_NUMBER;

  public synchronized ServerSocket getNextAvailableServerSocket() {
    this.lastPortNumber++;
    ServerSocket socket = getIfAvailable(this.lastPortNumber);
    while (socket == null) {
      this.lastPortNumber++;

      if (this.lastPortNumber > MAX_PORT_NUMBER) {
        this.lastPortNumber = 9000;
      }
      socket = getIfAvailable(this.lastPortNumber);
    }

    LOG.fine("using port " + this.lastPortNumber);

    return socket;
  }

  private static synchronized ServerSocket getIfAvailable(final int port) {
    ServerSocket ss = null;
    try {
      ss = new ServerSocket(port);
    } catch (final IOException e) {
      LOG.fine("port " + port + " is in use");
    }

    return ss;
  }

}
