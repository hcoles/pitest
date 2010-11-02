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
 * @date 28.01.2009 15:01:18
 */
public class InvokeQueueCleanerManager {
  public static void start(final InvokeQueue queue,
      final InvokeStatistics statistics, final int cleanerAmount) {
    for (int i = 0; i < cleanerAmount; i++) {
      final InvokeQueueCleaner cleaner = new InvokeQueueCleaner(queue,
          statistics, cleanerAmount);
      final Thread t = new Thread(cleaner);
      t.setDaemon(true);
      t.setName("CodeCoverageQueueCleaner");
      t.start();
    }
  }

  private InvokeQueueCleanerManager() {
  }
}
