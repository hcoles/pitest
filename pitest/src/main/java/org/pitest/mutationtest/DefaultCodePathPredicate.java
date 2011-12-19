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
package org.pitest.mutationtest;

import java.util.Collection;

import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.predicate.Predicate;
import org.pitest.internal.classloader.ClassPathRoot;

public class DefaultCodePathPredicate implements Predicate<ClassPathRoot> {

  private final int threshold;

  DefaultCodePathPredicate(int threshold) {
    this.threshold = threshold;
  }

  public Boolean apply(ClassPathRoot a) {
    return a.cacheLocation().hasSome()
        && !isATestPath(a.cacheLocation().value())
        && !isADependencyPath(a.cacheLocation().value())
        && hasLessThanThresholdOfProbableTests(a);
  }

  private boolean isADependencyPath(String path) {
    String lowerCasePath = path.toLowerCase();
    return lowerCasePath.endsWith(".jar") || lowerCasePath.endsWith(".zip");
  }

  private boolean isATestPath(String path) {
    return path.endsWith("test-classes");
  }

  private boolean hasLessThanThresholdOfProbableTests(ClassPathRoot a) {
    Collection<String> names = a.classNames();
    int numberOfTests = FCollection.filter(names, mightBeATest()).size();
    return ((100 / names.size()) * numberOfTests) < this.threshold;
  }

  private static F<String, Boolean> mightBeATest() {
    return new F<String, Boolean>() {
      public Boolean apply(String a) {
        return a.contains("Test");
      }

    };
  }

}
