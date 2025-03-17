package org.pitest.mutationtest.engine.gregor;

import java.util.Objects;

public final class MutantUrl {
    private final UrlType type;
    private final String url;

    public MutantUrl(UrlType type, String url) {
        this.type = type;
        this.url = url;
    }

    public UrlType type() {
        return type;
    }

    public String url() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MutantUrl mutantUrl = (MutantUrl) o;
        return type == mutantUrl.type && Objects.equals(url, mutantUrl.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, url);
    }
}
