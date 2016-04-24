package org.pitest.mutationtest.filter;

import java.util.Properties;

import org.pitest.classpath.CodeSource;

public class KotlinFilterFactory implements MutationFilterFactory {

  @Override
  public String description() {
    return "Kotlin junk mutations filter";
  }

  @Override
  public MutationFilter createFilter(Properties props, CodeSource source,
      int maxMutationsPerClass) {
    return new KotlinFilter();
  }

}
