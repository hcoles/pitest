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
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.pitest.boot.HotSwapAgent;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassPathByteArraySource;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.coverage.CoverageTransformer;
import org.pitest.dependency.DependencyExtractor;
import org.pitest.functional.prelude.Prelude;
import org.pitest.help.PitHelpError;
import org.pitest.mutationtest.config.ClientPluginServices;
import org.pitest.mutationtest.config.MinionSettings;
import org.pitest.mutationtest.mocksupport.BendJavassistToMyWillTransformer;
import org.pitest.mutationtest.mocksupport.JavassistInputStreamInterceptorAdapater;
import org.pitest.testapi.Configuration;
import org.pitest.testapi.TestUnit;
import org.pitest.testapi.execute.FindTestUnits;
import org.pitest.util.ExitCode;
import org.pitest.util.Glob;
import org.pitest.util.IsolationUtils;
import org.pitest.util.Log;
import org.pitest.util.SafeDataInputStream;

import sun.pitest.CodeCoverageStore;

public class CoverageMinion {

  private static final Logger LOG = Log.getLogger();

  public static void main(final String[] args) {

    enablePowerMockSupport();

    ExitCode exitCode = ExitCode.OK;
    Socket s = null;
    CoveragePipe invokeQueue = null;
    try {

      final int port = Integer.parseInt(args[0]);
      s = new Socket("localhost", port);

      final SafeDataInputStream dis = new SafeDataInputStream(
          s.getInputStream());

      final CoverageOptions paramsFromParent = dis.read(CoverageOptions.class);

      Log.setVerbose(paramsFromParent.isVerbose());

      invokeQueue = new CoveragePipe(new BufferedOutputStream(
          s.getOutputStream()));

      CodeCoverageStore.init(invokeQueue);

      HotSwapAgent.addTransformer(new CoverageTransformer(
          convertToJVMClassFilter(paramsFromParent.getFilter())));

      final List<TestUnit> tus = getTestsFromParent(dis, paramsFromParent);

      LOG.info(tus.size() + " tests received");

      final CoverageWorker worker = new CoverageWorker(invokeQueue, tus);

      worker.run();

    } catch (final PitHelpError phe) {
      LOG.log(Level.SEVERE, phe.getMessage());
      exitCode = ExitCode.JUNIT_ISSUE;
    } catch (final Throwable ex) {
      ex.printStackTrace(System.out);
      LOG.log(Level.SEVERE, "Error calculating coverage. Process will exit.",
          ex);
      exitCode = ExitCode.UNKNOWN_ERROR;
    } finally {
      if (invokeQueue != null) {
        invokeQueue.end(exitCode);
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

  private static void enablePowerMockSupport() {
    // Bwahahahahahahaha
    HotSwapAgent.addTransformer(new BendJavassistToMyWillTransformer(Prelude
        .or(new Glob("javassist/*")),
        JavassistInputStreamInterceptorAdapater.inputStreamAdapterSupplier(JavassistCoverageInterceptor.class)));
  }

  private static Predicate<String> convertToJVMClassFilter(
      final Predicate<String> child) {
    return a -> child.test(a.replace("/", "."));
  }

  private static List<TestUnit> getTestsFromParent(
      final SafeDataInputStream dis, final CoverageOptions paramsFromParent)
      throws IOException {
    final List<ClassName> classes = receiveTestClassesFromParent(dis);
    Collections.sort(classes); // ensure classes loaded in a consistent order

    final Configuration testPlugin = createTestPlugin(paramsFromParent);
    verifyEnvironment(testPlugin);

    final List<TestUnit> tus = discoverTests(testPlugin, classes);

    final DependencyFilter filter = new DependencyFilter(
        new DependencyExtractor(new ClassPathByteArraySource(),
            paramsFromParent.getDependencyAnalysisMaxDistance()),
        paramsFromParent.getFilter());
    final List<TestUnit> filteredTus = filter
        .filterTestsByDependencyAnalysis(tus);

    LOG.info("Dependency analysis reduced number of potential tests by "
        + (tus.size() - filteredTus.size()));
    return filteredTus;

  }

  private static List<TestUnit> discoverTests(final Configuration testPlugin,
 final List<ClassName> classes) {
    final FindTestUnits finder = new FindTestUnits(testPlugin);
    final List<TestUnit> tus = finder
        .findTestUnitsForAllSuppliedClasses(classes.stream().flatMap(ClassName.nameToClass()).collect(Collectors.toList()));
    LOG.info("Found  " + tus.size() + " tests");
    return tus;
  }

  private static Configuration createTestPlugin(
      final CoverageOptions paramsFromParent) {
    final ClientPluginServices plugins = new ClientPluginServices(IsolationUtils.getContextClassLoader());
    final MinionSettings factory = new MinionSettings(plugins);
    final Configuration testPlugin = factory.getTestFrameworkPlugin(paramsFromParent.getPitConfig(), ClassloaderByteArraySource.fromContext());
    return testPlugin;
  }

  private static void verifyEnvironment(Configuration config) {
    LOG.info("Checking environment");
    if (config.verifyEnvironment().isPresent()) {
      throw config.verifyEnvironment().get();
    }
  }

  private static List<ClassName> receiveTestClassesFromParent(
      final SafeDataInputStream dis) {
    final int count = dis.readInt();
    LOG.fine("Expecting " + count + " tests classes from parent");
    final List<ClassName> classes = new ArrayList<>(count);
    for (int i = 0; i != count; i++) {
      classes.add(ClassName.fromString(dis.readString()));
    }
    LOG.fine("Tests classes received");

    return classes;
  }

}
