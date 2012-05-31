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
package org.pitest.mutationtest.execute;

import java.io.IOException;
import java.lang.management.MemoryNotificationInfo;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;

import org.pitest.Pitest;
import org.pitest.boot.HotSwapAgent;
import org.pitest.classinfo.ClassName;
import org.pitest.extension.Configuration;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.UnGroupedStrategy;
import org.pitest.functional.F3;
import org.pitest.functional.FCollection;
import org.pitest.functional.Prelude;
import org.pitest.internal.ClassloaderByteArraySource;
import org.pitest.internal.IsolationUtils;
import org.pitest.mutationtest.instrument.TimeOutDecoratedTestSource;
import org.pitest.mutationtest.mocksupport.BendJavassistToMyWillTransformer;
import org.pitest.util.CommandLineMessage;
import org.pitest.util.ExitCode;
import org.pitest.util.Functions;
import org.pitest.util.Glob;
import org.pitest.util.Log;
import org.pitest.util.MemoryWatchdog;
import org.pitest.util.SafeDataInputStream;
import org.pitest.util.Unchecked;

public class MutationTestSlave {

  private static final Logger LOG = Log.getLogger();

  private final SafeDataInputStream        dis;
  private final Reporter reporter;

  public MutationTestSlave(final SafeDataInputStream dis, final Reporter reporter) {
    this.dis = dis;
    this.reporter = reporter;
  }

  public void run() {
    try {

      final SlaveArguments paramsFromParent = dis.read(SlaveArguments.class);

      Log.setVerbose(paramsFromParent.isVerbose());

      final F3<String, ClassLoader, byte[], Boolean> hotswap = new F3<String, ClassLoader, byte[], Boolean>() {

        public Boolean apply(final String clazzName, final ClassLoader loader,
            final byte[] b) {
          Class<?> clazz;
          try {
            clazz = Class.forName(clazzName, false, loader);
            return HotSwapAgent.hotSwap(clazz, b);
          } catch (final ClassNotFoundException e) {
            throw Unchecked.translateCheckedException(e);
          }

        }

      };



      final ClassLoader loader = IsolationUtils.getContextClassLoader();
      final MutationTestWorker worker = new MutationTestWorker(hotswap,
          paramsFromParent.engine.createMutator(new ClassloaderByteArraySource(
              loader)), loader);

      final List<TestUnit> tests = findTestsForTestClasses(loader,
          paramsFromParent.testClasses, paramsFromParent.pitConfig);

      worker.run(paramsFromParent.mutations, reporter, new TimeOutDecoratedTestSource(
          paramsFromParent.timeoutStrategy, tests, reporter));
      reporter.done(ExitCode.OK);
    } catch (final Throwable ex) {
      LOG.log(Level.WARNING, "Error during mutation test", ex);
      reporter.done(ExitCode.UNKNOWN_ERROR);
    }

  }

  public static void main(final String[] args) {

    enablePowerMockSupport();

    final int port = Integer.valueOf(args[0]);

    Socket s = null;
    try {
      s = new Socket("localhost", port);
      final SafeDataInputStream dis = new SafeDataInputStream(
          s.getInputStream());
      
      Reporter reporter = new DefaultReporter(s.getOutputStream());
      addMemoryWatchDog(reporter);
      
      final MutationTestSlave instance = new MutationTestSlave(dis, reporter);
      instance.run();
    } catch (final UnknownHostException ex) {
      LOG.log(Level.WARNING, "Error during mutation test", ex);
    } catch (final IOException ex) {
      LOG.log(Level.WARNING, "Error during mutation test", ex);
    } finally {
      if ( s != null ) {
        safelyCloseSocket(s);
      }
    }

  }

  private static List<TestUnit> findTestsForTestClasses(
      final ClassLoader loader, final Collection<ClassName> testClasses,
      final Configuration pitConfig) {
    final Collection<Class<?>> tcs = FCollection.flatMap(testClasses,
        Functions.nameToClass(loader));
    return Pitest.findTestUnitsForAllSuppliedClasses(pitConfig,
        new UnGroupedStrategy(), tcs);
  }

  @SuppressWarnings("unchecked")
  private static void enablePowerMockSupport() {
    // Bwahahahahahahaha
    HotSwapAgent.addTransformer(new BendJavassistToMyWillTransformer(Prelude
        .or(new Glob("javassist/*"))));
  }

  private static void safelyCloseSocket(final Socket s) {
    if (s != null) {
      try {
        s.close();
      } catch (final IOException e) {
        LOG.log(Level.WARNING, "Couldn't close scoket", e);
      }
    }
  }

  private static void addMemoryWatchDog(final Reporter r) {
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

          r.done(ExitCode.OUT_OF_MEMORY);

        } else {
          LOG.warning("Unknown notification: " + notification);
        }
      }

    };

    MemoryWatchdog.addWatchDogToAllPools(90, listener);

  }

}
