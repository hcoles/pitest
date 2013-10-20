package org.pitest.process;

public class KnownLocationJavaExecutableLocator implements
    JavaExecutableLocator {

  private final String location;

  public KnownLocationJavaExecutableLocator(final String location) {
    this.location = location;
  }

  public String javaExecutable() {
    return this.location;
  }

}
