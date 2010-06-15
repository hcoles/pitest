package org.pitest.distributed.slave;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.pitest.distributed.DirectoryCache;
import org.pitest.distributed.GroupState;
import org.pitest.distributed.HandlerNotificationMessage;
import org.pitest.distributed.Listener;
import org.pitest.distributed.ResourceCache;
import org.pitest.distributed.SharedNames;
import org.pitest.distributed.master.MasterService;
import org.pitest.distributed.message.RunDetails;
import org.pitest.distributed.message.TestGroupExecuteMessage;
import org.pitest.distributed.slave.client.MasterClient;
import org.pitest.util.RingBuffer;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

public class SlaveListener implements Listener {

  private final RingBuffer<RemoteContainer> runs = new RingBuffer<RemoteContainer>(
                                                     6);

  private final HazelcastInstance           client;

  public SlaveListener() {
    this.client = Hazelcast.getDefaultInstance();
  }

  public SlaveListener(final HazelcastInstance client) {
    this.client = client;
  }

  public void executeTest(final RunDetails run, final long id,
      final byte[] testGroup,
      final ITopic<HandlerNotificationMessage> handlerNotificationTopic,
      final InetSocketAddress socket) {
    System.out.println("Thread is " + Thread.currentThread());
    try {
      final RemoteContainer container = getContainer(run);
      container.submit(testGroup);
      handlerNotificationTopic.publish(new HandlerNotificationMessage(run, id,
          socket, GroupState.COMPLETE));
      System.out.println("Just run test in group " + id);
    } catch (final Throwable t) {
      t.printStackTrace();
      handlerNotificationTopic.publish(new HandlerNotificationMessage(run, id,
          socket, GroupState.ERROR));
    }
  }

  private RemoteContainer getContainer(final RunDetails run) {

    // fixme thread safety
    if (this.getCachedContainer(run) == null) {
      System.out.println("Creating new container");
      final ResourceCache cache = new DirectoryCache(run);

      final MasterService master = new MasterClient(this.client, run);

      final Map<String, String> environment = master.getEnvironmentSettings();
      final RemoteContainer m = new RemoteContainer(run, this.client, master,
          cache, environment);

      this.runs.enqueue(m);

    }

    return this.getCachedContainer(run);

  }

  public RemoteContainer getCachedContainer(final RunDetails run) {
    for (final RemoteContainer container : this.runs) {
      if (container.getRun().equals(run)) {
        return container;
      }
    }
    return null;
  }

  public static void main(final String[] args) {
    final SlaveListener slave = new SlaveListener();
    slave.run();
  }

  public void run() {

    final InetSocketAddress myAddress = this.client.getCluster()
        .getLocalMember().getInetSocketAddress();

    final BlockingQueue<TestGroupExecuteMessage> queue = this.client
        .getQueue(SharedNames.TEST_REQUEST);

    final ITopic<HandlerNotificationMessage> handlerNotificationTopic = this.client
        .getTopic(SharedNames.TEST_HANDLER_NOTIFICATION);

    // final ITopic<ControlMessage> topic = Hazelcast
    // .getTopic(SharedNames.TEST_CONTROL);
    // topic.addMessageListener(this);

    try {
      while (true) {

        // FIXME queue read + topic write should be in 1 atomic transaction

        final TestGroupExecuteMessage message = queue.poll(1, TimeUnit.SECONDS);
        if (message != null) {
          handlerNotificationTopic
              .publish(new HandlerNotificationMessage(message.getRun(), message
                  .getId(), myAddress, GroupState.RECEIVED));
          executeTest(message.getRun(), message.getId(), message.getBytes(),
              handlerNotificationTopic, myAddress);
        }
      }
    } catch (final InterruptedException ex) {
      ex.printStackTrace();
    }

  }

  private void cleanupRun(final RunDetails run) {

    getContainer(run).shutdown();

  }

  public void stop() {
    this.client.shutdown();
  }

}
