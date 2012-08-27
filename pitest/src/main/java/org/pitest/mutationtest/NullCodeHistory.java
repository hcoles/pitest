package org.pitest.mutationtest;

import java.util.Collection;

import org.pitest.classinfo.ClassIdentifier;
import org.pitest.classinfo.ClassName;
import org.pitest.functional.Option;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.execute.MutationStatusTestPair;
import org.pitest.mutationtest.incremental.CodeHistory;

public class NullCodeHistory implements CodeHistory {

  public void recordClassPath(final Collection<ClassIdentifier> ids) {

  }

  public boolean hasClassChanged(final ClassName className) {
    return false;
  }

  public Option<MutationStatusTestPair> getPreviousResult(
      final MutationIdentifier id) {
    return Option.none();
  }

}
