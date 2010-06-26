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

import org.pitest.TestGroup;
import org.pitest.functional.Option;

class TestGroupMemberRecord {
  public TestGroupMemberRecord(final long id, final TestGroup testGroup,
      final Option<InetSocketAddress> handler) {
    this.id = id;
    this.group = testGroup;
    this.handler = handler;
  }

  private long                      id;
  private TestGroup                 group;
  private Option<InetSocketAddress> handler;

  public long getId() {
    return this.id;
  }

  public void setId(final long id) {
    this.id = id;
  }

  public TestGroup getGroup() {
    return this.group;
  }

  public void setGroup(final TestGroup group) {
    this.group = group;
  }

  public Option<InetSocketAddress> getHandler() {
    return this.handler;
  }

  public void setHandler(final Option<InetSocketAddress> handler) {
    this.handler = handler;
  }

}