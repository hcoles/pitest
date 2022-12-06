package org.pitest.mutationtest.config;

import java.util.function.Predicate;
import org.pitest.classpath.ClassPathRoot;
public class TestClassPathPredicate implements Predicate<ClassPathRoot>{
    public TestClassPathPredicate() {}
    @Override
    public boolean test(final ClassPathRoot a) {
        return a.cacheLocation().isPresent()
            && !isADependencyPath(a.cacheLocation().get())
            && !isACodePath(a.cacheLocation().get());
    }

    private boolean isADependencyPath(final String path) {
        final String lowerCasePath = path.toLowerCase();
        return lowerCasePath.endsWith(".jar") || lowerCasePath.endsWith(".zip");
    }

    private boolean isACodePath(final String path) {
        final String lowerCasePath = path.toLowerCase();
        return path.endsWith("classes");
    }
}
