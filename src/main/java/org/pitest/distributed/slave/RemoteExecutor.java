// Copyright 2010 Henry Coles
// Licensed under the Apache License, Version 2.0 (the "License"); 
// you may not use this file except in compliance with the License. 
// You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
// Unless required by applicable law or agreed to in writing, 
// software distributed under the License is distributed on an "AS IS" BASIS, 
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and limitations under the License. 

package org.pitest.distributed.slave;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import org.pitest.TestGroup;
import org.pitest.distributed.ResultMessage;
import org.pitest.distributed.SharedNames;
import org.pitest.distributed.message.RunDetails;
import org.pitest.extension.ResultCollector;
import org.pitest.extension.TestUnit;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

public class RemoteExecutor implements Runnable {

  private final byte[]            group;
  private final RunDetails        run;
  private final ClassLoader       loader;
  private final HazelcastInstance hazelcast;

  public RemoteExecutor(final byte[] group, final RunDetails run,
      final HazelcastInstance hazelcast, final ClassLoader loader) {
    this.group = group;
    this.run = run;
    this.hazelcast = hazelcast;
    this.loader = loader;
  }

  public void run() {
    final ITopic<ResultMessage> topic = this.hazelcast
        .getTopic(SharedNames.TEST_RESULTS);
    final ResultCollector rc = new SlaveResultCollector(this.run, topic);
    Thread.currentThread().setContextClassLoader(this.loader);
    final TestGroup deserializedGroup = bytesToTestGroup(this.group,
        this.loader);
    for (final TestUnit each : deserializedGroup) {
      each.execute(this.loader, rc);
    }

  }

  private TestGroup bytesToTestGroup(final byte[] bytes, final ClassLoader cl) {
    try {
      final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
      final ObjectInputStream ois = new ForeignClassLoaderObjectInputStream(
          bis, cl);
      final TestGroup tu = (TestGroup) ois.readObject();
      ois.close();
      return tu;
    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    }
  }

}
