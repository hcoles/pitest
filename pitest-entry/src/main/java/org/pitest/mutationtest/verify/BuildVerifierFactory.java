package org.pitest.mutationtest.verify;

import org.pitest.classpath.CodeSource;
import org.pitest.plugin.ToolClasspathPlugin;

public interface BuildVerifierFactory extends ToolClasspathPlugin {

    default BuildVerifier create(BuildVerifierArguments args) {
        return create(args.code());
    }

    @Deprecated
    default BuildVerifier create(CodeSource code) {
        throw new IllegalStateException();
    }
}
