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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.pitest.coverage.CoverageTransformer;
import org.pitest.extension.TestUnit;
import org.pitest.internal.IsolationUtils;
import org.pitest.mutationtest.instrument.HotSwapAgent;
import org.pitest.util.ExitCode;
import org.pitest.util.Log;

public class CoverageSlave {

  private final static Logger LOG = Log.getLogger();

  public static void main(final String[] args) {

    ExitCode exitCode = ExitCode.OK;
    Socket s = null;
    try {
      final File input = new File(args[0]);

      LOG.fine("Input file is " + input);

      final BufferedReader br = new BufferedReader(new InputStreamReader(
          new FileInputStream(input)));

      final SlaveArguments paramsFromParent = (SlaveArguments) IsolationUtils
          .fromTransportString(br.readLine());

      System.setProperties(paramsFromParent.getSystemProperties());

      Log.setVerbose(paramsFromParent.isVerbose());

      br.close();

      HotSwapAgent.addTransformer(new CoverageTransformer(paramsFromParent
          .getFilter()));

      s = new Socket("localhost", paramsFromParent.getPort());

      final List<TestUnit> tus = getTestsFromParent(s);

      LOG.info(tus.size() + " tests received");

      final CoverageWorker worker = new CoverageWorker(s.getOutputStream(), tus);

      worker.run();

    } catch (final Throwable ex) {
      LOG.log(Level.SEVERE, "Error calculating coverage. Process will exit.",
          ex);
      ex.printStackTrace();
      exitCode = ExitCode.UNKNOWN_ERROR;
    } finally {
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

  private static List<TestUnit> getTestsFromParent(final Socket s)
      throws IOException {
    final BufferedReader br = new BufferedReader(new InputStreamReader(
        s.getInputStream()));
    final List<TestUnit> tus = new ArrayList<TestUnit>();
    String line = br.readLine();
    while (!line.equals("END")) {
      tus.add((TestUnit) IsolationUtils.fromTransportString(line));
      line = br.readLine();
    }

    return tus;
  }

}
