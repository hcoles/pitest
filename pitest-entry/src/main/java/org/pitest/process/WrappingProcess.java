package org.pitest.process;

import java.io.IOException;
import java.nio.file.Path;

public interface WrappingProcess {

    static WrappingProcess create(Path socketPath, ProcessArgs args, Class<?> minionClass) {
        return new Java9Process(socketPath, args, minionClass);
    }

    void start() throws IOException;

    boolean isAlive();

    void destroy();

    JavaProcess getProcess();
}
