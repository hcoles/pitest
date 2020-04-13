package org.pitest.mutationtest.engine;

import java.io.Serializable;
import java.util.Objects;

public class MethodName implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String name;

  MethodName(final String name) {
    this.name = name;
  }

  public static MethodName fromString(final String name) {
    return new MethodName(name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final MethodName other = (MethodName) obj;
    return Objects.equals(name, other.name);
  }

  @Override
  public String toString() {
    return this.name;
  }

  public final String name() {
    return this.name;
  }

}
