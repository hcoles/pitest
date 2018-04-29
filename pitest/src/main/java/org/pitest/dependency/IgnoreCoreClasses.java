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
package org.pitest.dependency;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;

import org.pitest.functional.prelude.Prelude;
import org.pitest.util.Glob;

public class IgnoreCoreClasses implements Predicate<DependencyAccess> {

  private final Predicate<String>  impl;
  private final Collection<String> filtered = Arrays.asList("java.*", "sun.*",
      "javax.*", "org.junit.*",
      "junit.*", "org.mockito.*",
      "org.powermock.*",
      "org.jmock.*", "com.sun.*");

  IgnoreCoreClasses() {
    this.impl = Prelude.not(Prelude.or(Glob.toGlobPredicates(this.filtered)));
  }

  @Override
  public boolean test(final DependencyAccess a) {
    final String owner = a.getDest().getOwner().replace("/", ".");
    return this.impl.test(owner);
  }

}
