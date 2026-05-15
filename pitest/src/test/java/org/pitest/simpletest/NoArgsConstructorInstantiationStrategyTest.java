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
package org.pitest.simpletest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pitest.simpletest.steps.NoArgsInstantiateStep;

public class NoArgsConstructorInstantiationStrategyTest {

  private NoArgsConstructorInstantiationStrategy testee;

  @Before
  public void setUp() {
    this.testee = new NoArgsConstructorInstantiationStrategy();
  }

  @Test
  public void shouldAllwaysReturnsTrue() {
    assertThat(this.testee.canInstantiate(NoArgsConstructorInstantiationStrategyTest.class)).isTrue();
    assertThat(this.testee.canInstantiate(null)).isTrue();
  }

  @Test
  public void shouldCreateSingleInstantiateTestStep() {
    final List<NoArgsInstantiateStep> expected = new ArrayList<>();
    expected.add(NoArgsInstantiateStep
        .instantiate(NoArgsConstructorInstantiationStrategyTest.class));
    assertThat(expected).isEqualTo(this.testee.instantiations(NoArgsConstructorInstantiationStrategyTest.class));
  }

}
