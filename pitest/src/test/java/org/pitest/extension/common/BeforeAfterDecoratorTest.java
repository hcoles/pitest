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
package org.pitest.extension.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import java.util.Collections;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.extension.TestUnit;
import org.pitest.teststeps.CallStep;

public class BeforeAfterDecoratorTest {

  private BeforeAfterDecorator testee;

  @Mock
  private TestUnit             tu;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void shouldIterateOverChild() {
    this.testee = new BeforeAfterDecorator(this.tu,
        Collections.<CallStep> emptyList(), Collections.<CallStep> emptyList());
    final Iterator<TestUnit> it = this.testee.iterator();
    assertSame(this.tu, it.next());
    assertFalse(it.hasNext());
  }
}
