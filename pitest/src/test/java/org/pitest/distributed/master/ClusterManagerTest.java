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
package org.pitest.distributed.master;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.distributed.message.HandlerNotificationMessage;
import org.pitest.distributed.message.RunDetails;
import org.pitest.distributed.message.TestGroupExecuteMessage;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

public class ClusterManagerTest {

  private ClusterManager                         testee;

  @Mock
  private HazelcastInstance                      hazelcast;
  @Mock
  private Cluster                                cluster;
  @Mock
  private ITopic<HandlerNotificationMessage>     notificationTopic;
  @Mock
  private BlockingQueue<TestGroupExecuteMessage> queue;
  @Mock
  private Config                                 config;
  @Mock
  private MapConfig                              mapConfig;

  private RunDetails                             run;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.run = new RunDetails(new InetSocketAddress(0), 1);

    when(this.hazelcast.getConfig()).thenReturn(this.config);
    when(this.config.getMapConfig(anyString())).thenReturn(this.mapConfig);

    this.testee = new ClusterManager(this.run, this.hazelcast, this.cluster,
        this.notificationTopic, this.queue);
  }

  @Test
  public void shouldBeAddedAsListenerForMembershipEvents() {
    this.testee.start();
    verify(this.cluster).addMembershipListener(this.testee);
  }

  @Test
  public void shouldBeAddedAsListenerForNotificationMessages() {
    this.testee.start();
    verify(this.notificationTopic).addMessageListener(this.testee);
  }

}
