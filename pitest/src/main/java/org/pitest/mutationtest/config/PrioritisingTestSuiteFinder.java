package org.pitest.mutationtest.config;

import org.pitest.testapi.TestSuiteFinder;

import java.util.Collections;
import java.util.List;

class PrioritisingTestSuiteFinder implements TestSuiteFinder {
    private final List<TestSuiteFinder> orderedChildren;

    PrioritisingTestSuiteFinder(List<TestSuiteFinder> orderedChildren) {
        this.orderedChildren = orderedChildren;
    }

    @Override
    public List<Class<?>> apply(Class<?> clazz) {
        for (TestSuiteFinder each : orderedChildren) {
            List<Class<?>> found = each.apply(clazz);
            if (!found.isEmpty()) {
                return found;
            }
        }
        return Collections.emptyList();
    }
}
