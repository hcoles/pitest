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
package org.pitest.extension.common.testsuitefinder;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.pitest.internal.TestClass;

interface Exclude {
}

abstract class Abstract {

}

class Foo {

}

class Bar {

}

class FooDerived extends Foo implements Exclude {

}

class BarDerived extends Bar {

}

public class ClasspathSuiteFinderTest {

  public static final String   FOO_NAME = "org.pitest.extension.common.testsuitefinder.Foo";
  public static final String   BAR_NAME = "org.pitest.extension.common.testsuitefinder.Bar";

  private ClasspathSuiteFinder testee;

  @Before
  public void setup() {
    this.testee = new ClasspathSuiteFinder();
  }

  @Test
  public void testReturnsEmptyListIfClassPathSuiteAnnotationNotPresent() {
    @ClassNameRegexFilter("[\\S]+")
    class Suite {

    }

    final PITStaticMethodSuiteFinder testee = new PITStaticMethodSuiteFinder();
    final TestClass root = new TestClass(Suite.class);
    assertEquals(Collections.emptyList(), testee.apply(root));
  }

  @Test
  public void testRegexNameFilterFindsSingleClass() {
    @ClasspathSuite
    @ClassNameRegexFilter(FOO_NAME)
    class Suite {

    }

    assertEquals(Arrays.asList(new TestClass(Foo.class)), this.testee
        .apply(new TestClass(Suite.class)));
  }

  @ClasspathSuite
  @ClassNameRegexFilter("org.pitest.extension.common.testsuitefinder.ClasspathSuiteFinderTest\\$ExampleSuite")
  public static class ExampleSuite {

  }

  @Test
  public void testRegexNameFilterNeverReturnsParentClass() {
    assertEquals(Collections.emptyList(), this.testee.apply(new TestClass(
        ExampleSuite.class)));
  }

  @Test
  public void testNameFilterNeverReturnsAbstractClass() {
    @ClasspathSuite
    @ClassNameRegexFilter("org.pitest.extension.common.testsuitefinder.Abstract")
    class Suite {

    }

    assertEquals(Collections.emptyList(), this.testee.apply(new TestClass(
        Suite.class)));
  }

  @Test
  public void testRegexNameFilterFindsMultipleClasses() {
    @ClasspathSuite
    @ClassNameRegexFilter(value = { FOO_NAME, BAR_NAME })
    class Suite {

    }

    assertEquals(Arrays.asList(new TestClass(Bar.class), new TestClass(
        Foo.class)), this.testee.apply(new TestClass(Suite.class)));
  }

  @Test
  public void testRegexNameFilterReturnsRegexMatches() {
    @ClasspathSuite
    @ClassNameRegexFilter(value = { "[\\S]+testsuitefinder\\.(Foo|Bar)" })
    class Suite {

    }

    assertEquals(Arrays.asList(new TestClass(Bar.class), new TestClass(
        Foo.class)), this.testee.apply(new TestClass(Suite.class)));
  }

  @Test
  public void testGlobNameFilterFindsSingleClass() {
    @ClasspathSuite
    @ClassNameGlobFilter(FOO_NAME)
    class Suite {

    }

    assertEquals(Arrays.asList(new TestClass(Foo.class)), this.testee
        .apply(new TestClass(Suite.class)));
  }

  @Test
  public void testGlobNameFilterFindsMultipleMatches() {
    @ClasspathSuite
    @ClassNameGlobFilter("org.pitest.extension.common.testsuitefinder.Foo*")
    class Suite {

    }

    assertEquals(Arrays.asList(new TestClass(FooDerived.class), new TestClass(
        Foo.class)), this.testee.apply(new TestClass(Suite.class)));
  }

  @Test
  public void testGlobNameFilterAppliesMultipleFilters() {
    @ClasspathSuite
    @ClassNameGlobFilter(value = {
        "org.pitest.extension.common.testsuitefinder.Foo*",
        "org.pitest.extension.common.testsuitefinder.Bar*" })
    class Suite {

    }

    assertEquals(Arrays.asList(new TestClass(FooDerived.class), new TestClass(
        Bar.class), new TestClass(Foo.class), new TestClass(BarDerived.class)),
        this.testee.apply(new TestClass(Suite.class)));
  }

  @Test
  public void testCanMixGlobAndRegexFilters() {
    @ClasspathSuite
    @ClassNameGlobFilter(FOO_NAME)
    @ClassNameRegexFilter(BAR_NAME)
    class Suite {

    }

    assertEquals(Arrays.asList(new TestClass(Bar.class), new TestClass(
        Foo.class)), this.testee.apply(new TestClass(Suite.class)));
  }

  public static class InnerClass {

  }

  @Test
  public void testCanSearchForInnerClasses() {
    @ClasspathSuite(excludeInnerClasses = false)
    @ClassNameGlobFilter("org.pitest.extension.common.testsuitefinder.*Inner*")
    class Suite {

    }

    assertEquals(Arrays.asList(new TestClass(InnerClass.class)), this.testee
        .apply(new TestClass(Suite.class)));
  }

  @Test
  public void testBaseClassFilterIncludesOnlyExtendingClasses() {
    @ClasspathSuite
    @ClassNameRegexFilter(value = { "[\\S]+testsuitefinder\\.[\\S]+" })
    @BaseClassFilter( { Foo.class })
    class Suite {

    }

    assertEquals(Arrays.asList(new TestClass(FooDerived.class), new TestClass(
        Foo.class)), this.testee.apply(new TestClass(Suite.class)));
  }

  @Test
  public void testExcludeBaseClassFilterExcludesExtendingClasses() {
    @ClasspathSuite
    @ClassNameRegexFilter(value = { "[\\S]+testsuitefinder\\.[\\S]+" })
    @BaseClassFilter( { Foo.class })
    @ExcludeBaseClassFilter( { Exclude.class })
    class Suite {

    }

    assertEquals(Arrays.asList(new TestClass(Foo.class)), this.testee
        .apply(new TestClass(Suite.class)));
  }

}
