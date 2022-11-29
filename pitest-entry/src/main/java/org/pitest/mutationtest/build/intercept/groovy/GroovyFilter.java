package org.pitest.mutationtest.build.intercept.groovy;

import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Prevents mutation of groovy code since we can't properly handle it
 */
public class GroovyFilter implements MutationInterceptor {

    boolean isGroovyClass = false;

    @Override
    public Collection<MutationDetails> intercept(
            Collection<MutationDetails> mutations, Mutater m) {
        if (isGroovyClass) {
            return Collections.emptyList();
        }
        return mutations;
    }

    @Override
    public InterceptorType type() {
        return InterceptorType.FILTER;
    }

    @Override
    public void begin(ClassTree clazz) {
        isGroovyClass = isGroovyClass(clazz);
    }

    @Override
    public void end() {
        isGroovyClass = false;
    }

    private boolean isGroovyClass(ClassTree clazz) {
        return clazz.rawNode().interfaces != null && clazz.rawNode().interfaces.stream()
                .anyMatch(a -> a.startsWith("groovy/lang/") || a.startsWith("org/codehaus/groovy/runtime"));
    }

}
