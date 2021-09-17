package org.pitest.util;

import java.io.OutputStream;
import java.io.PrintStream;

public class Quiet {
    public static void disableStdOutAndErr() {
    System.setErr(new PrintStream(new OutputStream() {
        public void write(int b) {
        }
    }));

    System.setOut(new PrintStream(new OutputStream() {
        public void write(int b) {
        }
    }));
  }
}
