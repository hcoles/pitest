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

package org.pitest.containers;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.pitest.TestGroup;
import org.pitest.TestResult;
import org.pitest.extension.ClassLoaderFactory;
import org.pitest.extension.Container;
import org.pitest.internal.TestUnitExecutor;

public class BaseThreadPoolContainer implements Container {

  private final ThreadPoolExecutor        executor;
  private final ClassLoaderFactory        loaderFactory;
  private final BlockingQueue<TestResult> feedbackQueue;

  public BaseThreadPoolContainer(final Integer threads,
      final ClassLoaderFactory loaderFactory, final ThreadFactory threadFactory) {
    this.executor = new ThreadPoolExecutor(threads, threads, 10,
        TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory);
    this.loaderFactory = loaderFactory;
    this.feedbackQueue = new LinkedBlockingQueue<TestResult>();
  }

  public boolean awaitTermination(final int i, final TimeUnit milliseconds)
      throws InterruptedException {
    return this.executor.awaitTermination(i, milliseconds);
  }

  public void shutdown() {
    this.executor.shutdown();
  }

  protected void submit(final Runnable c) {
    this.executor.submit(c);
  }

  protected ClassLoaderFactory loaderFactory() {
    return this.loaderFactory;
  }

  public void setMaxThreads(final int maxThreads) {
    this.executor.setCorePoolSize(maxThreads);
    this.executor.setMaximumPoolSize(maxThreads);

  }

  public void submit(final TestGroup c) {
    this
        .submit(new TestUnitExecutor(this.loaderFactory, c, this.feedbackQueue));
  }

  public BlockingQueue<TestResult> feedbackQueue() {
    return this.feedbackQueue;
  }

}