package org.pitest.mutationtest;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

public class EngineArguments implements Serializable {

  private static final long serialVersionUID = 1L;

  private final Collection<String> mutators;
  private final Collection<String> excludedMethods;

  public EngineArguments(Collection<String> mutators, Collection<String> excludedMethods) {
    this.mutators = mutators;
    this.excludedMethods = excludedMethods;
  }

  public static EngineArguments arguments() {
    return new EngineArguments(Collections.<String>emptyList(), Collections.<String>emptyList());
  }

  public EngineArguments withMutators(Collection<String> mutators) {
    return new EngineArguments(mutators, this.excludedMethods);
  }

  public EngineArguments withExcludedMethods(Collection<String> excludedMethods) {
    return new EngineArguments(this.mutators, excludedMethods);
  }

  public Collection<String> mutators() {
    return this.mutators;
  }

  public Collection<String> excludedMethods() {
    return this.excludedMethods;
  }

}

