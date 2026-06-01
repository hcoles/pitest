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
package org.pitest.mutationtest.statistics;

import org.junit.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class MutationStatisticsTest {

  @Test
  public void shouldNotHaveHundredPercentIfNotAllKilled() {
    assertThat(new MutationStatistics(null, 2000, 1999, 1, 1, null).getPercentageDetected()).isEqualTo(99);
  }

  @Test
  public void shouldHaveHundredPercentIfAllKilled() {
    assertThat(new MutationStatistics(null, 2000, 2000, 1, 1, null).getPercentageDetected()).isEqualTo(100);
  }

  @Test
  public void shouldHaveHundredPercentIfNoMutations() {
    assertThat(new MutationStatistics(null, 0, 0, 0, 0, null).getPercentageDetected()).isEqualTo(100);
  }

  @Test
  public void shouldCalculatePrecisePercentageDetected() {
    assertThat(new MutationStatistics(null, 413, 324, 1, 1, null).getPercentageDetected(2))
        .isEqualByComparingTo(new BigDecimal("78.45"));
  }

  @Test
  public void shouldCalculatePreciseTestStrength() {
    assertThat(new MutationStatistics(null, 413, 324, 335, 1, null).getTestStrength(2))
        .isEqualByComparingTo(new BigDecimal("96.72"));
  }

}
