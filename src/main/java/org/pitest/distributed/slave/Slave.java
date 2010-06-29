package org.pitest.distributed.slave;

import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.pitest.distributed.ControlMessage;
import org.pitest.distributed.GroupState;
import org.pitest.distributed.HandlerNotificationMessage;
import org.pitest.distributed.SharedNames;
import org.pitest.distributed.message.RunDetails;
import org.pitest.distributed.message.TestGroupExecuteMessage;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

public class Slave {

  private final HazelcastInstance                      client;
  private final BlockingQueue<TestGroupExecuteMessage> queue;
  private final ITopic<HandlerNotificationMessage>     handlerNotificationTopic;
  private final ITopic<ControlMessage>                 controlTopic;
  private final TestGroupExecutor                      groupExecutor;

  // private final List<ControlMessage>

  public Slave() {
    this(Hazelcast.getDefaultInstance());
  }

  public Slave(final HazelcastInstance client) {
    this.client = client;
    this.queue = this.client.getQueue(SharedNames.TEST_REQUEST);

    this.handlerNotificationTopic = this.client
        .getTopic(SharedNames.TEST_HANDLER_NOTIFICATION);
    this.controlTopic = this.client.getTopic(SharedNames.TEST_CONTROL);
    this.groupExecutor = new TestGroupExecutor(client,
        new RemoteContainerCache(3));
  }

  public static void main(final String[] args) {
    final Slave slave = new Slave();
    slave.run();
  }

  public void run() {

    final InetSocketAddress myAddress = this.client.getCluster()
        .getLocalMember().getInetSocketAddress();

    try {
      while (true) {
        // FIXME queue read + topic write should be in 1 atomic transaction
        final TestGroupExecuteMessage message = this.queue.poll(1,
            TimeUnit.SECONDS);
        if (message != null) {
          this.handlerNotificationTopic
              .publish(new HandlerNotificationMessage(message.getRun(), message
                  .getId(), myAddress, GroupState.RECEIVED));
          this.groupExecutor.executeTestGroup(message.getRun(),
              message.getId(), message.getBytes(),
              this.handlerNotificationTopic, myAddress);
        }
      }
    } catch (final InterruptedException ex) {
      ex.printStackTrace();
    }

  }

  public void stop() {
    this.client.shutdown();
  }

  public void endRun(final RunDetails run) {

  }

}
