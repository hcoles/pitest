package org.pitest.mutationtest.verify;

import java.util.Objects;
import java.util.Optional;

public final class BuildMessage implements Comparable<BuildMessage> {
    private final String text;
    private final String url;
    private final int priority;

    public BuildMessage(String text, String url, int priority) {
        this.text = text;
        this.url = url;
        this.priority = priority;
    }

    public static BuildMessage buildMessage(String text) {
        return new BuildMessage(text, null, 5);
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
        return text + Optional.ofNullable(url)
                .map( u -> " (" + u + ")").orElse("");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BuildMessage that = (BuildMessage) o;
        return priority == that.priority && Objects.equals(text, that.text) && Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, url, priority);
    }

    @Override
    public int compareTo(BuildMessage o) {
        return Integer.compare(this.priority, o.priority);
    }
}
