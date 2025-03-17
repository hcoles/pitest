package org.pitest.mutationtest.engine.gregor;

public enum UrlType {

    DOC(0), DETAIL(1), CONTEXTUAL(2);

    final int priority;

    UrlType(int priority) {
        this.priority = priority;
    }

    public int priority() {
        return this.priority;
    }
}
