package org.pitest.mutationtest.engine.gregor;

import java.util.HashSet;
import java.util.Set;

class PremutationClassInfo {

  private final Set<Integer> linesToAvoid = new HashSet<Integer>();

  void registerLineToAvoid(final int lineNumber) {
    this.linesToAvoid.add(lineNumber);
  }

  boolean isLineToAvoid(final int line) {
    return this.linesToAvoid.contains(line);
  }
}
