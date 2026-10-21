package org.pitest.mutationtest.engine.gregor;

/**
 * Largely deprecated context info about current class. Retained to allow
 * easy access to information about super class.
 */
public class ClassInfo {

  private final int      access;
  private final String   name;
  private final String   superName;

  /**
   * Nullness declared for this class, {@code null} when nothing was declared.
   * Set from {@code package-info} before the class is visited, then overridden
   * by an annotation on the class itself.
   */
  private Boolean        nullMarked;

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

  /**
   * @return TRUE when the class is covered by {@code @NullMarked}, FALSE when
   *         it opted out with {@code @NullUnmarked}, and null when nullness was
   *         never declared for the class or its package.
   */
  public Boolean getNullMarked() {
    return this.nullMarked;
  }

  public void setNullMarked(final Boolean nullMarked) {
    this.nullMarked = nullMarked;
  }

}
