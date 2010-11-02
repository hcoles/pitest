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

package org.pitest.coverage.calculator;

/**
 * @author ivanalx
 * @date 28.01.2009 14:39:17
 */
public class InvokeQueueCleaner implements Runnable {
  private final InvokeQueue      queue;
  private final InvokeStatistics invokeStatistics;
  private int                    amountOfCleaner;

  public InvokeQueueCleaner(final InvokeQueue queue,
      final InvokeStatistics invokeStatistics, final int amount) {
    this.queue = queue;
    this.invokeStatistics = invokeStatistics;
    this.amountOfCleaner = amount;
  }

  public void run() {
    while (!Thread.interrupted()) {
      try {
        final int amount = this.queue.size() / this.amountOfCleaner + 10;
        for (int i = 0; i < amount; i++) {
          final InvokeEntry s = this.queue.poll();
          if (s != null) {
            switch (s.getType()) {
            case LINE: {
              this.invokeStatistics.visitLine(s.getClassId(), s.getCodeId());
              break;
            }
            case METHOD: {
              this.invokeStatistics.visitMethod(s.getClassId(), s.getCodeId());
              break;
            }
            }
          }
        }
        Thread.sleep(1);
      } catch (final Exception e) {
        // ignore
      }
    }
  }

  public void setAmountOfCleaner(final int amountOfCleaner) {
    this.amountOfCleaner = amountOfCleaner;
  }
}
