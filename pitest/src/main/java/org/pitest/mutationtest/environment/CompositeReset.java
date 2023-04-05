package org.pitest.mutationtest.environment;

import org.pitest.mutationtest.engine.Mutant;

import java.util.Collections;
import java.util.List;

public class CompositeReset implements ResetEnvironment {
    private final List<ResetEnvironment> children;

    public CompositeReset(List<ResetEnvironment> children) {
        this.children = Collections.unmodifiableList(children);
    }

    @Override
    public void resetFor(Mutant mutatedClass) {
      for (ResetEnvironment each : children) {
          each.resetFor(mutatedClass);
      }
    }
}
