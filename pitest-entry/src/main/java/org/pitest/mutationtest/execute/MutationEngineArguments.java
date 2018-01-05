package org.pitest.mutationtest.execute;

import java.util.Collection;

public class MutationEngineArguments {
  private final String mutationEngine;
  private final Collection<String> excludedMethods;
  private final Collection<String> mutators;
  
  public MutationEngineArguments(String mutationEngine,
      Collection<String> excludedMethods, Collection<String> mutators) {
    this.mutationEngine = mutationEngine;
    this.excludedMethods = excludedMethods;
    this.mutators = mutators;
  }

  public String getMutationEngine() {
    return mutationEngine;
  }

  public Collection<String> getExcludedMethods() {
    return excludedMethods;
  }

  public Collection<String> getMutators() {
    return mutators;
  }
  
  public String[] mutatorsArray() {
    return getMutators().toArray(new String[0]);
  }
  
  public String[] excludedMethodsArray() {
    return getExcludedMethods().toArray(new String[0]);
  }
  
}
