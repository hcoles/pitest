package org.pitest.mutationtest.build;

import org.pitest.classpath.CodeSource;
import org.pitest.plugin.ToolClasspathPlugin;

public interface CoverageTransformerFactory extends ToolClasspathPlugin {

    CoverageTransformer create(CodeSource source);
}
