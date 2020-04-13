package org.pitest.coverage;

import org.pitest.classinfo.ClassName;

import java.util.Objects;

public final class InstructionLocation {
  private final BlockLocation blockLocation;
  private final int           instructionIndex;

  public InstructionLocation(BlockLocation blockLocation,
      int instructionIndex) {
    this.blockLocation = blockLocation;
    this.instructionIndex = instructionIndex;
  }

  public BlockLocation getBlockLocation() {
    return blockLocation;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final InstructionLocation other = (InstructionLocation) obj;
    return instructionIndex == other.instructionIndex
            && Objects.equals(blockLocation.getLocation(), other.blockLocation.getLocation());
  }

  @Override
  public int hashCode() {
    return Objects.hash(blockLocation.getLocation(), instructionIndex);
  }

  public boolean isFor(final ClassName clazz) {
    return this.blockLocation.getLocation().getClassName().equals(clazz);
  }

}
