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

package org.pitest.mutationtest;

import static org.pitest.util.Unchecked.translateCheckedException;

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

import org.pitest.DefaultStaticConfig;
import org.pitest.Pitest;
import org.pitest.containers.UnContainer;
import org.pitest.extension.Container;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.EmptyConfiguration;
import org.pitest.internal.ClassPath;
import org.pitest.internal.IsolationUtils;
import org.pitest.internal.classloader.DefaultPITClassloader;
import org.pitest.util.CommandLineMessage;
import org.pitest.util.ExitCodes;
import org.pitest.util.MemoryWatchdog;
import org.pitest.util.Unchecked;

import com.thoughtworks.xstream.XStream;

/// FIXME copy and paste !
public class HotSwapMutationTestSlave {

  protected static final int OUT_OF_MEMORY = -42;

  protected void run(final int startMutation, final int endMutation,
      final String className, final long normalExecutionTime,
      final BufferedReader br, final Writer w) throws IOException,
      ClassNotFoundException {

    final DefaultPITClassloader loader = createClassLoader(br.readLine());
    IsolationUtils.setContextClassLoader(loader);

    System.out.println("Slave Mutating class " + className);

    final List<TestUnit> tests = getTestList(br.readLine(), loader);
    br.close();

    final Container c = new UnContainer();
    for (int i = startMutation; i != endMutation; i++) {
      receiveMutation();
      System.out.println("Slave Running mutation " + i);

      final boolean mutationDetected = doTestsDetectMutation(c, loader, tests,
          normalExecutionTime);

      w.write("" + i + "=" + mutationDetected);

      System.out.println("Slave Mutation " + i + " of " + endMutation
          + " detected = " + mutationDetected);
    }

    System.out.println(".....................");
  }

  private static void receiveMutation() {
    System.out.println("Receive mutation");

  }

  public static void main(final String[] args) {

    addMemoryWatchDog();
    Writer w = null;
    try {

      final int startMutation = Integer.parseInt(args[0]);
      final int endMutation = Integer.parseInt(args[1]);
      final String className = args[2];
      final long normalExecutionTime = Long.parseLong(args[3]);
      final File input = new File(args[4]);
      final File outputFile = new File(args[5]);
      System.out.println("Slave Input file is " + input);
      System.out.println("Slave Output file is " + input);
      final BufferedReader br = new BufferedReader(new InputStreamReader(
          new FileInputStream(input)));
      w = new OutputStreamWriter(new FileOutputStream(outputFile));
      final HotSwapMutationTestSlave instance = new HotSwapMutationTestSlave();
      instance.run(startMutation, endMutation, className, normalExecutionTime,
          br, w);

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
    System.exit(ExitCodes.OK);

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

          System.exit(OUT_OF_MEMORY);

        } else {
          System.out.println("Unknown notification: " + notification);
        }
      }

    };

    MemoryWatchdog.addWatchDogToAllPools(90, listener);

  }

  private static boolean doTestsDetectMutation(final Container c,
      final ClassLoader loader, final List<TestUnit> tests,
      final long normalExecutionTime) {
    try {
      final CheckTestHasFailedResultListener listener = new CheckTestHasFailedResultListener();

      final EmptyConfiguration conf = new EmptyConfiguration();
      final Pitest pit = new Pitest(conf);
      final DefaultStaticConfig staticConfig = new DefaultStaticConfig();
      staticConfig.addTestListener(listener);
      pit.run(c, staticConfig, tests);

      return listener.resultIndicatesSuccess();
    } catch (final Exception ex) {
      throw translateCheckedException(ex);
    }

  }

  private static List<TestUnit> getTestList(final String xml,
      final DefaultPITClassloader cl) throws IOException {
    final List<TestUnit> tests = xmlToTestGroup(xml, cl);
    return tests;
  }

  private static DefaultPITClassloader createClassLoader(
      final String classPathXML) throws IOException {

    final String xml = IsolationUtils.decodeTransportString(classPathXML);

    final ClassPath cp = (ClassPath) IsolationUtils.fromXml(xml);

    final DefaultPITClassloader cl = new DefaultPITClassloader(cp,
        IsolationUtils.getContextClassLoader());
    return cl;
  }

  @SuppressWarnings("unchecked")
  private static List<TestUnit> xmlToTestGroup(final String encodedXml,
      final ClassLoader cl) {
    try {
      final XStream xstream = new XStream();
      xstream.setClassLoader(cl);
      return (List<TestUnit>) xstream.fromXML(IsolationUtils
          .decodeTransportString(encodedXml));

    } catch (final Exception ex) {
      throw Unchecked.translateCheckedException(ex);
    }
  }

}
