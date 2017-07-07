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
package org.pitest.mutationtest.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.pitest.mutationtest.LocationMother.aMutationId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;

public class LimitNumberOfMutationPerClassFilterTest {

  private LimitNumberOfMutationPerClassFilter testee;
  private Mutater unused;

  @Before
  public void setUp() {
    this.testee = new LimitNumberOfMutationPerClassFilter(3);
  }

  @Test
  public void shouldReturnUnalteredCollectionIfCollectionContainsLessThenMaxMutations() {
    final Collection<MutationDetails> expected = make(1, 2);
    assertSame(expected, this.testee.intercept(expected, unused));
  }

  @Test
  public void shouldReturnUnalteredCollectionIfCollectionContainsExactlyMaxMutations() {
    final Collection<MutationDetails> expected = make(1, 2, 3);
    assertSame(expected, this.testee.intercept(expected, unused));
  }

  @Test
  public void shouldTrimCollectionToMaximumAllowedNumber() {
    final Collection<MutationDetails> input = make(1, 2, 3, 4);
    assertEquals(make(1, 2, 3), this.testee.intercept(input, unused));
  }

  @Test
  public void shouldUseEvenDistributionOfMutations() {
    final Collection<MutationDetails> input = make(1, 2, 3, 4, 5, 6, 7, 8);
    assertEquals(make(2, 4, 6), this.testee.intercept(input, unused));
  }

  private Collection<MutationDetails> make(final Integer... ids) {
    final List<MutationDetails> ms = new ArrayList<MutationDetails>();
    for (final int each : ids) {
      ms.add(makeMutation(each));
    }
    return ms;
  }

  private MutationDetails makeMutation(final int id) {
    return new MutationDetails(makeId(id), "", "", 0, 0);
  }

  private MutationIdentifier makeId(final int id) {
    return aMutationId().withIndex(id).build();
  }

}
