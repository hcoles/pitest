/*
 * Based on http://code.google.com/p/javacoveragent/ by
 * "alex.mq0" and "dmitry.kandalov"
 * 
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

package org.pitest.coverage;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author ivanalx
 * @date 28.01.2009 14:35:42
 */
public class InvokeQueue {
  private final Queue<InvokeEntry> invokesQueue = new ConcurrentLinkedQueue<InvokeEntry>();

  public void addCodelineInvoke(final int classId, final int lineNumber) {
    this.invokesQueue.add(new InvokeEntry(classId, lineNumber));
  }

  public boolean isEmpty() {
    return this.invokesQueue.isEmpty();
  }

  public int size() {
    return this.invokesQueue.size();
  }

  public InvokeEntry poll() {
    return this.invokesQueue.poll();
  }

  @Override
  public String toString() {
    return this.invokesQueue.toString();
  }

}
