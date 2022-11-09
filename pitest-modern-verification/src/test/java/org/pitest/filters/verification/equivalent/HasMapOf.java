package org.pitest.filters.verification.equivalent;

import java.util.Map;

public class HasMapOf {

    public Map<String,String> mutateMe() {
        return Map.of();
    }
}
