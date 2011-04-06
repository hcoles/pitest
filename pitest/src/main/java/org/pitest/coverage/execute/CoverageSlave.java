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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.pitest.coverage.CoverageTransformer;
import org.pitest.internal.IsolationUtils;
import org.pitest.mutationtest.instrument.HotSwapAgent;
import org.pitest.util.ExitCode;
import org.pitest.util.Log;

public class CoverageSlave {

  private final static Logger LOG = Log.getLogger();

  public static void main(final String[] args) {

    final Writer w = null;
    ExitCode exitCode = ExitCode.OK;

    try {
      final File input = new File(args[0]);

      LOG.fine("Input file is " + input);

      final BufferedReader br = new BufferedReader(new InputStreamReader(
          new FileInputStream(input)));

      final SlaveArguments paramsFromParent = (SlaveArguments) IsolationUtils
          .fromTransportString(br.readLine());

      System.setProperties(paramsFromParent.getSystemProperties());

      br.close();

      HotSwapAgent.addTransformer(new CoverageTransformer(paramsFromParent
          .getFilter()));

      final CoverageWorker worker = new CoverageWorker(paramsFromParent, w);

      worker.run();

    } catch (final Throwable ex) {
      LOG.log(Level.SEVERE, "Error calculating coverage. Process will exit.",
          ex);
      exitCode = ExitCode.UNKNOWN_ERROR;
    } finally {
      if (w != null) {
        try {
          w.close();
        } catch (final IOException e) {
          e.printStackTrace();
        }
      }
    }

    System.exit(exitCode.getCode());

  }

}
