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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationResultListener;

public class CompoundTestListenerTest {

  private CompoundTestListener   testee;

  @Mock
  private MutationResultListener firstChild;

  @Mock
  private MutationResultListener secondChild;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.testee = new CompoundTestListener(Arrays.asList(this.firstChild,
        this.secondChild));
  }

  @Test
  public void shouldCallOnRunStartForAllChildren() {
    this.testee.runStart();
    verify(this.firstChild, times(1)).runStart();
    verify(this.secondChild, times(1)).runStart();
  }

  @Test
  public void shouldCallOnRunEndForAllChildren() {
    this.testee.runEnd();
    verify(this.firstChild, times(1)).runEnd();
    verify(this.secondChild, times(1)).runEnd();
  }

  @Test
  public void shouldCallOnTestErrorForAllChildren() {
    final ClassMutationResults metaData = new ClassMutationResults(
        Collections.<MutationResult> emptyList());
    this.testee.handleMutationResult(metaData);
    verify(this.firstChild, times(1)).handleMutationResult(metaData);
    verify(this.secondChild, times(1)).handleMutationResult(metaData);
  }

}
