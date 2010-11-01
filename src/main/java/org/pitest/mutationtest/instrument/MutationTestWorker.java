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

import java.io.IOException;
import java.util.List;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.util.ClassLoaderRepository;
import org.pitest.DefaultStaticConfig;
import org.pitest.Pitest;
import org.pitest.containers.UnContainer;
import org.pitest.extension.Container;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.EmptyConfiguration;
import org.pitest.functional.F2;
import org.pitest.internal.ConcreteResultCollector;
import org.pitest.internal.IsolationUtils;
import org.pitest.mutationtest.CheckTestHasFailedResultListener;
import org.pitest.mutationtest.ExitingResultCollector;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.util.ExitCodes;

import com.reeltwo.jumble.mutation.Mutater;

public class MutationTestWorker {

  private final List<TestUnit> tests;
  private final MutationConfig mutationConfig;
  private final ClassLoader    loader;

  public MutationTestWorker(final List<TestUnit> tests,
      final MutationConfig mutationConfig, final ClassLoader loader) {
    this.tests = tests;
    this.loader = loader;
    this.mutationConfig = mutationConfig;
  }

  protected void run(final F2<Class<?>, byte[], Boolean> hotswap,
      final int startMutation, final int endMutation, final String className,
      final Reporter r) throws IOException, ClassNotFoundException {

    System.out.println("Mutating class " + className);

    final Mutater m = this.mutationConfig.createMutator();
    m.setRepository(new ClassLoaderRepository(this.loader));
    final int maxMutation = m.countMutationPoints(className);

    final Class<?> testee = Class.forName(className, false, IsolationUtils
        .getContextClassLoader());

    for (int i = startMutation; (i <= endMutation) && (i != maxMutation); i++) {
      System.out.println("Running mutation " + i);
      final Container c = new UnContainer() {
        @Override
        public void submit(final TestUnit group) {
          final ExitingResultCollector rc = new ExitingResultCollector(
              new ConcreteResultCollector(this.feedbackQueue));
          group.execute(IsolationUtils.getContextClassLoader(), rc);
        }
      };

      final JavaClass mutatedClass = getMutation(className, m, i);
      final String method = m.getMutatedMethodName(className);
      System.out.println("mutating method " + method);

      if (method.trim().equals("<clinit>()V")) {
        if (i != startMutation) {
          // TODO would be more efficient to kick
          // off tests in a new classloader
          System.exit(ExitCodes.FORCED_EXIT);
        }
      }

      boolean mutationDetected = false;
      if (hotswap.apply(testee, mutatedClass.getBytes())) {
        mutationDetected = doTestsDetectMutation(c, this.loader, this.tests);
      } else {
        System.out.println("Mutation " + i + " of " + endMutation
            + " was not viable ");
        mutationDetected = true;
      }
      r.report(i, mutationDetected, mutatedClass, m, className);

      System.out.println("Mutation " + i + " of " + endMutation
          + " detected = " + mutationDetected);
    }

    System.out.println(".....................");
  }

  protected JavaClass getMutation(final String className, final Mutater m,
      final int i) throws ClassNotFoundException {

    m.setMutationPoint(i);
    final JavaClass mutatedClass = m.jumbler(className);
    return mutatedClass;
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

}
