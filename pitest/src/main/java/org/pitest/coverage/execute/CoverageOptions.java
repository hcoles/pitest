package org.pitest.coverage.execute;

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

import org.pitest.functional.predicate.Predicate;
import org.pitest.testapi.Configuration;

public class CoverageOptions {

  private final Predicate<String> filter;
  private final boolean           verbose;
  private final Configuration     pitConfig;
  private final int               maxDependencyDistance;

  public CoverageOptions(final Predicate<String> filter,
      final Configuration pitConfig, final boolean verbose,
      final int maxDependencyDistance) {
    this.filter = filter;
    this.verbose = verbose;
    this.pitConfig = pitConfig;
    this.maxDependencyDistance = maxDependencyDistance;
  }

  public Predicate<String> getFilter() {
    return this.filter;
  }

  public boolean isVerbose() {
    return this.verbose;
  }

  public Configuration getPitConfig() {
    return this.pitConfig;
  }

  public int getDependencyAnalysisMaxDistance() {
    return this.maxDependencyDistance;
  }

}
