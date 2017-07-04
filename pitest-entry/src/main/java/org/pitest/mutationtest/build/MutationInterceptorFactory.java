package org.pitest.mutationtest.build;

import org.pitest.plugin.ProvidesFeature;
import org.pitest.plugin.ToolClasspathPlugin;

public interface MutationInterceptorFactory extends ToolClasspathPlugin, ProvidesFeature {

  MutationInterceptor createInterceptor(InterceptorParameters params);
    
}
