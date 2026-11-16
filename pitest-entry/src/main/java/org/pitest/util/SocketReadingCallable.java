package org.pitest.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

class SocketReadingCallable implements Callable<ExitCode> {

  private final Consumer<SafeDataOutputStream> sendInitialData;
  private final ReceiveStrategy                   receive;
  private final ServerSocketChannel                      socket;

  SocketReadingCallable(final ServerSocketChannel socket,
      final Consumer<SafeDataOutputStream> sendInitialData,
      final ReceiveStrategy receive) {
    this.socket = socket;
    this.sendInitialData = sendInitialData;
    this.receive = receive;
  }

  @Override
  public ExitCode call() throws Exception {
    try (SocketChannel clientSocket = this.socket.accept()) {
      try (BufferedInputStream bif = new BufferedInputStream(
          Channels.newInputStream(clientSocket))) {

        sendDataToMinion(clientSocket);

        final SafeDataInputStream is = new SafeDataInputStream(bif);
        return receiveResults(is);
      } catch (final IOException e) {
        throw Unchecked.translateCheckedException(e);
      }
    } finally {
      try {
        final Path path = SocketFinder.pathOf(this.socket);
        this.socket.close();
        Files.deleteIfExists(path);
        Files.deleteIfExists(path.getParent());
      } catch (final IOException e) {
        throw Unchecked.translateCheckedException(e);
      }
    }
  }

  private void sendDataToMinion(final SocketChannel clientSocket) throws IOException {
    final OutputStream os = Channels.newOutputStream(clientSocket);
    final SafeDataOutputStream dos = new SafeDataOutputStream(os);
    this.sendInitialData.accept(dos);
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