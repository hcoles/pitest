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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.pitest.functional.SideEffect1;

public class CommunicationThread {

  private static final Logger                     LOG = Log.getLogger();

  private final SideEffect1<SafeDataOutputStream> sendInitialData;
  private final ReceiveStrategy                   receive;
  private final ServerSocket                      socket;
  private FutureTask<ExitCode>                    future;

  public CommunicationThread(final ServerSocket socket,
      final SideEffect1<SafeDataOutputStream> sendInitialData,
      final ReceiveStrategy receive) {
    this.socket = socket;
    this.sendInitialData = sendInitialData;
    this.receive = receive;
  }

  public void start() throws IOException, InterruptedException {
    this.future = createFuture();
  }

  private FutureTask<ExitCode> createFuture() {
    final FutureTask<ExitCode> newFuture = new FutureTask<>(
        new SocketReadingCallable(this.socket, this.sendInitialData,
            this.receive));
    final Thread thread = new Thread(newFuture);
    thread.setDaemon(true);
    thread.setName("pit communication");
    thread.start();
    return newFuture;
  }

  public ExitCode waitToFinish() {
    try {
      return this.future.get();
    } catch (final ExecutionException e) {
      LOG.log(Level.WARNING, "Error while watching child process", e);
      return ExitCode.UNKNOWN_ERROR;
    } catch (final InterruptedException e) {
      LOG.log(Level.WARNING, "interrupted while waiting for child process", e);
      return ExitCode.UNKNOWN_ERROR;
    }

  }

}
