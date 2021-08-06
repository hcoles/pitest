package org.pitest.mutationtest.build.intercept.javafeatures;

import org.objectweb.asm.Type;
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
        Set<String> accessorNames = currentClass.recordComponents().stream()
                .map(this::toName)
                .collect(Collectors.toSet());
        return m -> !accessorNames.contains(m.getMethod().name()) && !isStandardMethod(accessorNames.size(), m);
    }

    private boolean isStandardMethod(int numberOfComponents, MutationDetails m) {
        String name = m.getMethod().name();
        return isRecordInit(m, numberOfComponents) || name.equals("equals") || name.equals("hashCode") || name.equals("toString");
    }

    private boolean isRecordInit(MutationDetails m, int numberOfComponents) {
        // constructors with the same airty as the generated ones, but different
        // types won't get mutated. They're probably rare enough that this doesn't matter.
        int airty = Type.getArgumentTypes(m.getId().getLocation().getMethodDesc()).length;
        return m.getMethod().name().equals("<init>") && airty == numberOfComponents;
    }

    private String toName(RecordComponentNode recordComponentNode) {
       return recordComponentNode.name;
    }

    @Override
    public void end() {
        currentClass = null;
    }
}
