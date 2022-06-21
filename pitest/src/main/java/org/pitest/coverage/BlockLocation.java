package org.pitest.coverage;

import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.Location;

import java.util.Objects;

public final class BlockLocation {

  private final Location location;
  private final int      block;

  public BlockLocation(final Location location, final int block) {
    this.location = location;
    this.block = block;
  }

  public static BlockLocation blockLocation(final Location location,
      final int block ) {
    return new BlockLocation(location, block);
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
