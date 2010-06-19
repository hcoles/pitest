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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Suite;
import org.junit.runners.Parameterized.Parameters;

public class ParameterizedInstantiationStrategyTest {

  private ParameterizedInstantiationStrategy testee;

  @Before
  public void setUp() {
    this.testee = new ParameterizedInstantiationStrategy();
  }

  @Test
  public void testCanInstantiateReturnsFalseForClassNotAnnotatedWithRunWith() {
    class UnAnnotated {

    }

    assertFalse(this.testee.canInstantiate(UnAnnotated.class));
  }

  @Test
  public void testCanInstantiateReturnsFalseForClassRunWithRunnerOtherThanParametized() {
    @RunWith(Suite.class)
    class Annotated {

    }

    assertFalse(this.testee.canInstantiate(Annotated.class));
  }

  @Test
  public void testCanInstantiateReturnsTrueForClassRunWithParameterized() {
    @RunWith(Parameterized.class)
    class Annotated {

    }

    assertTrue(this.testee.canInstantiate(Annotated.class));
  }

  @RunWith(PITJUnitRunner.class)
  static class AnnotatedWithParametersMethod {
    @Parameters
    public static Collection<Object[]> params() {
      return Arrays.asList(new Object[][] { { 1 }, { 2 }, { 3 } });
    }

  }

  @Test
  public void testCanInstantiateReturnsTrueForClassRunWithPitRunnerWithParametersMethod() {
    assertTrue(this.testee.canInstantiate(AnnotatedWithParametersMethod.class));
  }

  @Test
  public void testCanInstantiateReturnsFalseForClassRunWithPitRunnerWithoutParametersMethod() {
    @RunWith(PITJUnitRunner.class)
    class Annotated {

    }

    assertFalse(this.testee.canInstantiate(Annotated.class));
  }

  @Test
  public void testReturnsTestStepForEachParameter() {
    assertEquals(AnnotatedWithParametersMethod.params().size(), this.testee
        .instantiations(AnnotatedWithParametersMethod.class).size());
  }

}
