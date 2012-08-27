package org.pitest.coverage.interprocess;

import java.net.ServerSocket;
import java.util.List;

import org.pitest.coverage.execute.CoverageOptions;
import org.pitest.coverage.execute.CoverageResult;
import org.pitest.functional.SideEffect1;
import org.pitest.util.CommunicationThread;

public class CoverageCommunicationThread extends CommunicationThread {

  public CoverageCommunicationThread(final ServerSocket socket,
      final CoverageOptions arguments, final List<String> tus,
      final SideEffect1<CoverageResult> handler) {
    super(socket, new SendData(arguments, tus), new Receive(handler));

  }

}
