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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.bcel.classfile.JavaClass;
import org.pitest.Description;
import org.pitest.TestGroup;
import org.pitest.TestResult;
import org.pitest.extension.Container;
import org.pitest.extension.ResultCollector;
import org.pitest.extension.ResultSource;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.NamedClassesIsolationStrategy;
import org.pitest.functional.predicate.Predicate;
import org.pitest.internal.ClassPath;
import org.pitest.internal.ConcreteResultCollector;
import org.pitest.internal.IsolationUtils;
import org.pitest.internal.TransformingClassLoaderFactory;
import org.pitest.mutationtest.loopbreak.PerContainerTimelimitCheck;

public class JumbleContainer implements Container {

  private final TransformingClassLoaderFactory factory;
  private final BlockingQueue<TestResult>      feedbackQueue = new ArrayBlockingQueue<TestResult>(
                                                                 BUFFER_SIZE);
  private final long                           normalExecutionTime;

  private static class ExitingResultCollector implements ResultCollector {

    private final ResultCollector child;
    private boolean               hadFailure = false;

    ExitingResultCollector(final ResultCollector child) {
      this.child = child;
    }

    public void notifyEnd(final Description description, final Throwable t) {
      this.child.notifyEnd(description, t);
      if (t != null) {
        this.hadFailure = true;
      }
    }

    public void notifyEnd(final Description description) {
      this.child.notifyEnd(description);
    }

    public void notifySkipped(final Description description) {
      this.child.notifySkipped(description);
    }

    public void notifyStart(final Description description) {
      this.child.notifyStart(description);
    }

    public boolean isHadFailure() {
      return this.hadFailure;
    }

  }

  public JumbleContainer(final ClassPath classPath, final JavaClass target,
      final long normalExecutionTime) {
    this.factory = new TransformingClassLoaderFactory(classPath,
        new JumbleTransformation(target), new NamedClassesIsolationStrategy(
            target.getClassName()));
    this.normalExecutionTime = normalExecutionTime;
  }

  public void setMaxThreads(final int maxThreads) {

  }

  public void shutdown() {

  }

  public void submit(final TestGroup group) {
    final ClassLoader cl = this.factory.get();
    final ExitingResultCollector rc = new ExitingResultCollector(
        new ConcreteResultCollector(this.feedbackQueue));

    if (this.normalExecutionTime > 0) {
      resetLoopBreakTimer(cl);
    }

    for (final TestUnit each : group) {
      each.execute(cl, rc);
      if (rc.isHadFailure()) {
        break;
      }
    }

  }

  public BlockingQueue<TestResult> feedbackQueue() {
    return this.feedbackQueue;
  }

  public boolean awaitCompletion() {
    return true;
  }

  public ResultSource getResultSource() {
    return new ResultSource() {

      public List<TestResult> getAvailableResults() {
        final List<TestResult> results = new ArrayList<TestResult>();
        JumbleContainer.this.feedbackQueue.drainTo(results);
        return results;
      }

      public boolean resultsAvailable() {
        return !JumbleContainer.this.feedbackQueue.isEmpty();
      }

    };
  }

  public void shutdownWhenProcessingComplete() {
    // TODO Auto-generated method stub

  }

  private void resetLoopBreakTimer(final ClassLoader loader) {
    final Class<?> c = IsolationUtils.convertForClassLoader(loader,
        PerContainerTimelimitCheck.class);
    final Predicate<Method> p = new Predicate<Method>() {

      public Boolean apply(final Method a) {
        return a.getName().equals("setMaxEndTime");
      }

    };
    final Method m = org.pitest.reflection.Reflection.publicMethod(c, p);

    final long maxEndTime = calculateMaxEndTime(this.normalExecutionTime);

    final Object[] params = { maxEndTime };
    try {
      m.invoke(null, params);
    } catch (final Exception e) {
      throw translateCheckedException(e);
    }

  }

  private long calculateMaxEndTime(final long normalExecution) {
    // TODO Auto-generated method stub
    return System.currentTimeMillis() + (normalExecution * 2) + 50;
  }

  public boolean canParallise() {
    return false;
  }

}
