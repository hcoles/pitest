package org.pitest.mutationtest.engine.gregor;

public class FieldInfo {
    private final int access;
    private final String name;
    private final String descriptor;
    private final String signature;
    private final Object value;

    public FieldInfo(int access, String name, String descriptor, String signature, Object value) {
        this.access = access;
        this.name = name;
        this.descriptor = descriptor;
        this.signature = signature;
        this.value = value;
    }

    public int access() {
        return access;
    }

    public String name() {
        return name;
    }

    public String descriptor() {
        return descriptor;
    }

    public String signature() {
        return signature;
    }

    public Object value() {
        return value;
    }
}
