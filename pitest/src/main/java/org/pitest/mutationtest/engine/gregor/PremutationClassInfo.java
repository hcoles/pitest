package org.pitest.mutationtest.engine.gregor;

import java.util.HashSet;
import java.util.Set;

public class PremutationClassInfo {

  private final Set<Integer> loggingLines = new HashSet<Integer>();

  public void registerLoggingLine(final int lineNumber) {
    this.loggingLines.add(lineNumber);

  }

  public boolean isLoggingLine(final int line) {
    return this.loggingLines.contains(line);
  }

}
