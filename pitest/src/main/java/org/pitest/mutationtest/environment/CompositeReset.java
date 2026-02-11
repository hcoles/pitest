package org.pitest.mutationtest.environment;

import org.pitest.mutationtest.engine.Mutant;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CompositeReset implements ResetEnvironment {
    private final List<ResetEnvironment> children;

    public CompositeReset(List<ResetEnvironment> children) {
        this.children = children.stream()
                .sorted(Comparator.comparingInt(ResetEnvironment::priority))
                .collect(Collectors.toList());
    }

    @Override
    public void resetFor(Mutant mutatedClass, ClassLoader loader) {
      for (ResetEnvironment each : children) {
          each.resetFor(mutatedClass, loader);
      }
    }
}
