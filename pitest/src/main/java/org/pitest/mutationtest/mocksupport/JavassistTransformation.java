package org.pitest.mutationtest.mocksupport;

import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.environment.TransformationPlugin;
import org.pitest.util.Glob;

import java.lang.instrument.ClassFileTransformer;

public class JavassistTransformation implements TransformationPlugin {

    @Override
    public ClassFileTransformer makeCoverageTransformer() {
        return new BendJavassistToMyWillTransformer(Prelude
                .or(new Glob("javassist/*")),
                JavassistInputStreamInterceptorAdapter.inputStreamAdapterSupplier(JavassistCoverageInterceptor.class));
    }

    @Override
    public ClassFileTransformer makeMutationTransformer() {
        return new BendJavassistToMyWillTransformer(Prelude
                .or(new Glob("javassist/*")),
                JavassistInputStreamInterceptorAdapter.inputStreamAdapterSupplier(JavassistInterceptor.class));
    }

    @Override
    public String description() {
        return "Support for mocking frameworks using javassist";
    }
}
