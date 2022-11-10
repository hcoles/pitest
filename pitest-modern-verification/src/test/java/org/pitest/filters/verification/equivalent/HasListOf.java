package org.pitest.filters.verification.equivalent;

import java.util.List;

public class HasListOf {
    public List<String> mutateMe() {
        return List.of();
    }
}