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
package org.pitest.junit;

import java.util.function.Predicate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.pitest.classinfo.ClassInfo;
import java.util.Optional;

abstract class TestInfo {

  public static boolean isWithinATestClass(final ClassInfo clazz) {

    final Optional<ClassInfo> outerClass = clazz.getOuterClass();
    return isATest(clazz)
        || (outerClass.isPresent() && isATest(outerClass.get()));

  }

  private static boolean isATest(final ClassInfo clazz) {
    return isJUnit3Test(clazz) || isJUnit4Test(clazz)
        || isATest(clazz.getSuperClass());
  }

  private static boolean isATest(final Optional<ClassInfo> clazz) {
    if (clazz.isPresent()) {
      return isATest(clazz.get());
    }
    return false;
  }

  public static Predicate<ClassInfo> isATest() {
    return clazz -> isATest(clazz);
  }

  private static boolean isJUnit3Test(final ClassInfo clazz) {
    return clazz.descendsFrom(junit.framework.TestCase.class)
        || clazz.descendsFrom(junit.framework.TestSuite.class);
  }

  private static boolean isJUnit4Test(final ClassInfo clazz) {
    return clazz.hasAnnotation(RunWith.class)
        || clazz.hasAnnotation(Test.class);
  }

}
