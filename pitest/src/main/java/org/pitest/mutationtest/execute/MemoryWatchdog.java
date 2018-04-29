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

package org.pitest.mutationtest.execute;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;

import javax.management.NotificationEmitter;
import javax.management.NotificationListener;

public class MemoryWatchdog {
  // private static final Logger LOG = Log.getLogger();

  public static void addWatchDogToAllPools(final long threshold,
      final NotificationListener listener) {
    final MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
    final NotificationEmitter ne = (NotificationEmitter) memBean;

    ne.addNotificationListener(listener, null, null);

    final List<MemoryPoolMXBean> memPools = ManagementFactory
        .getMemoryPoolMXBeans();
    for (final MemoryPoolMXBean mp : memPools) {
      if (mp.isUsageThresholdSupported()) {
        final MemoryUsage mu = mp.getUsage();
        final long max = mu.getMax();
        final long alert = (max * threshold) / 100;
        // LOG.info("Setting a threshold shutdown on pool: " + mp.getName()
        // + " for: " + alert);
        mp.setUsageThreshold(alert);

      }
    }
  }

}
