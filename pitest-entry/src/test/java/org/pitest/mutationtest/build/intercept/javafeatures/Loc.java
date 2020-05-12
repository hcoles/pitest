package org.pitest.mutationtest.build.intercept.javafeatures;

import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.Objects;

class Loc {
  int index;
  AbstractInsnNode node;

  @Override
  public String toString() {
    return "[" + this.index + "] " + this.node;
  }

  @Override
  public int hashCode() {
    return Objects.hash(index, node);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final Loc other = (Loc) obj;
    return index == other.index
            && Objects.equals(node, other.node);
  }
}