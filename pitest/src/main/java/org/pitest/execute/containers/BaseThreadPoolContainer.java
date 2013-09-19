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

package org.pitest.execute.containers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.pitest.execute.Container;
import org.pitest.execute.ResultSource;
import org.pitest.testapi.TestResult;
import org.pitest.testapi.TestUnit;

public class BaseThreadPoolContainer implements Container {

  private final ThreadPoolExecutor        executor;
  private final ClassLoaderFactory        loaderFactory;
  private final BlockingQueue<TestResult> feedbackQueue;

  public BaseThreadPoolContainer(final Integer threads,
      final ClassLoaderFactory loaderFactory, final ThreadFactory threadFactory) {
    this.executor = new ThreadPoolExecutor(threads, threads, 10,
        TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory);
    this.loaderFactory = loaderFactory;
    this.feedbackQueue = new ArrayBlockingQueue<TestResult>(BUFFER_SIZE);
  }

  private boolean awaitTermination(final int i, final TimeUnit milliseconds)
      throws InterruptedException {
    return this.executor.awaitTermination(i, milliseconds);
  }

  public void shutdownWhenProcessingComplete() {
    this.executor.shutdown();
  }

  private void submit(final Runnable c) {
    this.executor.submit(c);
  }

  public void setMaxThreads(final int maxThreads) {
    this.executor.setCorePoolSize(maxThreads);
    this.executor.setMaximumPoolSize(maxThreads);

  }

  public void submit(final TestUnit c) {
    this.submit(new TestUnitExecutor(this.loaderFactory, c, this.feedbackQueue));
  }

  public boolean awaitCompletion() {
    try {
      return awaitTermination(10, TimeUnit.MILLISECONDS);
    } catch (final InterruptedException e) {
      // swallow
    }
    return false;
  }

  public ResultSource getResultSource() {
    return new ResultSource() {

      public List<TestResult> getAvailableResults() {
        final List<TestResult> results = new ArrayList<TestResult>();
        BaseThreadPoolContainer.this.feedbackQueue.drainTo(results);
        return results;
      }

      public boolean resultsAvailable() {
        return !BaseThreadPoolContainer.this.feedbackQueue.isEmpty();
      }

    };
  }

}