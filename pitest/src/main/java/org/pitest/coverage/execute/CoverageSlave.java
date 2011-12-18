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
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.pitest.Pitest;
import org.pitest.boot.CodeCoverageStore;
import org.pitest.boot.HotSwapAgent;
import org.pitest.classinfo.ClassName;
import org.pitest.coverage.CoverageTransformer;
import org.pitest.dependency.DependencyExtractor;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.UnGroupedStrategy;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.predicate.Predicate;
import org.pitest.internal.ClassPath;
import org.pitest.internal.ClassPathByteArraySource;
import org.pitest.util.ExitCode;
import org.pitest.util.Functions;
import org.pitest.util.Log;
import org.pitest.util.SafeDataInputStream;
import org.pitest.util.Unchecked;

public class CoverageSlave {

  private final static Logger LOG = Log.getLogger();

  public static void main(final String[] args) {

    ExitCode exitCode = ExitCode.OK;
    Socket s = null;
    CoveragePipe invokeQueue = null;
    try {

      final int port = Integer.valueOf(args[0]);
      s = new Socket("localhost", port);

      final SafeDataInputStream dis = new SafeDataInputStream(
          s.getInputStream());

      final CoverageOptions paramsFromParent = dis.read(CoverageOptions.class);

      Log.setVerbose(paramsFromParent.isVerbose());

      final DataOutputStream dos = new DataOutputStream(
          new BufferedOutputStream(s.getOutputStream()));

      invokeQueue = new CoveragePipe(dos);

      CodeCoverageStore.init(invokeQueue);

      HotSwapAgent.addTransformer(new CoverageTransformer(
          convertToJVMClassFilter(paramsFromParent.getFilter())));

      final List<TestUnit> tus = getTestsFromParent(dis, paramsFromParent);

      LOG.info(tus.size() + " tests received");

      final CoverageWorker worker = new CoverageWorker(invokeQueue, tus);

      worker.run();

    } catch (final Throwable ex) {
      LOG.log(Level.SEVERE, "Error calculating coverage. Process will exit.",
          ex);
      ex.printStackTrace();
      exitCode = ExitCode.UNKNOWN_ERROR;
    } finally {
      if (invokeQueue != null) {
        invokeQueue.end();
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

  private static Predicate<String> convertToJVMClassFilter(
      final Predicate<String> child) {
    return new Predicate<String>() {
      public Boolean apply(final String a) {
        return child.apply(a.replace("/", "."));
      }

    };
  }

  private static List<TestUnit> getTestsFromParent(
      final SafeDataInputStream dis, final CoverageOptions paramsFromParent)
      throws IOException {
    final List<ClassName> classes = receiveTestClassesFromParent(dis);

    final List<TestUnit> tus = discoverTests(paramsFromParent, classes);

    final List<TestUnit> filteredTus = filterTestsByDependencyAnalysis(
        paramsFromParent, tus);

    LOG.info("Dependency analysis reduced number of potential tests by "
        + (tus.size() - filteredTus.size()));
    return filteredTus;

  }

  private static List<TestUnit> discoverTests(
      final CoverageOptions paramsFromParent, final List<ClassName> classes) {
    final List<TestUnit> tus = Pitest.findTestUnitsForAllSuppliedClasses(
        paramsFromParent.getPitConfig(), new UnGroupedStrategy(),
        FCollection.flatMap(classes, Functions.nameToClass()));
    LOG.info("Found  " + tus.size() + " tests");
    return tus;
  }

  private static List<ClassName> receiveTestClassesFromParent(
      final SafeDataInputStream dis) {
    final int count = dis.readInt();
    final List<ClassName> classes = new ArrayList<ClassName>(count);
    for (int i = 0; i != count; i++) {
      classes.add(new ClassName(dis.readString()));
    }
    LOG.fine("Receiving " + count + " tests classes from parent");
    return classes;
  }

  private static List<TestUnit> filterTestsByDependencyAnalysis(
      final CoverageOptions paramsFromParent, final List<TestUnit> tus) {
    final ClassPath cp = new ClassPath();
    final int maxDistance = paramsFromParent.getDependencyAnalysisMaxDistance();
    if (maxDistance < 0) {
      return tus;
    } else {
      return FCollection.filter(tus,
          isWithinReach(maxDistance, paramsFromParent, cp));
    }
  }

  private static F<TestUnit, Boolean> isWithinReach(final int maxDistance,
      final CoverageOptions paramsFromParent, final ClassPath classPath) {
    final DependencyExtractor analyser = new DependencyExtractor(
        new ClassPathByteArraySource(classPath), maxDistance);

    return new F<TestUnit, Boolean>() {
      private final Map<String, Boolean> cache = new HashMap<String, Boolean>();

      public Boolean apply(final TestUnit a) {
        final String each = a.getDescription().getFirstTestClass();
        try {
          boolean inReach;
          if (this.cache.containsKey(each)) {
            inReach = this.cache.get(each);
          } else {
            inReach = !analyser.extractCallDependenciesForPackages(each,
                paramsFromParent.getFilter()).isEmpty();
            this.cache.put(each, inReach);
          }

          if (inReach) {
            return true;
          }
        } catch (final IOException e) {
          throw Unchecked.translateCheckedException(e);
        }

        return false;
      }

    };
  }

}
