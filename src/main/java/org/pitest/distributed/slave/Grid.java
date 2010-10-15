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

package org.pitest.distributed.slave;

import java.util.Arrays;
import java.util.Collections;

import org.pitest.util.CommandLineMessage;
import org.pitest.util.JavaProcess;

public class Grid {

  private transient static boolean              run    = true;

  private transient volatile static JavaProcess worker = null;

  public static void main(final String[] args) {

    final Thread hook = new Thread() {
      @Override
      public void run() {
        // not thread safe by everything will probably be fine
        if ((worker != null) && worker.isAlive()) {
          CommandLineMessage.report("Shutdown requested. Killing child.");
          worker.destroy();
        }
      }
    };

    Runtime.getRuntime().addShutdownHook(hook);

    try {
      CommandLineMessage.report("PIT Grid vHighly.Experimental");

      while (run) {
        CommandLineMessage.report("Starting new slave process");

        worker = JavaProcess.launch(Arrays.asList(args), Slave.class,
            Collections.<String> emptyList());

        try {
          final int exitValue = worker.waitToDie();
          CommandLineMessage.report("Slave process exited with value of "
              + exitValue);
        } catch (final InterruptedException ex) {
          ex.printStackTrace();
        }

      }
    } catch (final Throwable ex) {
      ex.printStackTrace();
    }

    if ((worker != null) && worker.isAlive()) {
      worker.destroy();
    }

    CommandLineMessage.report("Closed down");
  }

}
