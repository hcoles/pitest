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
package org.pitest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.extension.TestUnit;

public class TestGroupTest {

  @Mock
  private TestUnit  emptyTestUnit;
  private TestGroup testee;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    this.testee = new TestGroup();
  }

  @Test
  public void testIteratorReturnsNoValuesWhenNoneAdded() {
    assertFalse(this.testee.iterator().hasNext());
  }

  @Test
  public void testIteratorReturnsAddedNoValues() {
    this.testee.add(this.emptyTestUnit);
    assertSame(this.emptyTestUnit, this.testee.iterator().next());
  }

  @Test
  public void testContainsReturnsFalseForEmptyGroup() {
    assertFalse(this.testee.contains(this.emptyTestUnit));
  }

  @Test
  public void testContainsReturnsFalseWhenDescriptionsAreNotEqual() {
    when(this.emptyTestUnit.description()).thenReturn(
        new Description("foo", String.class, null));
    final TestUnit notEqual = mock(TestUnit.class);
    when(this.emptyTestUnit.description()).thenReturn(
        new Description("bar", String.class, null));
    this.testee.add(this.emptyTestUnit);
    assertFalse(this.testee.contains(notEqual));
  }

  @Test
  public void testContainsReturnsTrueWhenDescriptionsAreEquals() {
    when(this.emptyTestUnit.description()).thenReturn(
        new Description("foo", String.class, null));
    this.testee.add(this.emptyTestUnit);
    assertTrue(this.testee.contains(this.emptyTestUnit));
  }

}
