package org.pitest.mutationtest.build;

import org.pitest.plugin.ProvidesFeature;
import org.pitest.plugin.ToolClasspathPlugin;

public interface ProjectMutationFilterFactory extends ToolClasspathPlugin, ProvidesFeature {

  ProjectMutationFilter createFilter(InterceptorParameters params);

}
