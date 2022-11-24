package org.pitest.coverage;

import org.objectweb.asm.tree.LineNumberNode;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classinfo.ClassName;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ClassLines {
    private final ClassName name;
    private final Set<Integer> codeLines;

    public ClassLines(ClassName name, Set<Integer> codeLines) {
        this.name = name;
        this.codeLines = codeLines;
    }

    public static ClassLines fromTree(ClassTree classTree) {
        Set<Integer> lines = classTree.methods().stream()
                .flatMap(m -> m.instructions().stream()
                        .filter(n -> n instanceof LineNumberNode)
                        .map(n -> ((LineNumberNode) n).line))
                .collect(Collectors.toSet());
        return new ClassLines(classTree.name(), lines);
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
