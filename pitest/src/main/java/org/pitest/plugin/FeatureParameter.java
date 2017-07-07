package org.pitest.plugin;

public final class FeatureParameter {
  
  private final String  name;

  private FeatureParameter(String name) {
    this.name = name;
  }

  public static FeatureParameter named(String name) {
    return new FeatureParameter(name);
  }

  public String name() {
    return name;
  } 
  
}
