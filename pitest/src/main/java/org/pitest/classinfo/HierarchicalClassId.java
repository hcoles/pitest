package org.pitest.classinfo;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

public final class HierarchicalClassId implements Serializable {

  private static final long serialVersionUID = 1L;

  private final ClassIdentifier classId;
  private final String          hierarchicalHash;

  public HierarchicalClassId(final ClassIdentifier classId,
      final String hierarchicalHash) {
    this.classId = classId;
    this.hierarchicalHash = hierarchicalHash;
  }

  public HierarchicalClassId(final ClassIdentifier id, final BigInteger deepHash) {
    this(id, deepHash.toString(16));
  }

  public HierarchicalClassId(final long hash, final ClassName name,
      final String hierarchicalHash) {
    this(new ClassIdentifier(hash, name), hierarchicalHash);
  }

  public String getHierarchicalHash() {
    return this.hierarchicalHash;
  }

  public ClassName getName() {
    return this.classId.getName();
  }

  public ClassIdentifier getId() {
    return this.classId;
  }

  @Override
  public String toString() {
    return "HierarchicalClassId [classId=" + this.classId
        + ", hierarchicalHash=" + this.hierarchicalHash + "]";
  }

  @Override
  public int hashCode() {
    return Objects.hash(classId, hierarchicalHash);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final HierarchicalClassId other = (HierarchicalClassId) obj;
    return Objects.equals(classId, other.classId)
            && Objects.equals(hierarchicalHash, other.hierarchicalHash);
  }
}
