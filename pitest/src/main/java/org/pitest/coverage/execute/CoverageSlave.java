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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.pitest.internal.IsolationUtils;
import org.pitest.util.ExitCode;

public class CoverageSlave {

  public static void main(final String[] args) {

    Writer w = null;
    ExitCode exitCode = ExitCode.OK;

    try {
      final File input = new File(args[0]);
      final File outputFile = new File(args[1]);

      System.out.println("Input file is " + input);
      System.out.println("Output file is " + outputFile);

      final BufferedReader br = new BufferedReader(new InputStreamReader(
          new FileInputStream(input)));
      w = new OutputStreamWriter(new FileOutputStream(outputFile));

      final SlaveArguments paramsFromParent = (SlaveArguments) IsolationUtils
          .fromTransportString(br.readLine());

      System.setProperties(paramsFromParent.systemProperties);

      br.close();

      final CoverageWorker worker = new CoverageWorker(paramsFromParent, w);

      worker.run();

    } catch (final Throwable ex) {
      ex.printStackTrace(System.out);
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
