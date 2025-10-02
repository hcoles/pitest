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
package org.pitest.junit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.pitest.testapi.TestGroupConfig.emptyConfig;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.pitest.testapi.NullExecutionListener;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.testapi.TestUnit;

public class ParameterisedJUnitTestFinderTest {

  private ParameterisedJUnitTestFinder testee;

  @Before
  public void setup() {
    this.testee = new ParameterisedJUnitTestFinder(emptyConfig());
  }

  @RunWith(Parameterized.class)
  public static class ParameterisedTest {

    public ParameterisedTest(final int i) {

    }

    @Parameters
    public static Collection<Object[]> params() {
      return Arrays.asList(new Object[][] { { 1 }, { 2 }, { 3 }, { 4 } });
    }

    @Test
    public void test() {
    }

    @Test
    public void anotherTest() {

    }

  }

  @Test
  public void shouldCreateTestUnitForEachParameterMethodCombinationOfParameterizedTest() {
    final Collection<TestUnit> actual = findWithTestee(ParameterisedTest.class);
    assertEquals(8, actual.size());
  }

  @Test
  public void shouldReturnNoTestForNonParameterisedTest() {
    final Collection<TestUnit> actual = findWithTestee(ParameterisedJUnitTestFinderTest.class);
    assertTrue(actual.isEmpty());
  }

  @Test
  public void includesSuppliedCategories() {
    setConfig(emptyConfig()
            .withIncludedGroups(ACategory.class.getName()));
    final Collection<TestUnit> actual = findWithTestee(Tagged.class);
    assertThat(actual).hasSize(2);
  }

  @Test
  public void excludesSuppliedCategories() {
    setConfig(emptyConfig()
            .withIncludedGroups(ACategory.class.getName())
            .withExcludedGroups(AnotherCategory.class.getName()));
    final Collection<TestUnit> actual = findWithTestee(Tagged.class);
    assertThat(actual).isEmpty();
  }

  @Test
  public void excludesInheritedCategories() {
    setConfig(emptyConfig()
            .withIncludedGroups(ACategory.class.getName())
            .withExcludedGroups(AnotherCategory.class.getName()));
    final Collection<TestUnit> actual = findWithTestee(IndirectlyTagged.class);
    assertThat(actual).isEmpty();
  }

  private void setConfig(TestGroupConfig config) {
    this.testee = new ParameterisedJUnitTestFinder(config);
  }


  private Collection<TestUnit> findWithTestee(final Class<?> clazz) {
    return this.testee.findTestUnits(clazz, new NullExecutionListener());
  }

  interface ACategory {

  }

  interface AnotherCategory {

  }

  @Category({ACategory.class, AnotherCategory.class})
  @RunWith(Parameterized.class)
  public static class Tagged {
    @Parameterized.Parameter
    public String vt;
    @Parameterized.Parameter(1)
    public String vtp;

    @Parameterized.Parameters(name = "P {1}")
    public static Iterable<Object[]> versions() {
      return Arrays.asList(new Object[][] {
              {"foo-1.4", "1.4"},
              {"foo-2.4", "2.4"}
      });
    }

    @Test
    public void aTest() {
    }
  }

  public static class IndirectlyTagged extends Tagged {
    @Test
    public void anotherTest() {
    }
  }


}
