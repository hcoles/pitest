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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

public class JUnit4SuiteFinderTest {

  private JUnit4SuiteFinder testee;

  @Before
  public void setUp() {
    this.testee = new JUnit4SuiteFinder();
  }

  @Test
  public void shouldReturnEmptyCollectionForUnannotatedClass() {
    final Class<?> noAnnotation = JUnit4SuiteFinderTest.class;
    assertTrue(this.testee.apply(noAnnotation).isEmpty());
  }

  private static class HideFromJUnit {

    @RunWith(Suite.class)
    @SuiteClasses({ String.class, Integer.class })
    private static class AnnotatedJUnit {

    }
  }

  @Test
  public void shouldReturnTestClassForEachClassInSuiteClassesAnnotationWhenRunnerIsSuite() {
    final Class<?> annotated = HideFromJUnit.AnnotatedJUnit.class;
    final Collection<Class<?>> expected = Arrays.<Class<?>> asList(
        String.class, Integer.class);
    assertEquals(expected, this.testee.apply(annotated));
  }

}
