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

public enum PortFinder {

  INSTANCE;

  private final static Logger LOG             = Log.getLogger();

  private final static int    MIN_PORT_NUMBER = 8081;
  private final static int    MAX_PORT_NUMBER = 9000;

  private int                 lastPortNumber  = MIN_PORT_NUMBER;

  public synchronized int getNextAvailablePort() {
    this.lastPortNumber++;
    while (!isPortAvailable(this.lastPortNumber)) {
      this.lastPortNumber++;

      if (this.lastPortNumber > MAX_PORT_NUMBER) {
        this.lastPortNumber = 9000;
      }
    }

    LOG.fine("using port " + this.lastPortNumber);

    return this.lastPortNumber;
  }

  public synchronized static boolean isPortAvailable(final int port) {

    ServerSocket ss = null;
    try {
      ss = new ServerSocket(port);
      ss.setReuseAddress(true);
      return true;
    } catch (final IOException e) {
      LOG.fine("port " + port + " is in use");
    } finally {

      if (ss != null) {
        try {
          ss.close();
        } catch (final IOException e) {
          // swallow
        }
      }
    }

    return false;
  }

}
