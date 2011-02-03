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
package org.pitest.mutationtest.instrument;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.management.MemoryNotificationInfo;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;

import org.pitest.functional.F2;
import org.pitest.functional.Option;
import org.pitest.internal.IsolationUtils;
import org.pitest.util.CommandLineMessage;
import org.pitest.util.ExitCode;
import org.pitest.util.MemoryWatchdog;

public class InstrumentedMutationTestSlave {

  public static void main(final String[] args) {

    addMemoryWatchDog();
    Writer w = null;

    try {
      final File input = new File(args[0]);
      final File outputFile = new File(args[1]);

      System.out.println("Input file is " + input);
      System.out.println("Output file is " + input);

      final BufferedReader br = new BufferedReader(new InputStreamReader(
          new FileInputStream(input)));
      w = new OutputStreamWriter(new FileOutputStream(outputFile));

      final SlaveArguments paramsFromParent = (SlaveArguments) IsolationUtils
          .fromTransportString(br.readLine());
      System.out.println("Received stats from parent = "
          + paramsFromParent.stats);

      System.setProperties(paramsFromParent.systemProperties);

      br.close();

      final F2<Class<?>, byte[], Boolean> hotswap = new F2<Class<?>, byte[], Boolean>() {

        public Boolean apply(final Class<?> a, final byte[] b) {
          return HotSwapAgent.hotSwap(a, b);
        }

      };

      final MutationTestWorker worker = new MutationTestWorker(hotswap,
          paramsFromParent.tests,
          paramsFromParent.config.createMutator(IsolationUtils
              .getContextClassLoader()), IsolationUtils.getContextClassLoader());

      final Reporter r = new DefaultReporter(w);

      Option<Statistics> stats = paramsFromParent.stats;
      if (stats.hasNone()) {
        stats = Option.some(worker.gatherStatistics(
            paramsFromParent.classesToMutate, r));
        w.write("STATS=" + IsolationUtils.toTransportString(stats) + "\n");
      }

      worker.run(paramsFromParent.mutations, r, stats.value(), true);

    } catch (final Exception ex) {
      ex.printStackTrace(System.out);
      System.out.println("----------------------");
      if (w != null) {
        try {
          w.close();
        } catch (final IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      System.exit(ExitCode.UNKNOWN_ERROR.getCode());
    } finally {
      if (w != null) {
        try {
          w.close();
        } catch (final IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }

  }

  private static void addMemoryWatchDog() {
    final NotificationListener listener = new NotificationListener() {

      public void handleNotification(final Notification notification,
          final Object handback) {
        final String type = notification.getType();
        if (type.equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED)) {
          final CompositeData cd = (CompositeData) notification.getUserData();
          final MemoryNotificationInfo memInfo = MemoryNotificationInfo
              .from(cd);
          CommandLineMessage.report(memInfo.getPoolName()
              + " has exceeded the shutdown threshold : " + memInfo.getCount()
              + " times.\n" + memInfo.getUsage());

          System.exit(ExitCode.OUT_OF_MEMORY.getCode());

        } else {
          System.out.println("Unknown notification: " + notification);
        }
      }

    };

    MemoryWatchdog.addWatchDogToAllPools(90, listener);

  }

}
