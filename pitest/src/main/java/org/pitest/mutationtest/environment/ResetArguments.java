package org.pitest.mutationtest.environment;

import org.pitest.classinfo.ClassByteArraySource;

public class ResetArguments {

    private final ClassByteArraySource source;

    public ResetArguments(ClassByteArraySource source) {
        this.source = source;
    }

    public ClassByteArraySource source() {
        return this.source;
    }
}
