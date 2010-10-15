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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.distributed.ResourceCache;
import org.pitest.distributed.master.MasterService;
import org.pitest.distributed.message.RunDetails;
import org.pitest.functional.Option;
import org.pitest.internal.IsolationUtils;
import org.pitest.internal.classloader.DefaultPITClassloader;

import com.hazelcast.core.HazelcastInstance;

public class RemoteContainerCacheTest {

  private RemoteContainerCache testee;
  private RunDetails           runDetailsOne;
  private RunDetails           runDetailsTwo;
  private RunDetails           runDetailsThree;

  @Mock
  private HazelcastInstance    hazelcast;
  @Mock
  private ResourceCache        resourceCache;
  @Mock
  private MasterService        service;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.testee = new RemoteContainerCache(3);
    this.runDetailsOne = new RunDetails(new InetSocketAddress(0), 1);
    this.runDetailsTwo = new RunDetails(new InetSocketAddress(0), 2);
    this.runDetailsThree = new RunDetails(new InetSocketAddress(0), 3);

  }

  @Test
  public void testGetCachedContainerReturnsNoneIfRunNotPresent() {
    assertEquals(Option.none(), this.testee
        .getCachedContainer(this.runDetailsOne));
  }

  @Test
  public void testReturnsContainerAssignedToRun() {
    this.testee.enqueue(createContainer(this.runDetailsOne));
    final RemoteContainer expected = createContainer(this.runDetailsTwo);
    this.testee.enqueue(expected);
    this.testee.enqueue(createContainer(this.runDetailsThree));
    assertEquals(Option.someOrNone(expected), this.testee
        .getCachedContainer(this.runDetailsTwo));

  }

  @Test
  public void testfirstContainerRemovedFromCacheWhenLimitReached() {
    final RunDetails firstRun = new RunDetails(new InetSocketAddress(0), 4);
    final RemoteContainer firstContainer = mock(RemoteContainer.class);
    when(firstContainer.getRun()).thenReturn(firstRun);
    this.testee.enqueue(firstContainer);
    this.testee.enqueue(createContainer(this.runDetailsOne));
    this.testee.enqueue(createContainer(this.runDetailsTwo));
    this.testee.enqueue(createContainer(this.runDetailsThree));

    assertEquals(Option.none(), this.testee.getCachedContainer(firstRun));
    verify(firstContainer).destroy();

  }

  private RemoteContainer createContainer(final RunDetails run) {
    return new DefaultRemoteContainer(run, this.hazelcast, this.service,
        this.resourceCache, getClassLoader(), new HashMap<String, String>());
  }

  private DefaultPITClassloader getClassLoader() {
    return new DefaultPITClassloader(null, IsolationUtils
        .getContextClassLoader());
  }

}
