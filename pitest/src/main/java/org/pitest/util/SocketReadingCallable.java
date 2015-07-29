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

  public SocketReadingCallable(final ServerSocket socket,
      final SideEffect1<SafeDataOutputStream> sendInitialData,
      final ReceiveStrategy receive) {
    this.socket = socket;
    this.sendInitialData = sendInitialData;
    this.receive = receive;
  }

  @Override
  public ExitCode call() throws Exception {
    final Socket clientSocket = this.socket.accept();
    ExitCode exitCode = ExitCode.UNKNOWN_ERROR;
    try {
      final BufferedInputStream bif = new BufferedInputStream(
          clientSocket.getInputStream());

      sendDataToSlave(clientSocket);

      final SafeDataInputStream is = new SafeDataInputStream(bif);
      exitCode = receiveResults(is);

      bif.close();

    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    } finally {
      try {
        if (clientSocket != null) {
          clientSocket.close();
        }
        this.socket.close();
      } catch (final IOException e) {
        throw Unchecked.translateCheckedException(e);
      }
    }

    return exitCode;
  }

  private void sendDataToSlave(final Socket clientSocket) throws IOException {
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