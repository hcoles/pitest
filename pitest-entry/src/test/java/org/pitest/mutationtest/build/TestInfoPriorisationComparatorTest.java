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
package org.pitest.mutationtest.build;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pitest.classinfo.ClassName;
import org.pitest.coverage.TestInfo;
import java.util.Optional;

public class TestInfoPriorisationComparatorTest {

  private static final String            TARGET         = "target";
  private static final int               TIME_WEIGHTING = 1000;

  private TestInfoPriorisationComparator testee;

  @Before
  public void setUp() {
    this.testee = new TestInfoPriorisationComparator(ClassName.fromString(TARGET),
        TIME_WEIGHTING);
  }

  @Test
  public void shouldPrioritiseFasterTestsThatDirectlyTestTargetBeforeSlowerTestsThatDirectlyTestTarget() {

    final TestInfo reallySlow = testInfo(2000, TARGET);
    final TestInfo slow = testInfo(2, TARGET);
    final TestInfo fast = testInfo(1, TARGET);
    final List<TestInfo> actual = sortWithTestee(slow, reallySlow, fast);

    assertThat(actual, is(Arrays.asList(fast, slow, reallySlow)));
  }

  @Test
  public void shouldFavourTestsThatDirectlyTestTargetOverFasterTestsThatDontByApplyingATimeWeighting() {

    final TestInfo slowButClose = testInfo(TIME_WEIGHTING, TARGET);
    final TestInfo fastButDistant = testInfo(1, "notTarget");
    final TestInfo verySlowButClose = testInfo(
        TIME_WEIGHTING + fastButDistant.getTime() + 1, TARGET);

    final List<TestInfo> actual = sortWithTestee(verySlowButClose,
        slowButClose, fastButDistant);

    assertThat(actual,
        is(Arrays.asList(slowButClose, fastButDistant, verySlowButClose)));
  }

  @Test
  public void shouldFavourTestsThatCoverFewerLinesInTheSameAmountOfTime() {
    final TestInfo fastButWide = testInfo(1, TARGET, 100);
    final TestInfo fastAndTargetted = testInfo(1, TARGET, 1);
    final TestInfo fastAndSlightlyTargetted = testInfo(1, TARGET, 50);

    final List<TestInfo> actual = sortWithTestee(fastButWide,
        fastAndSlightlyTargetted, fastAndTargetted);

    assertThat(actual, is(Arrays.asList(fastAndTargetted,
        fastAndSlightlyTargetted, fastButWide)));

  }

  private List<TestInfo> sortWithTestee(final TestInfo... testInfos) {
    final List<TestInfo> list = Arrays.asList(testInfos);
    Collections.sort(list, this.testee);
    return list;
  }

  private TestInfo testInfo(final int time, final String target) {
    return testInfo(time, target, 0);
  }

  private TestInfo testInfo(final int time, final String target,
      final int linesCovered) {
    return new TestInfo("", time + target + linesCovered, time,
        Optional.ofNullable(ClassName.fromString(target)), linesCovered);
  }

}
