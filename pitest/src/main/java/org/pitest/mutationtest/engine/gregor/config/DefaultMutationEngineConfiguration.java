/*
 * Copyright 2011 Henry Coles
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and limitations under the License. 
 */
package org.pitest.mutationtest.engine.gregor.config;

import java.util.Collection;

import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationEngineConfiguration;
import org.pitest.mutationtest.engine.gregor.TryWithResourcesFilter;
import org.pitest.mutationtest.engine.gregor.inlinedcode.InlinedCodeFilter;

public class DefaultMutationEngineConfiguration implements
    MutationEngineConfiguration {

  private final Predicate<MethodInfo>                      methodFilter;
  private final Collection<String>                         doNotMutate;
  private final Collection<? extends MethodMutatorFactory> mutators;
  private final InlinedCodeFilter                          inlinedCodeDetector;
  private final TryWithResourcesFilter                     tryWithResourcesFilter;

  public DefaultMutationEngineConfiguration(final Predicate<MethodInfo> filter,
      final Collection<String> loggingClasses,
      final Collection<? extends MethodMutatorFactory> mutators,
      final InlinedCodeFilter inlinedCodeDetector,
      final TryWithResourcesFilter tryWithResourcesFilter) {
    this.methodFilter = filter;
    this.doNotMutate = loggingClasses;
    this.mutators = mutators;
    this.inlinedCodeDetector = inlinedCodeDetector;
    this.tryWithResourcesFilter = tryWithResourcesFilter;
  }

  public Collection<? extends MethodMutatorFactory> mutators() {
    return this.mutators;
  }

  public Collection<String> doNotMutateCallsTo() {
    return this.doNotMutate;
  }

  public Predicate<MethodInfo> methodFilter() {
    return this.methodFilter;
  }

  public InlinedCodeFilter inlinedCodeDetector() {
    return this.inlinedCodeDetector;
  }

  public TryWithResourcesFilter tryWithResourcesFilter() {
    return this.tryWithResourcesFilter;
  }

}
