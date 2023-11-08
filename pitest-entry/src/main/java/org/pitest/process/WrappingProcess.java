package org.pitest.process;

import java.io.IOException;

public interface WrappingProcess {

    static WrappingProcess create(int port, ProcessArgs args, Class<?> minionClass) {
        return new Java9Process(port, args, minionClass);
    }

    void start() throws IOException;

    boolean isAlive();

    void destroy();

    JavaProcess getProcess();
}
