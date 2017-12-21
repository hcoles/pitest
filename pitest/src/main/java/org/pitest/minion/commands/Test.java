package org.pitest.minion.commands;

import java.beans.ConstructorProperties;

public class Test {
  private final String clazz;
  private final String name;
  
  @ConstructorProperties({"clazz", "name"}) 
  public Test(String clazz, String name) {
    this.clazz = clazz;
    this.name = name;
  }

  public String getClazz() {
    return clazz;
  }

  public String getName() {
    return name;
  }

}
