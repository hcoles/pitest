package org.pitest.mutationtest.build.intercept.javafeatures;

import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.Collection;
import java.util.Collections;

public class RecordFilter implements MutationInterceptor {

    private boolean isRecord;

    @Override
    public InterceptorType type() {
        return InterceptorType.FILTER;
    }

    @Override
    public void begin(ClassTree clazz) {
        isRecord = "java/lang/Record".equals(clazz.rawNode().superName);
    }

    @Override
    public Collection<MutationDetails> intercept(Collection<MutationDetails> mutations, Mutater m) {
        if (isRecord) {
            return Collections.emptyList();
        }
        return mutations;
    }

    @Override
    public void end() {

    }
}
