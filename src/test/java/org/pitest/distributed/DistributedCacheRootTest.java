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
package org.pitest.distributed;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DistributedCacheRootTest {

  private DistributedCacheRoot testee;

  @Mock
  private Map<String, byte[]>  map;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    this.testee = new DistributedCacheRoot(this.map);
  }

  @Test
  public void testReturnsInputStreamWhenDataPresentInMap() throws Exception {
    when(this.map.get(anyString())).thenReturn("bar".getBytes());
    assertNotNull(this.testee.getData("foo"));
  }

  @Test
  public void testReturnsInputStreamWhenDataNotPresentInMap() throws Exception {
    assertNull(this.testee.getData("foo"));
  }
}
