package org.pitest.classpath;

import org.pitest.plugin.ProvidesFeature;
import org.pitest.plugin.ToolClasspathPlugin;

public interface CodeSourceDecoratorFactory extends ToolClasspathPlugin, ProvidesFeature {
  CodeSourceDecorator createDecorator(CodeSourceParams params);
}
