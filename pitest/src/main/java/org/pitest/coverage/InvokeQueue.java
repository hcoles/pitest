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

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.pitest.coverage.execute.InvokeReceiver;
import org.pitest.util.Log;

/**
 * @author ivanalx
 * @date 28.01.2009 14:35:42
 */
public class InvokeQueue implements InvokeReceiver {
  private final static Logger                   LOG          = Log.getLogger();
  private final ArrayBlockingQueue<InvokeEntry> invokesQueue = new ArrayBlockingQueue<InvokeEntry>(
                                                                 15000);

  public void addCodelineInvoke(final int classId, final int lineNumber) {
    boolean ok = false;
    int count = 0;
    while (!ok && (count < 5)) {
      ok = attemptToWriteToQueue(classId, lineNumber);
      count++;
    }

    if (!ok) {
      LOG.warning("Lost coverage of line " + lineNumber);
    }
  }

  private boolean attemptToWriteToQueue(final int classId, final int lineNumber) {
    try {
      return this.invokesQueue.offer(new InvokeEntry(classId, lineNumber), 500,
          TimeUnit.MILLISECONDS);
    } catch (final InterruptedException e) {
      return false;
    }
  }

  public boolean isEmpty() {
    return this.invokesQueue.isEmpty();
  }

  public Collection<InvokeEntry> poll(final int timeout)
      throws InterruptedException {
    final Collection<InvokeEntry> recipient = new ArrayList<InvokeEntry>();
    final InvokeEntry blockOnFirst = this.invokesQueue.poll(timeout,
        TimeUnit.MILLISECONDS);
    if (blockOnFirst != null) {
      recipient.add(blockOnFirst);
      this.invokesQueue.drainTo(recipient);
    }
    return recipient;
  }

  @Override
  public String toString() {
    return this.invokesQueue.toString();
  }

  public void registerClass(final int id, final String className) {
  }

}
