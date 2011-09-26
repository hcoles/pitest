/*
 * Copyright 2010 Henry Coles
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
package org.pitest.coverage.execute;

import static org.pitest.util.Unchecked.translateCheckedException;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.pitest.boot.CodeCoverageStore;
import org.pitest.boot.HotSwapAgent;
import org.pitest.coverage.CoverageTransformer;
import org.pitest.extension.TestUnit;
import org.pitest.util.ExitCode;
import org.pitest.util.Log;
import org.pitest.util.SafeDataInputStream;

public class CoverageSlave {

  private final static Logger LOG = Log.getLogger();

  public static void main(final String[] args) {

    ExitCode exitCode = ExitCode.OK;
    Socket s = null;
    CoveragePipe invokeQueue = null;
    try {

      final int port = Integer.valueOf(args[0]);
      s = new Socket("localhost", port);

      final SafeDataInputStream dis = new SafeDataInputStream(
          s.getInputStream());

      final SlaveArguments paramsFromParent = dis.read(SlaveArguments.class);

      Log.setVerbose(paramsFromParent.isVerbose());

      final DataOutputStream dos = new DataOutputStream(
          new BufferedOutputStream(s.getOutputStream()));

      invokeQueue = new CoveragePipe(dos);

      CodeCoverageStore.init(invokeQueue);

      HotSwapAgent.addTransformer(new CoverageTransformer(paramsFromParent
          .getFilter()));

      final List<TestUnit> tus = getTestsFromParent(dis);

      LOG.info(tus.size() + " tests received");

      final CoverageWorker worker = new CoverageWorker(invokeQueue, tus);

      worker.run();

    } catch (final Throwable ex) {
      LOG.log(Level.SEVERE, "Error calculating coverage. Process will exit.",
          ex);
      ex.printStackTrace();
      exitCode = ExitCode.UNKNOWN_ERROR;
    } finally {
      if (invokeQueue != null) {
        invokeQueue.end();
      }

      try {
        if (s != null) {
          s.close();
        }
      } catch (final IOException e) {
        throw translateCheckedException(e);
      }
    }

    System.exit(exitCode.getCode());

  }

  private static List<TestUnit> getTestsFromParent(final SafeDataInputStream dis)
      throws IOException {
    final int count = dis.readInt();
    final List<TestUnit> tus = new ArrayList<TestUnit>(count);
    for (int i = 0; i != count; i++) {
      tus.add(dis.read(TestUnit.class));
    }
    LOG.fine("Receiving " + count + " tests from parent");
    return tus;

  }

}
