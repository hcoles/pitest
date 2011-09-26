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

import java.util.ArrayList;
import java.util.List;

import org.pitest.distributed.message.RunDetails;
import org.pitest.functional.Option;

public class RemoteContainerCache {

  private final int                   maxSize;
  private final List<RemoteContainer> runs = new ArrayList<RemoteContainer>();

  public RemoteContainerCache(final int maxSize) {
    this.maxSize = maxSize;
  }

  public Option<RemoteContainer> getCachedContainer(final RunDetails run) {
    for (final RemoteContainer each : this.runs) {
      if (each.getRun().equals(run)) {
        return Option.some(each);
      }
    }
    return Option.none();
  }

  public void enqueue(final RemoteContainer container) {
    if (this.runs.size() >= this.maxSize) {
      final RemoteContainer dead = this.runs.get(0);
      this.runs.remove(0);
      dead.destroy();
    }
    this.runs.add(container);
  }

}
