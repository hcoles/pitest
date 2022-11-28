package org.pitest.classinfo;

import java.io.Serializable;
import java.util.Objects;

public final class ClassIdentifier implements Serializable {

  private static final long serialVersionUID = 1L;

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
    return Objects.hash(hash, name);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final ClassIdentifier other = (ClassIdentifier) obj;
    return hash == other.hash
            && Objects.equals(name, other.name);
  }
}
