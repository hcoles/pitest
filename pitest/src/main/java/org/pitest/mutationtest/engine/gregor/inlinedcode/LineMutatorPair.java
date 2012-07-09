package org.pitest.mutationtest.engine.gregor.inlinedcode;

public class LineMutatorPair {
  
  private final int lineNumber;
  private final String mutator;
  
  public LineMutatorPair(int lineNumber, String mutator) {
    this.lineNumber = lineNumber;
    this.mutator = mutator;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + lineNumber;
    result = prime * result + ((mutator == null) ? 0 : mutator.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    LineMutatorPair other = (LineMutatorPair) obj;
    if (lineNumber != other.lineNumber)
      return false;
    if (mutator == null) {
      if (other.mutator != null)
        return false;
    } else if (!mutator.equals(other.mutator))
      return false;
    return true;
  }
  
  

}
