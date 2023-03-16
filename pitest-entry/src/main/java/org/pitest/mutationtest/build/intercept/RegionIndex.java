package org.pitest.mutationtest.build.intercept;

public class RegionIndex {
    private final int start;
    private final int end;

    public RegionIndex(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int start() {
        return start;
    }

    public int end() {
        return end;
    }
}
