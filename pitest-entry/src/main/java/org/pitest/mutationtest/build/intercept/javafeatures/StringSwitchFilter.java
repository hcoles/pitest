package org.pitest.mutationtest.build.intercept.javafeatures;

import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.Collection;

public class StringSwitchFilter implements MutationInterceptor {

    private ClassTree currentClass;


    @Override
    public InterceptorType type() {
        return InterceptorType.FILTER;
    }

    @Override
    public void begin(ClassTree clazz) {
        currentClass = clazz;
    }

    @Override
    public Collection<MutationDetails> intercept(Collection<MutationDetails> mutations, Mutater m) {
        return mutations;
    }


    @Override
    public void end() {
        currentClass = null;
    }

}