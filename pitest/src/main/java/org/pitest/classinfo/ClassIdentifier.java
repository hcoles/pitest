package org.pitest.classinfo;

public class ClassIdentifier {

  private final long hash;
  private final ClassName name;
  
  public ClassIdentifier(long hash, ClassName name) {
    this.hash = hash;
    this.name = name;
  }

  public long getHash() {
    return hash;
  }

  public ClassName getName() {
    return name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (hash ^ (hash >>> 32));
    result = prime * result + ((name == null) ? 0 : name.hashCode());
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
    ClassIdentifier other = (ClassIdentifier) obj;
    if (hash != other.hash)
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }
  
  
  
}
