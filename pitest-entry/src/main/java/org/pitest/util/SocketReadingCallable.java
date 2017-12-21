package org.pitest.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;

import org.pitest.functional.SideEffect1;

class SocketReadingCallable implements Callable<ExitCode> {

  private final SideEffect1<SafeDataOutputStream> sendInitialData;
  private final ReceiveStrategy                   receive;
  private final ServerSocket                      socket;

  SocketReadingCallable(final ServerSocket socket,
      final SideEffect1<SafeDataOutputStream> sendInitialData,
      final ReceiveStrategy receive) {
    this.socket = socket;
    this.sendInitialData = sendInitialData;
    this.receive = receive;
  }

  @Override
  public ExitCode call() throws Exception {
    try (Socket clientSocket = this.socket.accept()) {
      try (BufferedInputStream bif = new BufferedInputStream(
          clientSocket.getInputStream())) {

        sendDataToMinion(clientSocket);

        final SafeDataInputStream is = new SafeDataInputStream(bif);
        return receiveResults(is);
      } catch (final IOException e) {
        throw Unchecked.translateCheckedException(e);
      }
    } finally {
      try {
        this.socket.close();
      } catch (final IOException e) {
        throw Unchecked.translateCheckedException(e);
      }
    }
  }

  private void sendDataToMinion(final Socket clientSocket) throws IOException {
    final OutputStream os = clientSocket.getOutputStream();
    final SafeDataOutputStream dos = new SafeDataOutputStream(os);
    this.sendInitialData.apply(dos);
  }

  private ExitCode receiveResults(final SafeDataInputStream is) {
    byte control = is.readByte();
    while (control != Id.DONE) {
      this.receive.apply(control, is);
      control = is.readByte();
    }
    return ExitCode.fromCode(is.readInt());

  }

}