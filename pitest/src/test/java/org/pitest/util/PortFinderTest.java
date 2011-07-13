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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.Before;
import org.junit.Test;

public class PortFinderTest {

  PortFinder testee;

  @Before
  public void setUp() {
    this.testee = new PortFinder();
  }

  @Test
  public void shouldNotIncrementPortNumberIfPortIsNotInUse() {
    final int nextPort = this.testee.getNextAvailablePort();
    assertEquals(nextPort, this.testee.getNextAvailablePort());
  }

  @Test
  public void shouldDetectIfAPortIsInUse() throws IOException {
    final int nextPort = this.testee.getNextAvailablePort();

    ServerSocket ss = null;
    try {
      ss = new ServerSocket(nextPort);
      assertFalse(PortFinder.isPortAvailable(nextPort));
      assertEquals(nextPort + 1, this.testee.getNextAvailablePort());
    } finally {
      ss.close();
    }
  }

}
