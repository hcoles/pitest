package org.pitest.mutationtest.verify;

import java.util.Objects;

public final class BuildIssue implements Comparable<BuildIssue> {
    private final String text;
    private final String url;
    private final int priority;

    public BuildIssue(String text, String url, int priority) {
        this.text = text;
        this.url = url;
        this.priority = priority;
    }

    public static BuildIssue issue(String text) {
        return new BuildIssue(text, null, 5);
    }

    public String text() {
        return text;
    }

    public String url() {
        return url;
    }

    public int priority() {
        return priority;
    }

    @Override
    public String toString() {
        return text + " (" + url + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BuildIssue that = (BuildIssue) o;
        return priority == that.priority && Objects.equals(text, that.text) && Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, url, priority);
    }

    @Override
    public int compareTo(BuildIssue o) {
        return Integer.compare(this.priority, o.priority);
    }
}
