package org.pitest.coverage;

import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.Location;

public final class BlockLocation {

  private final Location location;
  private final int      block;
  private final int      firstInsnInBlock;
  private final int      lastInsnInBlock;

  public BlockLocation(final Location location, final int block,
      final int firstInsnInBlock, final int lastInsnInBlock) {
    this.location = location;
    this.block = block;
    this.firstInsnInBlock = firstInsnInBlock;
    this.lastInsnInBlock = lastInsnInBlock;
  }

  public static BlockLocation blockLocation(final Location location,
      final int block ) {
    return new BlockLocation(location, block, -1, -1);
  }

  public boolean isFor(final ClassName clazz) {
    return this.location.getClassName().equals(clazz);
  }

  public int getBlock() {
    return this.block;
  }

  public Location getLocation() {
    return this.location;
  }

  public int getFirstInsnInBlock() {
    return firstInsnInBlock;
  }

  public int getLastInsnInBlock() {
    return lastInsnInBlock;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.block;
    result = (prime * result)
        + ((this.location == null) ? 0 : this.location.hashCode());
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
    final BlockLocation other = (BlockLocation) obj;
    if (this.block != other.block) {
      return false;
    }
    if (this.location == null) {
      if (other.location != null) {
        return false;
      }
    } else if (!this.location.equals(other.location)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "BlockLocation [location=" + this.location + ", block=" + this.block
        + "]";
  }

}
