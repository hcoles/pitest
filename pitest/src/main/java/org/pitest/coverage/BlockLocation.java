package org.pitest.coverage;

import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.Location;

import java.util.Objects;

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
    return Objects.hash(location, block);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final BlockLocation other = (BlockLocation) obj;
    return block == other.block
            && Objects.equals(location, other.location);
  }

  @Override
  public String toString() {
    return "BlockLocation [location=" + this.location + ", block=" + this.block
        + "]";
  }

}
