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

import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.config.TestPluginArguments;
import org.pitest.util.Verbosity;
import org.pitest.util.Glob;

public class CoverageOptions implements Serializable {

  private static final long serialVersionUID = 1L;

  private final Collection<String>      include;
  private final Collection<String>      exclude;
  private final Verbosity verbosity;
  private final TestPluginArguments pitConfig;


  public CoverageOptions(final Collection<String> include, final Collection<String> exclude,
      final TestPluginArguments pitConfig, final Verbosity verbose) {
    Objects.requireNonNull(pitConfig);
    this.include = include;
    this.exclude = exclude;
    this.verbosity = verbose;
    this.pitConfig = pitConfig;
  }

  public Predicate<String> getFilter() {
    return Prelude.and(Prelude.or(Glob.toGlobPredicates(this.include)),
        Prelude.not(Prelude.or(Glob.toGlobPredicates(this.exclude))),
        Prelude.not(commonClasses()));
  }

  public Verbosity verbosity() {
    return this.verbosity;
  }

  public TestPluginArguments getPitConfig() {
    return this.pitConfig;
  }

  private static Predicate<String> commonClasses() {
    return Prelude.or(
            glob("org.pitest.*"),
            glob("java.*"),
            glob("javax.*"),
            glob("com.sun.*"),
            glob("org.junit.*"),
            glob("sun.*"));
  }


  private static Glob glob(String match) {
    return new Glob(match);
  }

}
