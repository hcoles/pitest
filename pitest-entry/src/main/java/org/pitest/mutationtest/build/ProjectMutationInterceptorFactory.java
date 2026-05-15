package org.pitest.mutationtest.build;

import org.pitest.plugin.ProvidesFeature;
import org.pitest.plugin.ToolClasspathPlugin;

public interface ProjectMutationInterceptorFactory extends ToolClasspathPlugin, ProvidesFeature {

  ProjectMutationInterceptor createInterceptor(InterceptorParameters params);

}
