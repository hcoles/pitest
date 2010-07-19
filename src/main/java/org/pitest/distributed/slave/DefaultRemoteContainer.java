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

package org.pitest.distributed.slave;

import static org.pitest.util.Unchecked.translateCheckedException;

import java.lang.reflect.Method;
import java.util.Map;

import org.pitest.TestGroup;
import org.pitest.distributed.DistributedCacheRoot;
import org.pitest.distributed.RemoteRoot;
import org.pitest.distributed.ResourceCache;
import org.pitest.distributed.master.MasterService;
import org.pitest.distributed.message.RunDetails;
import org.pitest.extension.ResultSource;
import org.pitest.extension.common.AllwaysIsolateStrategy;
import org.pitest.internal.ClassPath;
import org.pitest.internal.IsolationUtils;
import org.pitest.internal.classloader.TransformingClassLoader;
import org.pitest.internal.transformation.EnvironmentAccessTransformation;
import org.pitest.internal.transformation.IsolatedSystem;
import org.pitest.reflection.Reflection;

import com.hazelcast.core.HazelcastInstance;

public class DefaultRemoteContainer implements RemoteContainer {

  private final HazelcastInstance   hazelcast;
  private final RunDetails          run;
  private final ClassLoader         loader;
  private final Map<String, String> environment;
  private final ResourceCache       resourceCache;

  public DefaultRemoteContainer(final RunDetails run,
      final HazelcastInstance hazelcast, final MasterService master,
      final ResourceCache cache, final Map<String, String> environment) {
    this(run, hazelcast, master, cache, new TransformingClassLoader(
        new ClassPath(new DistributedCacheRoot(hazelcast
            .<String, byte[]> getMap(run.getIdentifier())), new RemoteRoot(
            master, cache)), new EnvironmentAccessTransformation(),
        new AllwaysIsolateStrategy(), Thread.currentThread()
            .getContextClassLoader()), environment);
  }

  public DefaultRemoteContainer(final RunDetails run,
      final HazelcastInstance hazelcast, final MasterService master,
      final ResourceCache cache, final ClassLoader loader,
      final Map<String, String> environment) {
    // this.master = master;
    this.loader = loader;
    this.environment = environment;
    this.resourceCache = cache;
    this.run = run;
    this.hazelcast = hazelcast;
    initEnvironment(loader);
  }

  public void setMaxThreads(final int maxThreads) {
    // TODO Auto-generated method stub

  }

  public void shutdownWhenProcessingComplete() {
    // TODO Auto-generated method stub

  }

  public void submit(final String testGroupXML) {
    final RemoteExecutor r = new RemoteExecutor(testGroupXML, this.run,
        this.hazelcast, this.loader);
    // use a new thread to ensure that tests run with correct
    // context class loader but framework code retains it's own
    final Thread t = new Thread(r);
    t.start();
    while (t.isAlive()) {
      try {
        t.join(30 * 1000);
      } catch (final InterruptedException ex) {
        // swallow
      }
    }

  }

  private void initEnvironment(final ClassLoader classLoader) {
    final Method m = IsolationUtils.convertForClassLoader(classLoader,
        Reflection.publicMethod(IsolatedSystem.class, "setProperty"));
    try {
      for (final String key : this.environment.keySet()) {
        if (key != null) {
          final String value = this.environment.get(key);
          if (value != null) {
            m.invoke(null, key, value);
          }
        }

      }
    } catch (final Exception ex) {
      throw translateCheckedException(ex);
    }

  }

  public RunDetails getRun() {
    return this.run;
  }

  public boolean awaitCompletion() {
    return true;
  }

  public ResultSource getResultSource() {
    return null;
  }

  public void destroy() {
    this.resourceCache.destroy();
  }

  public void submit(final TestGroup c) {
    // TODO Auto-generated method stub

  }

}
