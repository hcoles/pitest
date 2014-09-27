package org.pitest.coverage;

import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.Location;

public final class BlockLocation {
  
  private final Location location;
  private final int block;
  
  public BlockLocation(Location location, int block) {
    this.location = location;
    this.block = block;
  }

  public static BlockLocation blockLocation(Location location, int block) {
    return new BlockLocation(location,block);
  }
  
  public boolean isFor(ClassName clazz) {
    return this.location.getClassName().equals(clazz);
  }
  
  public int getBlock() {
    return block;
  }
  
  public Location getLocation() {
	  return this.location;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + block;
    result = prime * result + ((location == null) ? 0 : location.hashCode());
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
    BlockLocation other = (BlockLocation) obj;
    if (block != other.block)
      return false;
    if (location == null) {
      if (other.location != null)
        return false;
    } else if (!location.equals(other.location))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "BlockLocation [location=" + location + ", block=" + block + "]";
  }


  

}
