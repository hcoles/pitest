package org.pitest.mutationtest.engine;

public class MethodName {

  private final String name;

  MethodName(final String name) {
    this.name = name;
  }

  public static MethodName fromString(final String name) {
    return new MethodName(name);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
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
    final MethodName other = (MethodName) obj;
    if (this.name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!this.name.equals(other.name)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return this.name;
  }

  public final String name() {
    return this.name;
  }

}
