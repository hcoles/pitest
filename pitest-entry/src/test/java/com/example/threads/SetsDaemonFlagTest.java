package com.example.threads;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SetsDaemonFlagTest {
    @Test
    public void createThread() {
        var thread = SetsDaemonFlag.createThread();
        assertTrue(thread.isDaemon());
    }

}
