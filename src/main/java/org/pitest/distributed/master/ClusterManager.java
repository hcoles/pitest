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

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.SerializationUtils;
import org.pitest.TestGroup;
import org.pitest.distributed.HandlerNotificationMessage;
import org.pitest.distributed.SharedNames;
import org.pitest.distributed.message.RunDetails;
import org.pitest.distributed.message.TestGroupExecuteMessage;
import org.pitest.functional.Option;

import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import com.hazelcast.core.MessageListener;

public class ClusterManager implements
    MessageListener<HandlerNotificationMessage>, MembershipListener {

  private static class TestGroupMemberRecord {
    public TestGroupMemberRecord(final long id, final TestGroup testGroup,
        final Option<InetSocketAddress> handler) {
      this.id = id;
      this.group = testGroup;
      this.handler = handler;
    }

    public long                      id;
    public TestGroup                 group;
    public Option<InetSocketAddress> handler;
  }

  private final Map<Long, TestGroupMemberRecord>       inprogressTestGroupHandlers = new ConcurrentHashMap<Long, TestGroupMemberRecord>();

  private final ITopic<HandlerNotificationMessage>     notificationTopic;
  private final BlockingQueue<TestGroupExecuteMessage> queue;

  private final RunDetails                             run;
  private final HazelcastInstance                      hazelcast;
  private final Cluster                                cluster;

  private long                                         counter                     = 0;

  public ClusterManager(final RunDetails run, final HazelcastInstance hazelcast) {
    this.run = run;
    this.hazelcast = hazelcast;
    this.cluster = hazelcast.getCluster();
    this.notificationTopic = hazelcast
        .getTopic(SharedNames.TEST_HANDLER_NOTIFICATION);
    this.queue = hazelcast.getQueue(SharedNames.TEST_REQUEST);
  }

  public void start() {
    this.cluster.addMembershipListener(this);
    this.notificationTopic.addMessageListener(this);

    final MapConfig cacheConfig = this.hazelcast.getConfig().getMapConfig(
        this.run.getIdentifier());
    cacheConfig.setBackupCount(0);
    cacheConfig.setMaxIdleSeconds(5 * 60);
    cacheConfig.setEvictionPolicy("LRU");
    cacheConfig.setMaxSize(3000);

  }

  public void stop() {
    this.cluster.removeMembershipListener(this);
    this.notificationTopic.removeMessageListener(this);
  }

  public void onMessage(final HandlerNotificationMessage message) {
    if (message.getRun().equals(this.run)) {
      final TestGroupMemberRecord record = this.inprogressTestGroupHandlers
          .get(message.getTestGroupId());
      switch (message.getState()) {
      case RECEIVED:
        handleReceived(message, record);
        break;
      case COMPLETE:
        handleComplete(message, record);
        break;
      case ERROR:
        handleError(message, record);
        break;
      }
    }
  }

  private void handleComplete(final HandlerNotificationMessage message,
      final TestGroupMemberRecord record) {
    System.out.println("Group " + message.getTestGroupId() + " is complete ");
    this.inprogressTestGroupHandlers.remove(message.getTestGroupId());

  }

  private void handleError(final HandlerNotificationMessage message,
      final TestGroupMemberRecord record) {
    // FIXME should retry error cases at least once
    System.out.println("Error reported handling test group by peer at "
        + message.getHandler());
    this.inprogressTestGroupHandlers.remove(message.getTestGroupId());
  }

  private void handleReceived(final HandlerNotificationMessage message,
      final TestGroupMemberRecord record) {
    record.handler = Option.someOrNone(message.getHandler());
  }

  public long registerGroup(final TestGroup testGroup) {
    final long id = this.counter++;
    this.inprogressTestGroupHandlers.put(id, new TestGroupMemberRecord(id,
        testGroup, Option.<InetSocketAddress> none()));
    return id;
  }

  public boolean noTestsPending() {
    return this.inprogressTestGroupHandlers.isEmpty();
  }

  public void memberAdded(final MembershipEvent membershipEvent) {
    // nothing to do fro now

  }

  public void memberRemoved(final MembershipEvent membershipEvent) {
    if ((membershipEvent != null) && (membershipEvent.getMember() != null)) {

      final InetSocketAddress leaverAddress = membershipEvent.getMember()
          .getInetSocketAddress();
      for (final TestGroupMemberRecord each : this.inprogressTestGroupHandlers
          .values()) {
        if (each.handler.hasSome()) {
          if (each.handler.value().equals(leaverAddress)) {
            each.handler = Option.none();
            System.out.println("Reassigning group id " + each.id);
            submitTestGroupToGrid(each.id, each.group);
          }
        }
      }
    }

  }

  private void submitTestGroupToGrid(final TestGroupExecuteMessage message) {
    this.queue.add(message);

  }

  private void submitTestGroupToGrid(final long id, final TestGroup group) {
    submitTestGroupToGrid(new TestGroupExecuteMessage(this.run, id,
        testGroupToByteArray(group)));

  }

  public void submitTestGroupToGrid(final TestGroup testGroup) {
    submitTestGroupToGrid(registerGroup(testGroup), testGroup);
  }

  private byte[] testGroupToByteArray(final TestGroup each) {
    return SerializationUtils.serialize(each);
  }

  public void endRun() {
    this.hazelcast.getMap(this.run.getIdentifier()).destroy();
    this.hazelcast.shutdown();
  }

}
