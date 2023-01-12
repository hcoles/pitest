package org.pitest.classpath;

import org.pitest.plugin.ToolClasspathPlugin;

public interface CodeSourceFactory extends ToolClasspathPlugin {
    CodeSource createCodeSource(ProjectClassPaths classPath);
}
