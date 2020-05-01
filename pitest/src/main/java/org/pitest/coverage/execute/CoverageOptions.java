package org.pitest.coverage.execute;

import java.io.Serializable;
import java.util.Collection;

/*
 * Copyright 2010 Henry Coles
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

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.config.TestPluginArguments;
import org.pitest.util.Glob;

public class CoverageOptions implements Serializable {

  private static final long serialVersionUID = 1L;

  private final Collection<String>      include;
  private final Collection<String>      exclude;
  private final boolean           verbose;
  private final TestPluginArguments pitConfig;
  private final int               maxDependencyDistance;

  public CoverageOptions(final Collection<String> include, final Collection<String> exclude,
      final TestPluginArguments pitConfig, final boolean verbose,
      final int maxDependencyDistance) {
    Objects.requireNonNull(pitConfig);
    this.include = include;
    this.exclude = exclude;
    this.verbose = verbose;
    this.pitConfig = pitConfig;
    this.maxDependencyDistance = maxDependencyDistance;
  }

  public Predicate<String> getFilter() {
    return Stream.of(
        Prelude.or(Glob.toGlobPredicates(this.include)),
        Prelude.not(Prelude.or(Glob.toGlobPredicates(this.exclude))),
        Prelude.not(commonClasses())
    ).reduce(x -> true, Predicate::and);
  }

  public boolean isVerbose() {
    return this.verbose;
  }

  public TestPluginArguments getPitConfig() {
    return this.pitConfig;
  }

  public int getDependencyAnalysisMaxDistance() {
    return this.maxDependencyDistance;
  }

  private static Predicate<String> commonClasses() {
    return Stream.<Predicate<String>>of(
        glob("java/*"),
        glob("sun/*"),
        glob("org/pitest/coverage/*"),
        glob("org/pitest/reloc/*"),
        glob("org/pitest/boot/*")
    ).reduce(x -> false, Predicate::or);
  }

  private static Glob glob(String match) {
    return new Glob(match);
  }

}
