package org.pitest.mutationtest;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

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
    return Objects.hash(id, coverageId);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final ClassHistory other = (ClassHistory) obj;
    return Objects.equals(id, other.id)
            && Objects.equals(coverageId, other.coverageId);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ClassHistory.class.getSimpleName() + "[", "]")
            .add("id=" + id)
            .add("coverageId='" + coverageId + "'")
            .toString();
  }
}