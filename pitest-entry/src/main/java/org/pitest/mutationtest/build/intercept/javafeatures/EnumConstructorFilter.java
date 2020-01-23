package org.pitest.mutationtest.build.intercept.javafeatures;

import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Filters out mutations in Enum constructors, these are called only once
 * per instance so are effectively static initializers.
 */
public class EnumConstructorFilter implements MutationInterceptor {

    private boolean isEnum;

    @Override
    public InterceptorType type() {
        return InterceptorType.FILTER;
    }

    @Override
    public void begin(ClassTree clazz) {
        this.isEnum = clazz.rawNode().superName.equals("java/lang/Enum");
    }

    @Override
    public Collection<MutationDetails> intercept(
            Collection<MutationDetails> mutations, Mutater m) {
        return mutations.stream()
                .filter(isInEnumConstructor().negate())
                .collect(Collectors.toList());
    }

    private Predicate<MutationDetails> isInEnumConstructor() {
        return m -> isEnum && m.getMethod().name().equals("<init>");
    }


    @Override
    public void end() {
    }

}

