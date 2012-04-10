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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.pitest.functional.SideEffect1;
import org.pitest.mutationtest.instrument.protocol.Id;

public class CommunicationThread extends Thread {


  private final SideEffect1<SafeDataOutputStream> sendInitialData;
  private final ReceiveStrategy                   receive;

  private final ServerSocket                               socket;

  private ExitCode                                exitCode;

  public CommunicationThread(final ServerSocket socket,
      final SideEffect1<SafeDataOutputStream> sendInitialData,
      final ReceiveStrategy receive) {
    super("pit comms thread");
    this.setDaemon(true);
    this.socket = socket;
    this.sendInitialData = sendInitialData;
    this.receive = receive;
  }

  @Override
  public final void run() {

    Socket clientSocket = null;
    try {
      clientSocket = socket.accept();
      final BufferedInputStream bif = new BufferedInputStream(
          clientSocket.getInputStream());

      sendDataToSlave(clientSocket);

      final SafeDataInputStream is = new SafeDataInputStream(bif);
      receiveResults(is);

      bif.close();

    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    } finally {
      try {
        if (clientSocket != null) {
          clientSocket.close();
        }

          socket.close();
        
      } catch (final IOException e) {
        throw Unchecked.translateCheckedException(e);
      }
    }

  }

  private void receiveResults(final SafeDataInputStream is) {
    byte control = is.readByte();
    while (control != Id.DONE) {
      this.receive.apply(control, is);
      control = is.readByte();
    }
    this.exitCode = ExitCode.fromCode(is.readInt());

  }

  private void sendDataToSlave(final Socket clientSocket) throws IOException {
    final OutputStream os = clientSocket.getOutputStream();
    final SafeDataOutputStream dos = new SafeDataOutputStream(os);
    this.sendInitialData.apply(dos);
  }

  public void waitToFinish() throws InterruptedException {
    this.join();
  }

  public ExitCode getExitCode() {
    return this.exitCode;
  }

}
