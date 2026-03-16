package com.example;

import org.junit.Test;

public class KeepAliveNonDaemonTest {

    @Test
    public void testNonDaemon() {
        new KeepAliveNonDaemon().run();
    }
}
