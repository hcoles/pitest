package org.pitest.mutationtest.build;

import java.util.Properties;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.plugin.ToolClasspathPlugin;

public interface MutationInterceptorFactory extends ToolClasspathPlugin {

  MutationInterceptor createInterceptor(Properties props, ClassByteArraySource source);
  
}
