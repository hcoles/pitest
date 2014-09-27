package org.pitest.coverage.analysis;

import java.util.Set;

public final class Block {
  private final int          firstInstruction;
  private final int          lastInstruction;
  private final Set<Integer> lines;

  public Block(int firstInstruction, int lastInstruction, Set<Integer> lines) {
    this.firstInstruction = firstInstruction;
    this.lastInstruction = lastInstruction;
    this.lines = lines;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + firstInstruction;
    result = prime * result + lastInstruction;
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
    Block other = (Block) obj;
    if (firstInstruction != other.firstInstruction)
      return false;
    if (lastInstruction != other.lastInstruction)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Block [firstInstruction=" + firstInstruction + ", lastInstruction="
        + lastInstruction + "]";
  }

  public boolean firstInstructionIs(int ins) {
    return this.firstInstruction == ins;
  }

  public Set<Integer> getLines() {
    return lines;
  }

}
