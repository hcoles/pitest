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

import org.pitest.classinfo.ClassInfo;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.testapi.TestClassIdentifier;

public class CompoundTestClassIdentifier implements TestClassIdentifier {

  private final Iterable<TestClassIdentifier> children;

  public CompoundTestClassIdentifier(
      final Iterable<TestClassIdentifier> children) {
    this.children = children;
  }

  @Override
  public boolean isATestClass(final ClassInfo a) {
    return FCollection.contains(this.children, isATest(a));
  }

  @Override
  public boolean isIncluded(ClassInfo a) {
    return FCollection.contains(this.children, isIncludedClass(a));
  }

  private F<TestClassIdentifier, Boolean> isIncludedClass(
      final ClassInfo classInfo) {
    return new F<TestClassIdentifier, Boolean>() {

      @Override
      public Boolean apply(final TestClassIdentifier a) {
        return a.isIncluded(classInfo);
      }

    };
  }

  private static F<TestClassIdentifier, Boolean> isATest(
      final ClassInfo classInfo) {
    return new F<TestClassIdentifier, Boolean>() {

      @Override
      public Boolean apply(final TestClassIdentifier a) {
        return a.isATestClass(classInfo);
      }

    };
  }

}
