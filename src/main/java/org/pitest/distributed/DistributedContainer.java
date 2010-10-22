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

package org.pitest.distributed;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.pitest.containers.BaseThreadPoolContainer;
import org.pitest.distributed.master.ClusterManager;
import org.pitest.distributed.master.MasterResultQueueListener;
import org.pitest.distributed.message.RunDetails;
import org.pitest.extension.ClassLoaderFactory;
import org.pitest.extension.TestUnit;
import org.pitest.internal.ClassPath;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class DistributedContainer extends BaseThreadPoolContainer {

  // static state required for funky executor based member
  // to member communication until hazelcast provides something
  // better
  private final static Map<RunDetails, DistributedContainer> CONTAINER_INSTANCES = new ConcurrentHashMap<RunDetails, DistributedContainer>();

  private final MasterResultQueueListener                    resultListener;
  private final RunDetails                                   runDetails;

  private final ClusterManager                               cluster;

  // child object?
  private final ClassPath                                    classPath;
  private final Map<String, String>                          environment;

  public DistributedContainer(final String... environmentExports) {
    this(new ClassPath(), Hazelcast.newHazelcastInstance(null),
        environmentExports);
  }

  public DistributedContainer(final Config config,
      final String... environmentExports) {
    this(new ClassPath(), Hazelcast.newHazelcastInstance(config),
        environmentExports);
  }

  public DistributedContainer(final ClassPath classpath,
      final HazelcastInstance hazelcast, final String... environmentExports) {
    super(1, new ClassLoaderFactory() {
      public ClassLoader get() {
        return this.getClass().getClassLoader();
      }
    }, Executors.defaultThreadFactory());

    this.classPath = classpath;

    final InetSocketAddress socket = hazelcast.getCluster().getLocalMember()
        .getInetSocketAddress();

    this.environment = createEnvironmentMap(environmentExports);

    this.runDetails = new RunDetails(socket, System.currentTimeMillis());

    this.cluster = new ClusterManager(this.runDetails, hazelcast);
    this.cluster.start();
    this.resultListener = new MasterResultQueueListener(this.runDetails,
        hazelcast, this.feedbackQueue());
    this.resultListener.start();

    CONTAINER_INSTANCES.put(this.runDetails, this);

  }

  public static DistributedContainer getInstanceForRun(final RunDetails run) {
    return CONTAINER_INSTANCES.get(run);
  }

  private Map<String, String> createEnvironmentMap(
      final String[] environmentExports) {
    final Map<String, String> map = new HashMap<String, String>();
    for (final String key : environmentExports) {
      map.put(key, System.getProperty(key));
    }
    return map;
  }

  @Override
  public void setMaxThreads(final int maxThreads) {
    // ignore
  }

  @Override
  public void shutdownWhenProcessingComplete() {
    super.shutdownWhenProcessingComplete();
    // nothing to do ??????
  }

  @Override
  public boolean awaitTermination(final int i, final TimeUnit milliseconds)
      throws InterruptedException {
    super.awaitTermination(i, milliseconds);
    final boolean complete = allTestsComplete();
    if (!complete) {
      try {
        Thread.sleep(i);
      } catch (final InterruptedException ex) {
        // swallow
      }
    } else {
      this.cluster.endRun();
    }

    return complete;

  }

  private boolean allTestsComplete() {
    return this.cluster.noTestsPending();
  }

  @Override
  public void submit(final TestUnit testGroup) {
    this.cluster.submitTestGroupToGrid(testGroup);
  }

  public ClassPath getClassPath() {
    return this.classPath;
  }

  public Map<String, String> getEnvironment() {
    return this.environment;
  }

}
