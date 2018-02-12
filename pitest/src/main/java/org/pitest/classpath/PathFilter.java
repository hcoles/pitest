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
package org.pitest.classpath;

import java.util.function.Predicate;

public class PathFilter {

  private final Predicate<ClassPathRoot> codeFilter;
  private final Predicate<ClassPathRoot> testFilter;

  public PathFilter(final Predicate<ClassPathRoot> codeFilter,
      final Predicate<ClassPathRoot> testFilter) {
    this.codeFilter = codeFilter;
    this.testFilter = testFilter;
  }

  public Predicate<ClassPathRoot> getCodeFilter() {
    return this.codeFilter;
  }

  public Predicate<ClassPathRoot> getTestFilter() {
    return this.testFilter;
  }

}
