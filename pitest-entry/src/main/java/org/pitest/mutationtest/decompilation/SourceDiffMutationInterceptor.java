package org.pitest.mutationtest.decompilation;

import java.util.Collection;
import java.util.List;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

public class SourceDiffMutationInterceptor implements MutationInterceptor {

  private final Decompiler decompiler;

  public SourceDiffMutationInterceptor(ClassByteArraySource source) {
    this.decompiler = new Decompiler(source);
  }

  private List<String> origClassSource;

  @Override
  public void begin(ClassName clazz) {
    origClassSource = getOriginalClassSource(clazz);
  }

  @Override
  public Collection<MutationDetails> intercept(
      Collection<MutationDetails> mutations, Mutater m) {
    if (decompiledSuccesfully()) {
      return FCollection.map(mutations, addSourceDiff(origClassSource, m));
    } else {
      return mutations;
    }
  }

  @Override
  public void end() {
    origClassSource = null;
  }

  private F<MutationDetails, MutationDetails> addSourceDiff(
      final List<String> origClassSource, final Mutater m) {
    return new F<MutationDetails, MutationDetails>() {

      @Override
      public MutationDetails apply(MutationDetails a) {
        byte[] bytes = m.getMutation(a.getId()).getBytes();
        List<String> src = decompiler.decompile(a.getClassName(), bytes);
        SourceDiff diff = new SourceDiff();
        return a.withDescription(diff.describe(origClassSource, src));
      }

    };
  }

  private List<String> getOriginalClassSource(ClassName className) {
    return decompiler.decompile(className);
  }
  
  private boolean decompiledSuccesfully() {
    return origClassSource != null;
  }

}


