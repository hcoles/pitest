package org.pitest.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

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
import org.pitest.mutationtest.instrument.protocol.Id;

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
    testee = new SocketReadingCallable(socket, sendDataSideEffect,
        receiveStrategy);

    when(socket.accept()).thenReturn(clientSocket);

    o = new ByteArrayOutputStream();
  }

  @Test
  public void shouldReportTheExitCodeSentByTheSlaveProcess() throws Exception {
    mockClientSocketToSendExitCode(ExitCode.TIMEOUT);
    assertEquals(ExitCode.TIMEOUT, testee.call());
  }

  @Test
  public void shouldSendInitialDataToSlave() throws Exception {
    mockClientSocketToSendExitCode(ExitCode.TIMEOUT);
    testee.call();
    verify(this.sendDataSideEffect).apply(any(SafeDataOutputStream.class));
  }

  @Test
  public void shouldPassNotPassDoneCommandToReceiver() throws Exception {
    mockClientSocketToSendExitCode(ExitCode.TIMEOUT);
    testee.call();
    verify(this.receiveStrategy, never()).apply(anyByte(),
        any(SafeDataInputStream.class));
  }

  @Test
  public void shouldPassCommandsToReceiver() throws Exception {
    final SafeDataOutputStream dos = new SafeDataOutputStream(o);
    dos.writeByte(Id.DESCRIBE);
    dos.writeByte(Id.DONE);
    dos.writeInt(ExitCode.OK.getCode());
    mockClientSocketInputStream();
    testee.call();
    verify(this.receiveStrategy, times(1)).apply(anyByte(),
        any(SafeDataInputStream.class));
  }

  private void mockClientSocketInputStream() throws IOException {
    final ByteArrayInputStream bis = new ByteArrayInputStream(o.toByteArray());
    when(clientSocket.getInputStream()).thenReturn(bis);
  }

  private void mockClientSocketToSendExitCode(ExitCode timeout)
      throws IOException {
    final SafeDataOutputStream dos = new SafeDataOutputStream(o);
    dos.writeByte(Id.DONE);
    dos.writeInt(timeout.getCode());
    mockClientSocketInputStream();
  }

}
