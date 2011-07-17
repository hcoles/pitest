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
package org.pitest.mutationtest.instrument;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.instrument.ResultsReader.DetectionStatus;
import org.pitest.mutationtest.instrument.protocol.Id;
import org.pitest.util.SafeDataInputStream;
import org.pitest.util.SafeDataOutputStream;
import org.pitest.util.Unchecked;

public class MutationTestCommunicationThread extends Thread {

  // private final static Logger LOG = Log
  // .getLogger();

  private final int                                      port;
  private final SlaveArguments                           arguments;
  private final Map<MutationIdentifier, DetectionStatus> idMap = new HashMap<MutationIdentifier, DetectionStatus>();

  public MutationTestCommunicationThread(final int port,
      final SlaveArguments arguments) {
    this.setDaemon(true);
    this.port = port;
    this.arguments = arguments;
  }

  @Override
  public void run() {

    ServerSocket socket = null;
    Socket clientSocket = null;
    try {
      socket = new ServerSocket(this.port);
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

        if (socket != null) {
          socket.close();
        }
      } catch (final IOException e) {
        throw Unchecked.translateCheckedException(e);
      }
    }

  }

  private void receiveResults(final SafeDataInputStream is) {
    byte control = is.readByte();
    while (control != Id.DONE) {
      switch (control) {
      case Id.DESCRIBE:
        handleDescribe(is);
        break;
      case Id.REPORT:
        handleReport(is);
        break;
      }
      control = is.readByte();
    }
  }

  private void handleReport(final SafeDataInputStream is) {
    final MutationIdentifier mutation = is.read(MutationIdentifier.class);
    final DetectionStatus value = is.read(DetectionStatus.class);
    this.idMap.put(mutation, value);
  }

  private void handleDescribe(final SafeDataInputStream is) {
    final MutationIdentifier mutation = is.read(MutationIdentifier.class);
    this.idMap.put(mutation, DetectionStatus.STARTED);
  }

  private void sendDataToSlave(final Socket clientSocket) throws IOException {
    final OutputStream os = clientSocket.getOutputStream();
    final SafeDataOutputStream dos = new SafeDataOutputStream(os);
    sendArguments(dos);
  }

  private void sendArguments(final SafeDataOutputStream dos) throws IOException {
    dos.write(this.arguments);
    dos.flush();
  }

  public void waitToFinish() throws InterruptedException {
    this.join();
  }

  public DetectionStatus getStatus(final MutationIdentifier id) {
    return this.idMap.get(id);
  }

}
