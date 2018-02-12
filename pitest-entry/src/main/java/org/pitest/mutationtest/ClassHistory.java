package org.pitest.mutationtest;

import java.io.Serializable;

import org.pitest.classinfo.ClassName;
import org.pitest.classinfo.HierarchicalClassId;

public class ClassHistory implements Serializable {

  private static final long serialVersionUID = 1L;

  private final HierarchicalClassId id;
  private final String              coverageId;

  public ClassHistory(final HierarchicalClassId id, final String coverageId) {
    this.id = id;
    this.coverageId = coverageId;
  }

  public HierarchicalClassId getId() {
    return this.id;
  }

  public String getCoverageId() {
    return this.coverageId;
  }

  public ClassName getName() {
    return this.id.getName();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result)
        + ((this.coverageId == null) ? 0 : this.coverageId.hashCode());
    result = (prime * result) + ((this.id == null) ? 0 : this.id.hashCode());
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
    final ClassHistory other = (ClassHistory) obj;
    if (this.coverageId == null) {
      if (other.coverageId != null) {
        return false;
      }
    } else if (!this.coverageId.equals(other.coverageId)) {
      return false;
    }
    if (this.id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!this.id.equals(other.id)) {
      return false;
    }
    return true;
  }

}