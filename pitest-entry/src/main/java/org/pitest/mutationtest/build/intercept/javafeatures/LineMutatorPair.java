package org.pitest.mutationtest.build.intercept.javafeatures;

public class LineMutatorPair {

  private final int    lineNumber;
  private final String mutator;

  public LineMutatorPair(final int lineNumber, final String mutator) {
    this.lineNumber = lineNumber;
    this.mutator = mutator;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.lineNumber;
    result = (prime * result)
        + ((this.mutator == null) ? 0 : this.mutator.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final LineMutatorPair other = (LineMutatorPair) obj;
    if (this.lineNumber != other.lineNumber) {
      return false;
    }
    if (this.mutator == null) {
      if (other.mutator != null) {
        return false;
      }
    } else if (!this.mutator.equals(other.mutator)) {
      return false;
    }
    return true;
  }

}
