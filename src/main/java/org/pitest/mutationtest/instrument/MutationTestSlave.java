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

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.util.ClassLoaderRepository;
import org.pitest.DefaultStaticConfig;
import org.pitest.Pitest;
import org.pitest.containers.UnContainer;
import org.pitest.extension.Container;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.EmptyConfiguration;
import org.pitest.internal.ClassPath;
import org.pitest.internal.ConcreteResultCollector;
import org.pitest.internal.IsolationUtils;
import org.pitest.internal.classloader.DefaultPITClassloader;
import org.pitest.mutationtest.CheckTestHasFailedResultListener;
import org.pitest.mutationtest.ExitingResultCollector;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.mutationtest.Mutator;
import org.pitest.mutationtest.hotswap.RunDetails;
import org.pitest.util.CommandLineMessage;
import org.pitest.util.ExitCodes;
import org.pitest.util.MemoryWatchdog;

import com.reeltwo.jumble.mutation.Mutater;

public class MutationTestSlave {

  private static File outputFile;

  protected void run(final DefaultPITClassloader loader, final RunDetails run,
      final Writer w, final Mutater m) throws IOException,
      ClassNotFoundException {

    System.out.println("Slave Mutating class " + run.getClassName());
    final Class<?> testee = Class.forName(run.getClassName());

    for (int i = run.getStartMutation(); i != run.getEndMutation(); i++) {
      m.setMutationPoint(i);
      final JavaClass mutant = m.jumbler(run.getClassName());
      if (HotSwapAgent.hotSwap(testee, mutant.getBytes())) {
        System.out.println("Slave Running mutation " + i);

        final Container c = new UnContainer() {
          @Override
          public void submit(final TestUnit group) {
            final ExitingResultCollector rc = new ExitingResultCollector(
                new ConcreteResultCollector(this.feedbackQueue));
            group.execute(IsolationUtils.getContextClassLoader(), rc);
          }
        };

        final boolean mutationDetected = doTestsDetectMutation(c, loader, run
            .getTests());

        w.write("" + i + "=" + mutationDetected + "\n");
        w.flush();

        System.out.println("Slave Mutation " + i + " of "
            + run.getEndMutation() + " detected = " + mutationDetected);
      } else {
        System.out.println("mutant i was not viable");
        w.write("" + i + "=" + true + "\n");
        w.flush();
      }
    }

    System.out.println(".....................");
  }

  public static void main(final String[] args) {

    addMemoryWatchDog();

    final File input = new File(args[0]);
    outputFile = new File(args[1]);
    final String classPathXML = args[2];

    System.out.println("Slave Input file is " + input);
    System.out.println("Slave Output file is " + input);

    Writer w = null;
    try {

      final DefaultPITClassloader loader = createClassLoader(classPathXML);
      IsolationUtils.setContextClassLoader(loader);

      final RunDetails run = readDetailsFromFile(input);

      final MutationConfig config = new MutationConfig(false, 100,
          Mutator.INCREMENTS, Mutator.RETURN_VALS);
      final Mutater m = config.createMutator();
      m.setRepository(new ClassLoaderRepository(loader));
      // final String name = run.getClassName();

      w = new OutputStreamWriter(new FileOutputStream(outputFile, false));
      final MutationTestSlave instance = new MutationTestSlave();
      instance.run(loader, run, w, m);
      w.close();
      w = null;

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

  private static RunDetails readDetailsFromFile(final File input)
      throws IOException {
    RunDetails rd = null;
    final BufferedReader br = new BufferedReader(new InputStreamReader(
        new FileInputStream(input)));
    try {

      rd = (RunDetails) IsolationUtils.fromTransportString(br.readLine());
      System.out.println("Running with " + rd);

    } finally {

      br.close();

    }

    return rd;

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

  private static boolean doTestsDetectMutation(final Container c,
      final ClassLoader loader, final List<TestUnit> tests) {
    try {
      final CheckTestHasFailedResultListener listener = new CheckTestHasFailedResultListener();

      final EmptyConfiguration conf = new EmptyConfiguration();

      final DefaultStaticConfig staticConfig = new DefaultStaticConfig();
      staticConfig.addTestListener(listener);
      final Pitest pit = new Pitest(staticConfig, conf);
      pit.run(c, tests);

      return listener.resultIndicatesSuccess();
    } catch (final Exception ex) {
      throw translateCheckedException(ex);
    }

  }

  private static DefaultPITClassloader createClassLoader(
      final String classPathXML) throws IOException {

    final String xml = IsolationUtils.decodeTransportString(classPathXML);

    final ClassPath cp = (ClassPath) IsolationUtils.fromXml(xml);

    final DefaultPITClassloader cl = new DefaultPITClassloader(cp,
        IsolationUtils.getContextClassLoader());
    return cl;
  }

}
