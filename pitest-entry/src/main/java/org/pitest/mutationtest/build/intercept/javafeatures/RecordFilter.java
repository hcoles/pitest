package org.pitest.mutationtest.build.intercept.javafeatures;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.RecordComponentNode;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.Collection;
import java.util.Optional;
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
        return m -> !accessorNames.contains(m.getMethod()) && !isStandardMethod(accessorNames.size(), m);
    }

    private boolean isStandardMethod(int numberOfComponents, MutationDetails m) {
        return isRecordInit(m, numberOfComponents)
                || isRecordEquals(m)
                || isRecordHashCode(m)
                || isRecordToString(m);
    }

    private boolean isRecordInit(MutationDetails m, int numberOfComponents) {
        // constructors with the same airty as the generated ones, but different
        // types won't get mutated. They're probably rare enough that this doesn't matter.
        int airty = Type.getArgumentTypes(m.getId().getLocation().getMethodDesc()).length;
        return m.getMethod().equals("<init>") && airty == numberOfComponents;
    }

    private boolean isRecordEquals(MutationDetails m) {
        return m.getId().getLocation().getMethodDesc().equals("(Ljava/lang/Object;)Z")
                && m.getMethod().equals("equals")
                && hasDynamicObjectMethodsCall(m);
    }

    private boolean isRecordHashCode(MutationDetails m) {
        return m.getId().getLocation().getMethodDesc().equals("()I")
                && m.getMethod().equals("hashCode")
                && hasDynamicObjectMethodsCall(m);
    }

    private boolean isRecordToString(MutationDetails m) {
        return m.getId().getLocation().getMethodDesc().equals("()Ljava/lang/String;")
                && m.getMethod().equals("toString")
                && hasDynamicObjectMethodsCall(m);
    }

    private boolean hasDynamicObjectMethodsCall(MutationDetails mutation) {
        // java/lang/runtime/ObjectMethods was added to support records and can be used as a marker
        // for an auth generated equals method. It's not likely that a custom method would
        // contain a dynamic call to it
        Optional<MethodTree> method = currentClass.method(mutation.getId().getLocation());
        return method.filter(m -> m.instructions().stream().anyMatch(this::isInvokeDynamicCallToObjectMethods))
                .isPresent();
    }

    private boolean isInvokeDynamicCallToObjectMethods(AbstractInsnNode node) {
        if (node instanceof InvokeDynamicInsnNode) {
            InvokeDynamicInsnNode call = (InvokeDynamicInsnNode) node;
            return call.bsm.getOwner().equals("java/lang/runtime/ObjectMethods")
                && call.bsm.getName().equals("bootstrap");

        }
        return false;
    }

    private String toName(RecordComponentNode recordComponentNode) {
       return recordComponentNode.name;
    }

    @Override
    public void end() {
        currentClass = null;
    }
}
