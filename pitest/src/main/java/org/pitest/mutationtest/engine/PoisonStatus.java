package org.pitest.mutationtest.engine;

public enum PoisonStatus {
  NORMAL(false), MAY_POISON_JVM(true), IS_STATIC_INITIALIZER_CODE(true);

  private final boolean mayPoison;

  PoisonStatus(boolean mayPoison) {
    this.mayPoison = mayPoison;
  }

  boolean mayPoison() {
    return this.mayPoison;
  }
}
