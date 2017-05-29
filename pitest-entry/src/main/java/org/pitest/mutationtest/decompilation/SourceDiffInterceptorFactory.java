package org.pitest.mutationtest.decompilation;

import java.util.Properties;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;

public class SourceDiffInterceptorFactory implements MutationInterceptorFactory {

  @Override
  public String description() {
    return "Source diff mutation description plugin";
  }

  @Override
  public MutationInterceptor createInterceptor(Properties props,
      ClassByteArraySource source) {
    return new SourceDiffMutationInterceptor(source);
  }

}
