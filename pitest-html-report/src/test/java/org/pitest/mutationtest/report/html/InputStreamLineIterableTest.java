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
package org.pitest.mutationtest.report.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.pitest.functional.prelude.Prelude.isEqualTo;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pitest.functional.F;
import org.pitest.functional.Option;
import org.pitest.functional.prelude.Prelude;

public class InputStreamLineIterableTest {

  private InputStreamLineIterable testee;
  private List<String>            actual;

  @Before
  public void setUp() {
    this.actual = new ArrayList<String>();
    final StringReader input = new StringReader("1\n2\n3\n");
    this.testee = new InputStreamLineIterable(input);
  }

  @Test
  public void shouldApplyForEachToAllLines() {
    this.testee.forEach(Prelude.accumulateTo(this.actual));
    assertEquals(this.actual, Arrays.asList("1", "2", "3"));
  }

  @Test
  public void shouldApplyFilterToAllLines() {
    assertEquals(Arrays.asList("1"), this.testee.filter(isEqualTo("1")));
  }

  @Test
  public void shouldApplyMapToAllLines() {
    assertEquals(Arrays.asList("1", "2", "3"),
        this.testee.map(Prelude.<String> id()));
  }

  @Test
  public void shouldApplyFlatMapToAllLines() {
    assertEquals(Arrays.asList("1", "3"), this.testee.flatMap(mapIfNotTwo()));
  }

  @Test
  public void shouldReturnTrueWhenContainsCalledAndPredicateMatches() {
    assertTrue(this.testee.contains(isEqualTo("1")));
  }

  @Test
  public void shouldReturnFalseWhenContainsCalledAndPredicateDoesNotMatch() {
    assertFalse(this.testee.contains(isEqualTo("10")));
  }
  
  private F<String, Option<String>> mapIfNotTwo() {
    return new F<String, Option<String>>() {
      @Override
      public Option<String> apply(final String a) {
        if (a.equals("2")) {
          return Option.none();
        }
        return Option.some(a);
      }

    };
  }

}
