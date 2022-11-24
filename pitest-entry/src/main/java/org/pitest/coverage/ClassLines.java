package org.pitest.coverage;

import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classinfo.ClassName;

import java.util.Objects;
import java.util.Set;

public final class ClassLines {
    private final ClassName name;
    private final Set<Integer> codeLines;

    public ClassLines(ClassName name, Set<Integer> codeLines) {
        this.name = name;
        this.codeLines = codeLines;
    }

    public static ClassLines fromTree(ClassTree classTree) {
        return new ClassLines(classTree.name(), classTree.codeLineNumbers());
    }

    public ClassName name() {
        return name;
    }

    public int getNumberOfCodeLines() {
        return this.codeLines.size();
    }

    public boolean isCodeLine(final int line) {
        return this.codeLines.contains(line);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClassLines that = (ClassLines) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
