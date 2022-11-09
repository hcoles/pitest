package org.pitest.filters.verification.equivalent;

import java.util.Set;

public class HasSetOf {
    public Set<String> mutateMe() {
        return Set.of();
    }
}
