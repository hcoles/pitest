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
package org.pitest.distributed.slave.client;

import static org.junit.Assert.assertNotNull;

import java.net.InetSocketAddress;

import org.junit.Test;
import org.pitest.distributed.message.RunDetails;
import org.pitest.internal.IsolationUtils;

import com.hazelcast.core.Hazelcast;

public class MasterClientIntegrationTest {

  @Test
  // not a unit test performs network io
  public void shouldSerializeAndDeserializeViaXStreamWithoutError()
      throws Exception {
    final RunDetails rd = new RunDetails(new InetSocketAddress(0), 1);

    final MasterClient testee = new MasterClient(
        Hazelcast.getDefaultInstance(), rd);

    final MasterClient actual = (MasterClient) IsolationUtils.cloneForLoader(
        testee, IsolationUtils.getContextClassLoader());

    assertNotNull(actual.getHazelCast());
    assertNotNull(actual.getHazelCast().getCluster());
    // pass if we get here

  }

}
