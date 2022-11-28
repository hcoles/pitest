package org.pitest.mutationtest.engine.gregor;

public class ClassInfo {

  private final int      access;
  private final String   name;
  private final String   superName;

  public ClassInfo(final int access, final String name, final String superName) {
    this.access = access;
    this.name = name;
    this.superName = superName;
  }

  public boolean isEnum() {
    return this.superName.equals("java/lang/Enum");
  }

  public int getAccess() {
    return this.access;
  }

  public String getName() {
    return this.name;
  }
  public String getSuperName() {
    return this.superName;
  }

}
