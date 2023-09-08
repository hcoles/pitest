package org.pitest.mutationtest.incremental;

import org.pitest.classinfo.ClassName;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Project {
    private final Path root;
    private final List<Class<?>> code;
    private final List<Class<?>> tests;

    private final Set<ClassName> modified = new HashSet<>();

    public Project(Path root, List<Class<?>> code, List<Class<?>> tests) {
        this.root = root;
        this.code = new ArrayList<>(code);
        this.tests = new ArrayList<>(tests);
    }

    public String reportsDir() {
        return root.resolve("reports").toString();
    }

    public Path root() {
        return root;
    }

    public List<Class<?>> classes() {
        return code;
    }

    public List<Class<?>> tests() {
        return tests;
    }

    public void removeTest(Class<?> toRemove) {
        tests.remove(toRemove);
    }

    public Optional<ClassName> hasClass(ClassName clazz) {
        if (includes(classes(), clazz) || includes(tests(), clazz)) {
            return Optional.of(clazz);
        }
        return Optional.empty();
    }

    public boolean isModified(ClassName clazz) {
        return modified.contains(clazz);
    }

    private boolean includes(List<Class<?>> cs, ClassName clazz) {
        return cs.stream()
                .map(ClassName::fromClass)
                .anyMatch(c -> c.equals(clazz));
    }

    public void addTest(Class<?> test) {
        tests.add(test);
    }

    public void modifyClass(Class<?> clazz) {
        modified.add(ClassName.fromClass(clazz));
    }
}
