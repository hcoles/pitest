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

  private ClasspathSuiteFinder testee;

  @Before
  public void setup() {
    this.testee = new ClasspathSuiteFinder();
  }

  @Test
  public void testReturnsEmptyListIfClassPathSuiteAnnotationNotPresent() {
    @ClassNameRegexFilter(value = { "[\\S]+" })
    class Suite {

    }

    final PITStaticMethodSuiteFinder testee = new PITStaticMethodSuiteFinder();
    final TestClass root = new TestClass(Suite.class);
    assertEquals(Collections.emptyList(), testee.apply(root));
  }

  @Test
  public void testNameFilterFindsSingleClass() {
    @ClasspathSuite
    @ClassNameRegexFilter(value = { "org.pitest.extension.common.testsuitefinder.Foo" })
    class Suite {

    }

    assertEquals(Arrays.asList(new TestClass(Foo.class)), this.testee
        .apply(new TestClass(Suite.class)));
  }

  @ClasspathSuite
  @ClassNameRegexFilter(value = { "org.pitest.extension.common.testsuitefinder.ClasspathSuiteFinderTest\\$ExampleSuite" })
  public static class ExampleSuite {

  }

  @Test
  public void testNameFilterNeverReturnsParentClass() {
    System.out.println(ExampleSuite.class);
    assertEquals(Collections.emptyList(), this.testee.apply(new TestClass(
        ExampleSuite.class)));
  }

  @Test
  public void testNameFilterNeverReturnsAbstractClass() {
    @ClasspathSuite
    @ClassNameRegexFilter(value = { "org.pitest.extension.common.testsuitefinder.Abstract" })
    class Suite {

    }

    assertEquals(Collections.emptyList(), this.testee.apply(new TestClass(
        Suite.class)));
  }

  @Test
  public void testNameFilterFindsMultipleClasses() {
    @ClasspathSuite
    @ClassNameRegexFilter(value = {
        "org.pitest.extension.common.testsuitefinder.Foo",
        "org.pitest.extension.common.testsuitefinder.Bar" })
    class Suite {

    }

    assertEquals(Arrays.asList(new TestClass(Bar.class), new TestClass(
        Foo.class)), this.testee.apply(new TestClass(Suite.class)));
  }

  @Test
  public void testNameFilterReturnsRegexMatches() {
    @ClasspathSuite
    @ClassNameRegexFilter(value = { "[\\S]+testsuitefinder\\.(Foo|Bar)" })
    class Suite {

    }

    assertEquals(Arrays.asList(new TestClass(Bar.class), new TestClass(
        Foo.class)), this.testee.apply(new TestClass(Suite.class)));
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
