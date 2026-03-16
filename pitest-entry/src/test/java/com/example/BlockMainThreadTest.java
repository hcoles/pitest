package com.example;

import org.junit.Test;

public class BlockMainThreadTest {

    @Test
    public void testNonDaemon() {
        new BlockMainThread().run(0);
    }
}
