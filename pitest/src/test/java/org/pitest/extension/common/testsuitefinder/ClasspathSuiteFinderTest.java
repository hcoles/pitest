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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
  public void shouldReturnEmptyListIfClassPathSuiteAnnotationNotPresent() {
    @ClassNameRegexFilter("[\\S]+")
    class Suite {

    }

    final PITStaticMethodSuiteFinder testee = new PITStaticMethodSuiteFinder();
    final TestClass root = new TestClass(Suite.class);
    assertEquals(Collections.emptyList(), testee.apply(root));
  }

  @Test
  public void shouldBeAbleToFindSingleClassUsingRegexNameFilter() {
    @ClasspathSuite
    @ClassNameRegexFilter(FOO_NAME)
    class Suite {

    }

    assertEquals(Arrays.asList(new TestClass(Foo.class)),
        this.testee.apply(new TestClass(Suite.class)));
  }

  @ClasspathSuite
  @ClassNameRegexFilter("org.pitest.extension.common.testsuitefinder.ClasspathSuiteFinderTest\\$ExampleSuite")
  public static class ExampleSuite {

  }

  @Test
  public void shouldNeverReturnParentClassUsingRegexNameFilter() {
    assertEquals(Collections.emptyList(),
        this.testee.apply(new TestClass(ExampleSuite.class)));
  }

  @Test
  public void shouldNeverReturnsAbstractClassUsingNameRegexFilter() {
    @ClasspathSuite
    @ClassNameRegexFilter("org.pitest.extension.common.testsuitefinder.Abstract")
    class Suite {

    }

    assertEquals(Collections.emptyList(),
        this.testee.apply(new TestClass(Suite.class)));
  }

  @Test
  public void shouldBeAbleToFindMultipleClassesUsingRegexNameFilter() {
    @ClasspathSuite
    @ClassNameRegexFilter(value = { FOO_NAME, BAR_NAME })
    class Suite {

    }

    assertEquals(
        Arrays.asList(new TestClass(Bar.class), new TestClass(Foo.class)),
        this.testee.apply(new TestClass(Suite.class)));
  }

  @Test
  public void shouldReturnRegexMatchesWhenUsingRegexNameFilter() {
    @ClasspathSuite
    @ClassNameRegexFilter(value = { "[\\S]+testsuitefinder\\.(Foo|Bar)" })
    class Suite {

    }

    assertEquals(
        Arrays.asList(new TestClass(Bar.class), new TestClass(Foo.class)),
        this.testee.apply(new TestClass(Suite.class)));
  }

  @Test
  public void shouldBeAbleToFindSingleClassUsingGlobNameFilter() {
    @ClasspathSuite
    @ClassNameGlobFilter(FOO_NAME)
    class Suite {

    }

    assertEquals(Arrays.asList(new TestClass(Foo.class)),
        this.testee.apply(new TestClass(Suite.class)));
  }

  @Test
  public void shouldBeAbleToFindMultipleClassesUsingGlobNameFilter() {
    @ClasspathSuite
    @ClassNameGlobFilter("org.pitest.extension.common.testsuitefinder.Foo*")
    class Suite {

    }

    assertSameContentsIgnoringOrder(Arrays.asList(new TestClass(
        FooDerived.class), new TestClass(Foo.class)),
        this.testee.apply(new TestClass(Suite.class)));
  }

  private void assertSameContentsIgnoringOrder(
      final Collection<TestClass> expected, final Collection<TestClass> actual) {
    final List<TestClass> e = new ArrayList<TestClass>();
    e.addAll(expected);
    final List<TestClass> a = new ArrayList<TestClass>();
    a.addAll(actual);

    final Comparator<TestClass> c = new Comparator<TestClass>() {

      public int compare(final TestClass o1, final TestClass o2) {
        return o1.hashCode() - o2.hashCode();
      }

    };

    Collections.sort(e, c);
    Collections.sort(a, c);

    assertEquals(e, a);

  }

  @Test
  public void shouldBeAbleToApplyMultipleGLobFilters() {
    @ClasspathSuite
    @ClassNameGlobFilter(value = {
        "org.pitest.extension.common.testsuitefinder.Foo*",
        "org.pitest.extension.common.testsuitefinder.Bar*" })
    class Suite {

    }

    assertSameContentsIgnoringOrder(Arrays.asList(new TestClass(
        FooDerived.class), new TestClass(Bar.class), new TestClass(Foo.class),
        new TestClass(BarDerived.class)), this.testee.apply(new TestClass(
        Suite.class)));
  }

  @Test
  public void shouldBeAbleToMixGlobAndRegexFilters() {
    @ClasspathSuite
    @ClassNameGlobFilter(FOO_NAME)
    @ClassNameRegexFilter(BAR_NAME)
    class Suite {

    }

    assertEquals(
        Arrays.asList(new TestClass(Bar.class), new TestClass(Foo.class)),
        this.testee.apply(new TestClass(Suite.class)));
  }

  public static class InnerClass {

  }

  @Test
  public void shouldBeAbleToSearchForInnerClasses() {
    @ClasspathSuite(excludeInnerClasses = false)
    @ClassNameGlobFilter("org.pitest.extension.common.testsuitefinder.*Inner*")
    class Suite {

    }

    assertEquals(Arrays.asList(new TestClass(InnerClass.class)),
        this.testee.apply(new TestClass(Suite.class)));
  }

  @Test
  public void shouldIncludeOnlySubClassesOfTheSuppliedTypeWhenUsingBaseClassFilter() {
    @ClasspathSuite
    @ClassNameRegexFilter(value = { "[\\S]+testsuitefinder\\.[\\S]+" })
    @BaseClassFilter({ Foo.class })
    class Suite {

    }

    assertSameContentsIgnoringOrder(Arrays.asList(new TestClass(
        FooDerived.class), new TestClass(Foo.class)),
        this.testee.apply(new TestClass(Suite.class)));
  }

  @Test
  public void shouldExcludeSubClassesOfTheSuppliuedTypeWhenUsingExcludeBaseClassFilter() {
    @ClasspathSuite
    @ClassNameRegexFilter(value = { "[\\S]+testsuitefinder\\.[\\S]+" })
    @BaseClassFilter({ Foo.class })
    @ExcludeBaseClassFilter({ Exclude.class })
    class Suite {

    }

    assertEquals(Arrays.asList(new TestClass(Foo.class)),
        this.testee.apply(new TestClass(Suite.class)));
  }

}
