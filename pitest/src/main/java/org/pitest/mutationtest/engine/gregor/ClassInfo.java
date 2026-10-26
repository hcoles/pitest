package org.pitest.mutationtest.engine.gregor;

/**
 * Largely deprecated context info about current class. Retained to allow
 * easy access to information about super class.
 */
public class ClassInfo {

  private final int      access;
  private final String   name;
  private final String   superName;

  public ClassInfo(final int access, final String name, final String superName) {
    this.access = access;
    this.name = name;
    this.superName = superName;
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
