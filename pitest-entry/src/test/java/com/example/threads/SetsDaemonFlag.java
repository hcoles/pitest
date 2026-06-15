package com.example.threads;

public class SetsDaemonFlag {

    public static Thread createThread() {
        Thread thread = new Thread(() -> {
        });
        thread.setDaemon(true);
        return thread;
    }

}
