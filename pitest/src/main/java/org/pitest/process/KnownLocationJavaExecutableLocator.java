package org.pitest.process;

public class KnownLocationJavaExecutableLocator implements JavaExecutableLocator {
  
  private final String location;
  
  public KnownLocationJavaExecutableLocator(String location) {
    this.location = location;
  }

  public String javaExecutable() {
    return location;
  }

}
