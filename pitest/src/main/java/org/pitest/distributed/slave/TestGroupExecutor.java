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

import java.net.InetSocketAddress;
import java.util.Map;

import org.pitest.distributed.DirectoryCache;
import org.pitest.distributed.GroupState;
import org.pitest.distributed.ResourceCache;
import org.pitest.distributed.master.MasterService;
import org.pitest.distributed.message.HandlerNotificationMessage;
import org.pitest.distributed.message.RunDetails;
import org.pitest.distributed.slave.client.MasterClient;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

public class TestGroupExecutor {

  private final HazelcastInstance    client;
  private final RemoteContainerCache cache;

  public TestGroupExecutor(final HazelcastInstance client,
      final RemoteContainerCache cache) {
    this.client = client;
    this.cache = cache;
  }

  public void executeTestGroup(final RunDetails run, final long id,
      final String testGroup,
      final ITopic<HandlerNotificationMessage> handlerNotificationTopic,
      final InetSocketAddress socket) {

    try {
      final RemoteContainer container = getContainer(run);
      container.submit(testGroup);
      handlerNotificationTopic.publish(new HandlerNotificationMessage(run, id,
          socket, GroupState.COMPLETE));

    } catch (final Throwable t) {
      t.printStackTrace();
      handlerNotificationTopic.publish(new HandlerNotificationMessage(run, id,
          socket, GroupState.ERROR));
    }
  }

  private RemoteContainer getContainer(final RunDetails run) {

    // fixme thread safety
    if (this.cache.getCachedContainer(run).hasNone()) {

      final ResourceCache cache = new DirectoryCache(run);
      final MasterService master = new MasterClient(this.client, run);

      final Map<String, String> environment = master.getEnvironmentSettings();
      final DefaultRemoteContainer m = new DefaultRemoteContainer(run,
          this.client, master, cache, environment);

      this.cache.enqueue(m);

    }

    return this.cache.getCachedContainer(run).value();

  }

}
