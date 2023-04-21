package org.pitest.mutationtest.engine.gregor;

public class AnnotationInfo {
    private final String descriptor;
    private final boolean visible;

    public AnnotationInfo(String descriptor, boolean visible) {
        this.descriptor = descriptor;
        this.visible = visible;
    }

    public String descriptor() {
        return descriptor;
    }

    public boolean isVisible() {
        return visible;
    }
}
