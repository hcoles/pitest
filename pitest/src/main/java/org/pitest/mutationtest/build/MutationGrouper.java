package org.pitest.mutationtest.build;

import java.util.Collection;
import java.util.List;

import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassPathRoot;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.plugin.ToolClasspathPlugin;

public interface MutationGrouper extends ToolClasspathPlugin {

  List<List<MutationDetails>> groupMutations(ClassPathRoot bas,
      Collection<ClassName> codeClasses,
      final Collection<MutationDetails> mutations);

}
