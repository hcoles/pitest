package com.example;

import java.io.IOException;

public class KeepAliveNonDaemon {

    public String run() {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    // block
                    System.in.read();
                } catch (IOException e) {
                    // swallow
                }
            }
        });
        t.setDaemon(false);
        t.start();

        // block the main thread as well, just for fun
       // try {
        //    System.in.read();
        //} catch (IOException e) {
            // swallow
       // }
        return "ran";
    }
}
