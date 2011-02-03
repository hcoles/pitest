package org.pitest.distributed.slave.client;

import static org.pitest.util.Unchecked.translateCheckedException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.logging.Logger;

import org.pitest.distributed.master.MasterService;
import org.pitest.distributed.message.RunDetails;
import org.pitest.functional.Option;

import com.hazelcast.core.DistributedTask;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.util.concurrent.ConcurrentSkipListSet;

public class MasterClient implements MasterService, Serializable {

  private static final Logger            LOGGER                = Logger
                                                                   .getLogger(MasterClient.class
                                                                       .getName());

  private static final long              serialVersionUID      = 1L;

  private transient HazelcastInstance    hazelcast;
  private transient IMap<String, byte[]> classPathCache;

  private final RunDetails               run;

  private final Set<String>              knownMissingResources = new ConcurrentSkipListSet<String>();

  public MasterClient(final HazelcastInstance hazelcast, final RunDetails run) {
    this.hazelcast = hazelcast;
    this.run = run;
    this.classPathCache = hazelcast.getMap(run.getIdentifier());
  }

  public byte[] getClasspathData(final String name) throws IOException {

    final FutureTask<byte[]> task = new DistributedTask<byte[]>(
        new GetClasspathDataCallable(this.run, name), getMaster(this.run));

    final ExecutorService executorService = this.hazelcast.getExecutorService();
    executorService.execute(task);

    try {
      final byte[] bytes = task.get();
      if (bytes != null) {
        this.classPathCache.putIfAbsent(name, bytes);
      }
      return bytes;

    } catch (final InterruptedException ex) {
      throw translateCheckedException(ex);
    } catch (final ExecutionException ex) {
      throw translateCheckedException(ex);
    }
  }

  private Member getMaster(final RunDetails run) {
    for (final Member member : this.hazelcast.getCluster().getMembers()) {
      if (member.getInetSocketAddress().equals(run.getInetSocketAddress())) {
        return member;
      }
    }
    return null;
  }

  public Map<String, String> getEnvironmentSettings() {

    final FutureTask<Map<String, String>> task = new DistributedTask<Map<String, String>>(
        new GetEnvironmentCallable(this.run), getMaster(this.run));

    final ExecutorService executorService = this.hazelcast.getExecutorService();
    executorService.execute(task);

    try {
      return task.get();

    } catch (final InterruptedException ex) {
      throw translateCheckedException(ex);
    } catch (final ExecutionException ex) {
      throw translateCheckedException(ex);
    }
  }

  public Option<byte[]> getResourceData(final String name) {

    if (!this.knownMissingResources.contains(name)) {
      final Option<byte[]> remoteData = getRemoteResourceData(name);
      if (remoteData.hasNone()) {
        this.knownMissingResources.add(name);
      }
      return remoteData;
    } else {
      return Option.none();
    }

  }

  public HazelcastInstance getHazelCast() {
    return this.hazelcast;
  }

  private Option<byte[]> getRemoteResourceData(final String name) {
    LOGGER.info("Looking for remote resource " + name);
    final FutureTask<Option<byte[]>> task = new DistributedTask<Option<byte[]>>(
        new GetResourceDataCallable(this.run, name), getMaster(this.run));

    final ExecutorService executorService = this.hazelcast.getExecutorService();
    executorService.execute(task);

    try {
      return task.get();

    } catch (final InterruptedException ex) {
      throw translateCheckedException(ex);
    } catch (final ExecutionException ex) {
      throw translateCheckedException(ex);
    }
  }

  private void readObject(final ObjectInputStream aInputStream)
      throws ClassNotFoundException, IOException {

    aInputStream.defaultReadObject();

    // FIXME really should be serializing the config
    this.hazelcast = Hazelcast.getDefaultInstance();

    this.classPathCache = this.hazelcast.getMap(this.run.getIdentifier());

  }

}
