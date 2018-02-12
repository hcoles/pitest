package org.pitest.plugin;

public final class FeatureParameter {

  private final String  name;
  private final String  description;

  private FeatureParameter(String name, String desc) {
    this.name = name;
    this.description = desc;
  }

  public static FeatureParameter named(String name) {
    return new FeatureParameter(name, "");
  }

  public FeatureParameter withDescription(String desc) {
    return new FeatureParameter(this.name, desc);
  }

  public String name() {
    return this.name;
  }

  public String description() {
    return this.description;
  }

}
