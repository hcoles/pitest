package org.pitest.mutationtest.build.intercept.javafeatures;

import java.util.Objects;

final class LineMutatorPair {

  private final int    lineNumber;
  private final String mutator;

  LineMutatorPair(final int lineNumber, final String mutator) {
    this.lineNumber = lineNumber;
    this.mutator = mutator;
  }

  @Override
  public int hashCode() {
    return Objects.hash(lineNumber, mutator);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final LineMutatorPair other = (LineMutatorPair) obj;
    return lineNumber == other.lineNumber
            && Objects.equals(mutator, other.mutator);
  }
}
