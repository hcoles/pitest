package org.pitest.mutationtest.build.intercept.javafeatures;

import org.objectweb.asm.tree.AbstractInsnNode;

class Loc {
  int index;
  AbstractInsnNode node;
  @Override
  public String toString() {
    return "[" + this.index + "] " + this.node;
  }
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.index;
    result = (prime * result) + ((this.node == null) ? 0 : this.node.hashCode());
    return result;
  }
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Loc other = (Loc) obj;
    if (this.index != other.index) {
      return false;
    }
    if (this.node == null) {
      if (other.node != null) {
        return false;
      }
    } else if (!this.node.equals(other.node)) {
      return false;
    }
    return true;
  }

}