package org.pitest.util;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.util.List;

import org.junit.Test;

public class MemoryMonitorTest {
  @Test
  public void dumpMemoryInfo() {
    try {
      System.out.println("\nDUMPING MEMORY INFO\n");
      // Read MemoryMXBean
      final MemoryMXBean memorymbean = ManagementFactory.getMemoryMXBean();
      System.out.println("Heap Memory Usage: "
          + memorymbean.getHeapMemoryUsage());
      System.out.println("Non-Heap Memory Usage: "
          + memorymbean.getNonHeapMemoryUsage());

      // Read Garbage Collection information
      final List<GarbageCollectorMXBean> gcmbeans = ManagementFactory
          .getGarbageCollectorMXBeans();
      for (final GarbageCollectorMXBean gcmbean : gcmbeans) {
        System.out.println("\nName: " + gcmbean.getName());
        System.out.println("Collection count: " + gcmbean.getCollectionCount());
        System.out.println("Collection time: " + gcmbean.getCollectionTime());
        System.out.println("Memory Pools: ");
        final String[] memoryPoolNames = gcmbean.getMemoryPoolNames();
        for (final String memoryPoolName : memoryPoolNames) {
          System.out.println("\t" + memoryPoolName);
        }
      }

      // Read Memory Pool Information
      System.out.println("Memory Pools Info");
      final List<MemoryPoolMXBean> mempoolsmbeans = ManagementFactory
          .getMemoryPoolMXBeans();
      for (final MemoryPoolMXBean mempoolmbean : mempoolsmbeans) {
        System.out.println("\nName: " + mempoolmbean.getName());
        System.out.println("Usage: " + mempoolmbean.getUsage());
        System.out.println("Collection Usage: "
            + mempoolmbean.getCollectionUsage());
        System.out.println("Peak Usage: " + mempoolmbean.getPeakUsage());
        System.out.println("Type: " + mempoolmbean.getType());
        System.out.println("Memory Manager Names: ");
        final String[] memManagerNames = mempoolmbean.getMemoryManagerNames();
        for (final String memManagerName : memManagerNames) {
          System.out.println("\t" + memManagerName);
        }
        System.out.println("\n");
      }
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

}