package org.pitest.classinfo;

public class HierarchicalClassId {
  
  private final ClassIdentifier classId;
  private final String hierarchicalHash;
  
  public HierarchicalClassId(ClassIdentifier classId, String hierarchicalHash) {
    this.classId = classId;
    this.hierarchicalHash = hierarchicalHash;
  }
  
  public HierarchicalClassId(final long hash, final ClassName name, String hierarchicalHash) {
    this(new ClassIdentifier(hash,name), hierarchicalHash);
  }

  public String getHirearchialHash() {
    return hierarchicalHash;
  }

  public ClassName getName() {
    return classId.getName();
  }
  
  public ClassIdentifier getId() {
    return classId;
  }
  
  

  @Override
  public String toString() {
    return "HierarchicalClassId [classId=" + classId + ", hierarchicalHash="
        + hierarchicalHash + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((classId == null) ? 0 : classId.hashCode());
    result = prime * result
        + ((hierarchicalHash == null) ? 0 : hierarchicalHash.hashCode());
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
    HierarchicalClassId other = (HierarchicalClassId) obj;
    if (classId == null) {
      if (other.classId != null)
        return false;
    } else if (!classId.equals(other.classId))
      return false;
    if (hierarchicalHash == null) {
      if (other.hierarchicalHash != null)
        return false;
    } else if (!hierarchicalHash.equals(other.hierarchicalHash))
      return false;
    return true;
  }


  
  

}
