package com.example;

import java.io.IOException;

public class BlockMainThread {
    public String run(int i) {
        if ( i > 0) {
            try {
                System.in.read();
            } catch (IOException e) {
                //swallow
            }
        }
        return "ran";
    }
}
