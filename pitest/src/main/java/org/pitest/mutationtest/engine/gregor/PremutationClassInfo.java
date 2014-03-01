package org.pitest.mutationtest.engine.gregor;

import java.util.HashSet;
import java.util.Set;

public class PremutationClassInfo {

  private final Set<Integer> linesToAvoid = new HashSet<Integer>();

  public void registerLineToAvoid(final int lineNumber) {
    this.linesToAvoid.add(lineNumber);

  }

  public boolean isLineToAvoid(final int line) {
    return this.linesToAvoid.contains(line);
  }

}
