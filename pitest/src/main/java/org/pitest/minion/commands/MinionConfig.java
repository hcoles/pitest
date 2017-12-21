package org.pitest.minion.commands;

import java.beans.ConstructorProperties;

public class MinionConfig {

  private final String testPluginName;
  private final String engineName;
  private final String[] excludedGroups;
  private final String[] includedGroups;
  private final String[] excludedRunners;
  private final String[] mutators;
  private final String[] excludedMethods;
  
  @ConstructorProperties({"testPluginName", "engineName", "excludedGroups", "includedGroups", "excludedRunners", "mutators", "excludedMethods"}) 
  public MinionConfig(String testPluginName, String engineName,
      String[] excludedGroups, String[] includedGroups, String[] excludedRunners, String[] mutators, String[] excludedMethods) {
    this.testPluginName = testPluginName;
    this.engineName = engineName;
    this.excludedGroups = excludedGroups;
    this.includedGroups = includedGroups;
    this.excludedRunners = excludedRunners;
    this.mutators = mutators;
    this.excludedMethods = excludedMethods;
  }

  public String getTestPluginName() {
    return testPluginName;
  }

  public String getEngineName() {
    return engineName;
  }

  public String[] getExcludedGroups() {
    return excludedGroups;
  }

  public String[] getIncludedGroups() {
    return includedGroups;
  }

  public String[] getExcludedRunners() {
    return excludedRunners;
  }

  public String[] getMutators() {
    return mutators;
  }

  public String[] getExcludedMethods() {
    return excludedMethods;
  }
    
  
}
