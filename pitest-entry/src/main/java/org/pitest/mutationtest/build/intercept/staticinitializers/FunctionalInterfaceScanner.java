package org.pitest.mutationtest.build.intercept.staticinitializers;

import org.objectweb.asm.tree.AnnotationNode;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classpath.CodeSource;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FunctionalInterfaceScanner implements Function<CodeSource, Set<String>> {
    @Override
    public Set<String> apply(CodeSource codeSource) {
        return codeSource.codeTrees()
                .filter(this::isFunctionalInterface)
                .map(c -> c.rawNode().name)
                .collect(Collectors.toSet());
    }

    private boolean isFunctionalInterface(ClassTree classTree) {
        List<AnnotationNode> annotations = classTree.rawNode().visibleAnnotations;
        if (annotations == null) {
            return false;
        }

        return annotations.stream()
                .anyMatch(a -> a.desc.equals("Ljava/lang/FunctionalInterface;"));
    }
}
