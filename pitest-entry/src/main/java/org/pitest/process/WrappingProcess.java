package org.pitest.process;

import java.io.IOException;

public interface WrappingProcess {

  static WrappingProcess create(int port, ProcessArgs args, Class<?> minionClass) {
    String javaVersion = System.getProperty("java.version");
    if (javaVersion.startsWith("8") || javaVersion.startsWith("1.8")) {
      return new LegacyProcess(port, args, minionClass);
    }

    return new Java9Process(port, args, minionClass);
  }


    void start() throws IOException;

    boolean isAlive();

    void destroy();

    JavaProcess getProcess();
}
