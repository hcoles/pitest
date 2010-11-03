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
import java.util.List;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;

import org.pitest.extension.TestUnit;
import org.pitest.functional.F2;
import org.pitest.functional.Option;
import org.pitest.internal.IsolationUtils;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.util.CommandLineMessage;
import org.pitest.util.ExitCodes;
import org.pitest.util.MemoryWatchdog;
import org.pitest.util.Unchecked;

public class InstrumentedMutationTestSlave {

  @SuppressWarnings("unchecked")
  public static void main(final String[] args) {

    addMemoryWatchDog();
    Writer w = null;
    int exitCode = ExitCodes.OK;
    try {

      final int startMutation = Integer.parseInt(args[0]);
      final int endMutation = Integer.parseInt(args[1]);
      final String className = args[2];
      final File input = new File(args[3]);
      final File outputFile = new File(args[4]);
      System.out.println("Input file is " + input);
      System.out.println("Output file is " + input);
      final BufferedReader br = new BufferedReader(new InputStreamReader(
          new FileInputStream(input)));
      w = new OutputStreamWriter(new FileOutputStream(outputFile));

      Option<Statistics> stats = (Option<Statistics>) IsolationUtils
          .fromTransportString(br.readLine());

      System.out.println("Recevied stats from parent = " + stats);

      final MutationConfig mutationConfig = (MutationConfig) IsolationUtils
          .fromTransportString(br.readLine());

      final List<TestUnit> tests = getTestList(br.readLine(), IsolationUtils
          .getContextClassLoader());
      br.close();

      final MutationTestWorker worker = new MutationTestWorker(tests,
          mutationConfig, IsolationUtils.getContextClassLoader());

      final Reporter r = new DefaultReporter(w);

      final F2<Class<?>, byte[], Boolean> hotswap = new F2<Class<?>, byte[], Boolean>() {

        public Boolean apply(final Class<?> a, final byte[] b) {
          return HotSwapAgent.hotSwap(a, b);
        }

      };

      if (stats.hasNone()) {
        stats = Option.someOrNone(worker
            .gatherStatistics(hotswap, className, r));
        w.write("STATS=" + IsolationUtils.toTransportString(stats) + "\n");
      }

      for (final Integer each : stats.value().getStats().keySet()) {
        System.out.println("Covered line " + each);
      }

      exitCode = worker.run(hotswap, startMutation, endMutation, className, r,
          stats.value());

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
      System.exit(ExitCodes.UNKNOWN_ERROR);
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

    // sometimes hazelcast refuses to die. Kill explicitly
    System.exit(exitCode);

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

          System.exit(ExitCodes.OUT_OF_MEMORY);

        } else {
          System.out.println("Unknown notification: " + notification);
        }
      }

    };

    MemoryWatchdog.addWatchDogToAllPools(90, listener);

  }

  private static List<TestUnit> getTestList(final String xml,
      final ClassLoader cl) throws IOException {
    final List<TestUnit> tests = xmlToTestGroup(xml, cl);
    return tests;
  }

  @SuppressWarnings("unchecked")
  private static List<TestUnit> xmlToTestGroup(final String encodedXml,
      final ClassLoader cl) {
    try {

      return (List<TestUnit>) IsolationUtils.fromXml(IsolationUtils
          .decodeTransportString(encodedXml), cl);

    } catch (final Exception ex) {
      throw Unchecked.translateCheckedException(ex);
    }
  }

}
