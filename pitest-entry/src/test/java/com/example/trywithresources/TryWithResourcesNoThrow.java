package com.example.trywithresources;

import java.util.Map;

public class TryWithResourcesNoThrow {

    private final Map<String, ThreadLocal<Integer>> threadLocalMap;

    TryWithResourcesNoThrow(final Map<String, ThreadLocal<Integer>> threadLocalMap) {
        this.threadLocalMap = threadLocalMap;
    }

    public int usingTryWithResources() {
        final ThreadLocal<Integer> threadLocal = threadLocalMap.get("Value1");
        try (final NoThrowAutoClosableResource myAutoClosable = new NoThrowAutoClosableResource()) {
            return threadLocal.get();
        } finally {
            System.out.println("mutate me");
            threadLocal.remove();
        }
    }


}

