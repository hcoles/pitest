package org.pitest.coverage;

import org.pitest.classinfo.ClassName;

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
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    InstructionLocation that = (InstructionLocation) o;

    if (instructionIndex != that.instructionIndex) {
      return false;
    }
    return blockLocation.getLocation().equals(that.blockLocation.getLocation());
  }

  @Override
  public int hashCode() {
    int result = blockLocation.getLocation().hashCode();
    result = 31 * result + instructionIndex;
    return result;
  }

  public boolean isFor(final ClassName clazz) {
    return this.blockLocation.getLocation().getClassName().equals(clazz);
  }

}
