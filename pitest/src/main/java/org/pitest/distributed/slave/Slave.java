package org.pitest.distributed.slave;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryUsage;
import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;

import org.pitest.distributed.GroupState;
import org.pitest.distributed.SharedNames;
import org.pitest.distributed.message.HandlerNotificationMessage;
import org.pitest.distributed.message.RunDetails;
import org.pitest.distributed.message.TestGroupExecuteMessage;
import org.pitest.util.CommandLineMessage;
import org.pitest.util.ExitCode;
import org.pitest.util.FileUtil;
import org.pitest.util.Log;
import org.pitest.util.MemoryWatchdog;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

public class Slave {
  private final static Logger                          LOG                                     = Log
                                                                                                   .getLogger();

  private static final int                             SUICIDE_MEMORY_LIMIT_PERCENT            = 98;
  private static final int                             ALLOWED_SUICIDE_BREACHES                = 0;

  private static final int                             GRACEFULL_SHUTDOWN_MEMORY_LIMIT_PERCENT = 80;
  private static final int                             ALLOWED_GRAEFUL_BREACHES                = 0;

  private static final int                             MINUTES                                 = 1000 * 60;

  private final HazelcastInstance                      client;
  private final BlockingQueue<TestGroupExecuteMessage> queue;
  private final ITopic<HandlerNotificationMessage>     handlerNotificationTopic;
  private final TestGroupExecutor                      groupExecutor;

  private volatile transient boolean                   shouldRun                               = true;

  private static final int                             MAX_IDLE_MINUTES                        = 30;

  public Slave() {
    this(Hazelcast.getDefaultInstance());
  }

  public Slave(final HazelcastInstance client) {
    this.client = client;
    this.queue = this.client.getQueue(SharedNames.TEST_REQUEST);

    this.handlerNotificationTopic = this.client
        .getTopic(SharedNames.TEST_HANDLER_NOTIFICATION);
    // this.controlTopic = this.client.getTopic(SharedNames.TEST_CONTROL);
    this.groupExecutor = new TestGroupExecutor(client,
        new RemoteContainerCache(3));
  }

  public static void main(final String[] args) {
    final Slave slave = new Slave();
    slave.run();
  }

  private void addMemorySuicideWatchDog() {

    final NotificationListener listener = new NotificationListener() {

      public void handleNotification(final Notification notification,
          final Object handback) {
        final String type = notification.getType();
        if (type.equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED)) {

          final CompositeData cd = (CompositeData) notification.getUserData();
          final MemoryNotificationInfo memInfo = MemoryNotificationInfo
              .from(cd);

          CommandLineMessage.report(memInfo.getPoolName()
              + " has exceeded the suicide shutdown threshold : "
              + memInfo.getCount() + " times.\n" + memInfo.getUsage());
          if (memInfo.getCount() > Slave.ALLOWED_SUICIDE_BREACHES) {
            killSelf();
          }

        } else {
          LOG.warning("Unknown notification: " + notification);
        }
      }

    };

    MemoryWatchdog
        .addWatchDogToAllPools(SUICIDE_MEMORY_LIMIT_PERCENT, listener);
  }

  public void run() {
    addMemoryGracefullShutDownWatchDog();
    addMemorySuicideWatchDog();

    final InetSocketAddress myAddress = this.client.getCluster()
        .getLocalMember().getInetSocketAddress();

    try {
      long lastMessageReceivedAt = System.currentTimeMillis();
      while (this.shouldRun) {
        // FIXME queue read + topic write should be in 1 atomic transaction
        final TestGroupExecuteMessage message = this.queue.poll(10,
            TimeUnit.SECONDS);
        if (message != null) {
          lastMessageReceivedAt = System.currentTimeMillis();
          this.handlerNotificationTopic
              .publish(new HandlerNotificationMessage(message.getRun(), message
                  .getId(), myAddress, GroupState.RECEIVED));
          this.groupExecutor.executeTestGroup(message.getRun(),
              message.getId(), message.getXML(), this.handlerNotificationTopic,
              myAddress);
        } else {
          if (lastMessageReceivedAt + (MAX_IDLE_MINUTES * MINUTES) < System
              .currentTimeMillis()) {
            CommandLineMessage
                .report("Restarting and cleaning cache as has been idle for "
                    + MAX_IDLE_MINUTES + " minutes");
            selfClean();
          }
        }

        reportMemory();
      }
    } catch (final OutOfMemoryError ome) {
      ome.printStackTrace();
    } catch (final InterruptedException ex) {
      ex.printStackTrace();
    }

    this.stop();

    CommandLineMessage.report("Slave shutting down with no pending work");
  }

  protected void requestShutdown() {
    CommandLineMessage.report("Shutdown requested");
    this.shouldRun = false;
  }

  private void addMemoryGracefullShutDownWatchDog() {
    final NotificationListener listener = new NotificationListener() {

      public void handleNotification(final Notification notification,
          final Object handback) {
        final String type = notification.getType();
        if (type.equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED)) {
          final CompositeData cd = (CompositeData) notification.getUserData();
          final MemoryNotificationInfo memInfo = MemoryNotificationInfo
              .from(cd);
          CommandLineMessage.report(memInfo.getPoolName()
              + " has exceeded the gracefull shutdown threshold : "
              + memInfo.getCount() + " times.\n" + memInfo.getUsage());
          if (memInfo.getCount() > Slave.ALLOWED_GRAEFUL_BREACHES) {
            Slave.this.requestShutdown();
          }

        } else {
          LOG.warning("Unknown notification: " + notification);
        }
      }

    };

    MemoryWatchdog.addWatchDogToAllPools(
        GRACEFULL_SHUTDOWN_MEMORY_LIMIT_PERCENT, listener);

  }

  private void reportMemory() {
    final MemoryUsage nonHeapUsage = ManagementFactory.getMemoryMXBean()
        .getNonHeapMemoryUsage();
    final MemoryUsage heapUsage = ManagementFactory.getMemoryMXBean()
        .getHeapMemoryUsage();
    CommandLineMessage.report("Non heap memory : " + nonHeapUsage + "\n"
        + "Heap memory : " + heapUsage);
  }

  private void selfClean() {
    requestShutdown();
    FileUtil.deleteDirectory(new File("classpathCache"));
  }

  private void killSelf() {
    CommandLineMessage.report("Killing self");
    stop();
    System.exit(ExitCode.OUT_OF_MEMORY.getCode());
  }

  public void stop() {
    // sometimes hazelcast blocks after shutdown. Start a new thread to
    // guarantee
    // the the machine dies.
    final Thread deathThread = new Thread() {
      @Override
      public void run() {
        try {
          Thread.sleep(10 * 1000);
          CommandLineMessage
              .report("Failed to shutdown after 10 seconds. Killing jvm.");
          System.exit(ExitCode.FORCED_EXIT.getCode());
        } catch (final InterruptedException e) {
          e.printStackTrace();
        }
      }

    };
    deathThread.setDaemon(true);
    deathThread.start();
    this.client.getLifecycleService().shutdown();

  }

  public void endRun(final RunDetails run) {

  }

}
