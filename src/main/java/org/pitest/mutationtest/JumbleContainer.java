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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.bcel.classfile.JavaClass;
import org.pitest.TestGroup;
import org.pitest.TestResult;
import org.pitest.extension.Container;
import org.pitest.extension.ResultSource;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.NamedClassesIsolationStrategy;
import org.pitest.internal.ClassPath;
import org.pitest.internal.ConcreteResultCollector;
import org.pitest.internal.TransformingClassLoaderFactory;

public class JumbleContainer implements Container {

  private final TransformingClassLoaderFactory factory;
  private final BlockingQueue<TestResult>      feedbackQueue = new LinkedBlockingQueue<TestResult>();

  public JumbleContainer(final ClassPath classPath, final JavaClass target) {
    this.factory = new TransformingClassLoaderFactory(classPath,
        new JumbleTransformation(target), new NamedClassesIsolationStrategy(
            target.getClassName()));
  }

  public void setMaxThreads(final int maxThreads) {

  }

  public void shutdown() {

  }

  public void submit(final TestGroup group) {
    final ClassLoader cl = this.factory.get();
    final ConcreteResultCollector rc = new ConcreteResultCollector(
        this.feedbackQueue);
    for (final TestUnit each : group) {
      each.execute(cl, rc);
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

}
