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
package org.pitest.mutationtest.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.classinfo.ClassInfo;
import org.pitest.testapi.TestClassIdentifier;

public class CompoundTestClassIdentifierTest {

  private CompoundTestClassIdentifier testee;

  @Mock
  private TestClassIdentifier         firstChild;

  @Mock
  private TestClassIdentifier         secondChild;

  private ClassInfo                   clazz;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.testee = new CompoundTestClassIdentifier(Arrays.asList(
        this.firstChild, this.secondChild));
  }

  @Test
  public void shouldReturnTrueWhenFirstChildReturnsTrue() {
    when(this.firstChild.isATestClass(this.clazz)).thenReturn(true);
    assertTrue(this.testee.isATestClass(this.clazz));
    verify(this.secondChild, never()).isATestClass(this.clazz);
  }

  @Test
  public void shouldReturnTrueWhenLastChildReturnsTrue() {
    when(this.firstChild.isATestClass(this.clazz)).thenReturn(false);
    when(this.secondChild.isATestClass(this.clazz)).thenReturn(true);
    assertTrue(this.testee.isATestClass(this.clazz));
  }

  @Test
  public void shouldReturnFalseWhenAllChildrenReturnFalse() {
    when(this.firstChild.isATestClass(this.clazz)).thenReturn(false);
    when(this.secondChild.isATestClass(this.clazz)).thenReturn(false);
    assertFalse(this.testee.isATestClass(this.clazz));
  }

}
