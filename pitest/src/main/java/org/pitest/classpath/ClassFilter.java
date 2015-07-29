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

import org.pitest.functional.predicate.Predicate;

public class ClassFilter {
  private final Predicate<String> test;
  private final Predicate<String> code;

  public ClassFilter(final Predicate<String> test, final Predicate<String> code) {
    this.test = test;
    this.code = code;

  }

  public Predicate<String> getTest() {
    return this.test;
  }

  public Predicate<String> getCode() {
    return this.code;
  }
}
