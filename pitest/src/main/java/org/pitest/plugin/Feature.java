package org.pitest.plugin;

public final class Feature {

  private final boolean onByDefault;
  private final String name;
  private final String description;
  
  private Feature(boolean onByDefault, String name, String description) {
    this.onByDefault = onByDefault;
    this.name = name;
    this.description = description;
  }

  public static Feature named(String name) {
    return new Feature(false, name, "");
  }
  
  public Feature withOnByDefault(boolean onByDefault) {
    return new Feature(onByDefault, name, description);
  }
  
  public Feature withDescription(String description) {
    return new Feature(onByDefault, name, description);
  }

  public String name() {
    return name;
  }

  public boolean isOnByDefault() {
    return onByDefault;
  }
    
}
