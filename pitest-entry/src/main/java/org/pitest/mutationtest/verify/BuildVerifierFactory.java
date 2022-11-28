package org.pitest.mutationtest.verify;

import org.pitest.classpath.CodeSource;
import org.pitest.plugin.ToolClasspathPlugin;

public interface BuildVerifierFactory extends ToolClasspathPlugin {

    BuildVerifier create(CodeSource code);
}
