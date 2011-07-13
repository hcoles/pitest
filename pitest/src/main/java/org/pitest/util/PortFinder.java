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

import org.pitest.PitError;

public class PortFinder {

  private final static Logger LOG             = Log.getLogger();

  private final static int    MIN_PORT_NUMBER = 8081;
  private final static int    MAX_PORT_NUMBER = 9000;

  private int                 portNumber      = MIN_PORT_NUMBER;

  public int getNextAvailablePort() {
    while (!isPortAvailable(this.portNumber)) {
      this.portNumber++;

      if (this.portNumber > MAX_PORT_NUMBER) {
        throw new PitError("Could not find available port between "
            + MIN_PORT_NUMBER + " and " + MAX_PORT_NUMBER);
      }
    }

    LOG.fine("using port " + this.portNumber);

    return this.portNumber;
  }

  public static boolean isPortAvailable(final int port) {

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
