package org.pitest.plugin.export;

import java.nio.file.FileSystems;

import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;

public class MutantExportFactory implements MutationInterceptorFactory {

  @Override
  public String description() {
    return "Mutant export plugin";
  }

  @Override
  public MutationInterceptor createInterceptor(InterceptorParameters params) {
    return new MutantExportInterceptor(FileSystems.getDefault(), params.source(), params.data().getReportDir());
  }

  @Override
  public Feature provides() {
    return Feature.named("EXPORT")
        .withDescription("Exports mutants bytecode and other details to disk")
        .withOnByDefault(false);
  }

}
