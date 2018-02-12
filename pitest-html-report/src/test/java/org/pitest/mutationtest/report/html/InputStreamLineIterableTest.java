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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;
import org.pitest.functional.FCollection;

public class InputStreamLineIterableTest {

  private InputStreamLineIterable testee;

  @Before
  public void setUp() {
    final StringReader input = new StringReader("1\n2\n3\n");
    this.testee = new InputStreamLineIterable(input);
  }

  @Test
  public void shouldReadAllInput() {
    assertThat(FCollection.map(testee, s -> s)).containsExactly("1", "2", "3");
  }

}
