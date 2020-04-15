package org.pitest.coverage.analysis;

import java.util.Objects;
import java.util.Set;

public final class Block {
  private final int          firstInstruction;
  private final int          lastInstruction;
  private final Set<Integer> lines;

  public Block(final int firstInstruction, final int lastInstruction,
      final Set<Integer> lines) {
    this.firstInstruction = firstInstruction;
    this.lastInstruction = lastInstruction;
    this.lines = lines;
  }

  @Override
  public int hashCode() {
    return Objects.hash(firstInstruction, lastInstruction);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final Block other = (Block) obj;
    return (firstInstruction == other.firstInstruction)
            && (lastInstruction == other.lastInstruction);
  }

  @Override
  public String toString() {
    return "Block [firstInstruction=" + this.firstInstruction
        + ", lastInstruction=" + this.lastInstruction + "]";
  }

  public boolean firstInstructionIs(final int ins) {
    return this.firstInstruction == ins;
  }

  public Set<Integer> getLines() {
    return this.lines;
  }

  public int getFirstInstruction() {
    return this.firstInstruction;
  }

  public int getLastInstruction() {
    return this.lastInstruction;
  }
}
