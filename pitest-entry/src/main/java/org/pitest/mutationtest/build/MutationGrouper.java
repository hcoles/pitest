package org.pitest.mutationtest.build;

import java.util.Collection;
import java.util.List;

import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.MutationDetails;

public interface MutationGrouper {

  List<List<MutationDetails>> groupMutations(Collection<ClassName> codeClasses,
      Collection<MutationDetails> mutations);

}
