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
package org.pitest.simpletest;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class ExcludedPrefixIsolationStrategy implements IsolationStrategy {

  private final Set<String> excludedPackagePrefixes = new LinkedHashSet<>();

  public ExcludedPrefixIsolationStrategy(final String... prefixes) {
    this(Arrays.asList(prefixes));
  }

  public ExcludedPrefixIsolationStrategy(
      final Collection<String> excludedPackagePrefixes) {
    this.excludedPackagePrefixes.addAll(excludedPackagePrefixes);
  }

  @Override
  public boolean shouldIsolate(final String name) {
    for (final String s : this.excludedPackagePrefixes) {
      if (name.startsWith(s)) {
        return false;
      }
    }
    return true;
  }

}
