package org.pitest.classinfo;

public final class ClassIdentifier {

  private final long      hash;
  private final ClassName name;

  public ClassIdentifier(final long hash, final ClassName name) {
    this.hash = hash;
    this.name = name;
  }

  public long getHash() {
    return this.hash;
  }

  public ClassName getName() {
    return this.name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + (int) (this.hash ^ (this.hash >>> 32));
    result = (prime * result)
        + ((this.name == null) ? 0 : this.name.hashCode());
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
    final ClassIdentifier other = (ClassIdentifier) obj;
    if (this.hash != other.hash) {
      return false;
    }
    if (this.name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!this.name.equals(other.name)) {
      return false;
    }
    return true;
  }

}
