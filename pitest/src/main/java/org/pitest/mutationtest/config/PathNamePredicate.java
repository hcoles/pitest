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
package org.pitest.mutationtest.config;

import org.pitest.classpath.ClassPathRoot;
import org.pitest.functional.predicate.Predicate;

public class PathNamePredicate implements Predicate<ClassPathRoot> {

  private final Predicate<String> stringFilter;

  public PathNamePredicate(final Predicate<String> stringFilter) {
    this.stringFilter = stringFilter;
  }

  @Override
  public Boolean apply(final ClassPathRoot classPathRoot) {
    return cacheLocationOptionExists(classPathRoot)
        && cacheLocationMatchesFilter(classPathRoot);
  }

  private Boolean cacheLocationMatchesFilter(final ClassPathRoot classPathRoot) {
    final String cacheLocationValue = classPathRoot.cacheLocation().value();
    return this.stringFilter.apply(cacheLocationValue);
  }

  private boolean cacheLocationOptionExists(final ClassPathRoot a) {
    return a.cacheLocation().hasSome();
  }

}
