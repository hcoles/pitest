package org.pitest.mutationtest.build.intercept.javafeatures;

import org.objectweb.asm.tree.RecordComponentNode;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RecordFilter implements MutationInterceptor {

    private boolean isRecord;
    private ClassTree currentClass;

    @Override
    public InterceptorType type() {
        return InterceptorType.FILTER;
    }

    @Override
    public void begin(ClassTree clazz) {
        isRecord = "java/lang/Record".equals(clazz.rawNode().superName);
        currentClass = clazz;
    }

    @Override
    public Collection<MutationDetails> intercept(Collection<MutationDetails> mutations, Mutater m) {
        if (isRecord) {
            return mutations.stream()
                    .filter(makeMethodFilter(currentClass))
                    .collect(Collectors.toList());
        }
        return mutations;
    }

    private Predicate<MutationDetails> makeMethodFilter(ClassTree currentClass) {
        Set<String> accessorNames = currentClass.rawNode().recordComponents.stream()
                .map(this::toName)
                .collect(Collectors.toSet());
        return m -> !accessorNames.contains(m.getMethod().name()) && !isStandardMethod(currentClass, m);
    }

    private boolean isStandardMethod(ClassTree currentClass, MutationDetails m) {
        String name = m.getMethod().name();
        return name.equals("<init>") || name.equals("equals") || name.equals("hashCode") || name.equals("toString");
    }

    private String toName(RecordComponentNode recordComponentNode) {
       return recordComponentNode.name;
    }

    @Override
    public void end() {
        currentClass = null;
    }
}
