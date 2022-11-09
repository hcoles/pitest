package org.pitest.filters.verification.equivalent;

import java.util.Map;

public class NonEmptyMapOf {
    public Map<String,String> mutateMe() {
        return Map.of("A", "Z");
    }
}
