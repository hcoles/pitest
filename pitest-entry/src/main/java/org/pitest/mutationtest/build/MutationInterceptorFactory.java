package org.pitest.mutationtest.build;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.plugin.ToolClasspathPlugin;

public interface MutationInterceptorFactory extends ToolClasspathPlugin {

  MutationInterceptor createInterceptor(ReportOptions data, 
                                        ClassByteArraySource source);
  
}
