package org.pitest.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyByte;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.functional.SideEffect1;

public class SocketReadingCallableTest {

  private SocketReadingCallable             testee;

  @Mock
  private ServerSocket                      socket;

  @Mock
  private SideEffect1<SafeDataOutputStream> sendDataSideEffect;

  @Mock
  private ReceiveStrategy                   receiveStrategy;

  @Mock
  private Socket                            clientSocket;

  private ByteArrayOutputStream             o;

  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.initMocks(this);
    this.testee = new SocketReadingCallable(this.socket,
        this.sendDataSideEffect, this.receiveStrategy);

    when(this.socket.accept()).thenReturn(this.clientSocket);

    this.o = new ByteArrayOutputStream();
  }

  @Test
  public void shouldReportTheExitCodeSentByTheMinionProcess() throws Exception {
    mockClientSocketToSendExitCode(ExitCode.TIMEOUT);
    assertEquals(ExitCode.TIMEOUT, this.testee.call());
  }

  @Test
  public void shouldSendInitialDataToMinion() throws Exception {
    mockClientSocketToSendExitCode(ExitCode.TIMEOUT);
    this.testee.call();
    verify(this.sendDataSideEffect).apply(any(SafeDataOutputStream.class));
  }

  @Test
  public void shouldPassNotPassDoneCommandToReceiver() throws Exception {
    mockClientSocketToSendExitCode(ExitCode.TIMEOUT);
    this.testee.call();
    verify(this.receiveStrategy, never()).apply(anyByte(),
        any(SafeDataInputStream.class));
  }

  @Test
  public void shouldPassCommandsToReceiver() throws Exception {
    final SafeDataOutputStream dos = new SafeDataOutputStream(this.o);
    dos.writeByte(Id.DESCRIBE);
    dos.writeByte(Id.DONE);
    dos.writeInt(ExitCode.OK.getCode());
    mockClientSocketInputStream();
    this.testee.call();
    verify(this.receiveStrategy, times(1)).apply(anyByte(),
        any(SafeDataInputStream.class));
  }

  private void mockClientSocketInputStream() throws IOException {
    final ByteArrayInputStream bis = new ByteArrayInputStream(
        this.o.toByteArray());
    when(this.clientSocket.getInputStream()).thenReturn(bis);
  }

  private void mockClientSocketToSendExitCode(final ExitCode timeout)
      throws IOException {
    final SafeDataOutputStream dos = new SafeDataOutputStream(this.o);
    dos.writeByte(Id.DONE);
    dos.writeInt(timeout.getCode());
    mockClientSocketInputStream();
  }

}
