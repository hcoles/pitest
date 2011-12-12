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
package org.pitest.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.pitest.classinfo.ClassInfo;
import org.pitest.functional.Option;
import org.pitest.functional.predicate.Predicate;
import org.pitest.help.Help;
import org.pitest.help.PitHelpError;

public abstract class TestInfo {

  /*
   * public static FunctionalIterable<Class<?>> determineTestee(final Class<?>
   * test) { final org.pitest.annotations.ClassUnderTest annotation = test
   * .getAnnotation(org.pitest.annotations.ClassUnderTest.class); if (annotation
   * == null) { return FCollection.filter(determineTesteeFromName(test),
   * True.<Class<?>> all()); } else { return new
   * MutableList<Class<?>>(Arrays.asList(annotation.value())); } }
   * 
   * private static Option<Class<?>> determineTesteeFromName(final Class<?>
   * test) { final String name = test.getName(); final int testLength =
   * "Test".length(); if (name.endsWith("Test")) { final Option<Class<?>>
   * guessed = tryName(name.substring(0, name.length() - testLength)); if
   * (guessed.hasSome()) { return guessed; } }
   * 
   * final String className = getClassNameWithoutPackage(test); if
   * (className.startsWith("Test")) { final String nameGuess =
   * className.substring(testLength, className.length()); final Option<Class<?>>
   * guessed = tryName(test.getPackage().getName() + "." + nameGuess); if
   * (guessed.hasNone()) { if (test.getEnclosingClass() != null) { return
   * tryName(test.getEnclosingClass().getName() + "$" + nameGuess); } } else {
   * return guessed; } }
   * 
   * return Option.none(); }
   * 
   * private static String getClassNameWithoutPackage(final Class<?> clazz) { if
   * (clazz.getEnclosingClass() == null) { return clazz.getName().substring(
   * clazz.getPackage().getName().length() + 1, clazz.getName().length()); }
   * else { return clazz.getName().substring(
   * clazz.getEnclosingClass().getName().length() + 1,
   * clazz.getName().length()); } }
   * 
   * private static Option<Class<?>> tryName(final String name) { try { final
   * Class<?> guessed = Class.forName(name, false,
   * IsolationUtils.getContextClassLoader()); return Option.<Class<?>>
   * some(guessed); } catch (final ClassNotFoundException e) { return
   * Option.none(); } catch (final NoClassDefFoundError e) { // not clear why we
   * get this occasionally // when running with eclipse return Option.none(); }
   * }
   */
  public static boolean isWithinATestClass(final ClassInfo clazz) {

    final Option<ClassInfo> outerClass = clazz.getOuterClass();
    return isATest(clazz)
        || (outerClass.hasSome() && isATest(outerClass.value()));

  }

  public static boolean isATest(final ClassInfo clazz) {
    return isJUnit3Test(clazz) || isJUnit4Test(clazz)
        || isATest(clazz.getSuperClass());
  }

  private static boolean isATest(final Option<ClassInfo> clazz) {
    if (clazz.hasSome()) {
      return isATest(clazz.value());
    }
    return false;
  }

  public static Predicate<ClassInfo> isATest() {
    return new Predicate<ClassInfo>() {
      public Boolean apply(final ClassInfo clazz) {
        return isATest(clazz);
      }

    };
  }

  public static boolean isJUnit3Test(final ClassInfo clazz) {
    return clazz.descendsFrom(junit.framework.TestCase.class)
        || clazz.descendsFrom(junit.framework.TestSuite.class);
  }

  public static boolean isJUnit4Test(final ClassInfo clazz) {
    return clazz.hasAnnotation(RunWith.class)
        || clazz.hasAnnotation(Test.class);
  }

  public static void checkJUnitVersion() {
    try {
      final String version = junit.runner.Version.id();
      final String[] parts = version.split("\\.");
      final int major = Integer.parseInt(parts[0]);
      final int minor = Integer.parseInt(parts[1]);
      if ((major < 4) || ((major == 4) && (minor < 6))) {
        throw new PitHelpError(Help.WRONG_JUNIT_VERSION, version);
      }
    } catch (final NoClassDefFoundError er) {
      throw new PitHelpError(Help.NO_JUNIT);
    }

  }

}
