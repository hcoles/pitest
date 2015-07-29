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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.mutationtest.ListenerArguments;
import org.pitest.mutationtest.MutationResultListener;
import org.pitest.mutationtest.MutationResultListenerFactory;

public class CompoundListenerFactoryTest {

  private CompoundListenerFactory       testee;

  @Mock
  private MutationResultListenerFactory firstChild;

  @Mock
  private MutationResultListenerFactory secondChild;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.testee = new CompoundListenerFactory(Arrays.asList(this.firstChild,
        this.secondChild));
  }

  @Test
  public void shouldCreateACombinedListenerForAllChildFactories() {
    final MutationResultListener listenerOne = mock(MutationResultListener.class);
    final MutationResultListener listenerTwo = mock(MutationResultListener.class);
    when(
        this.firstChild.getListener(any(Properties.class),
            any(ListenerArguments.class))).thenReturn(listenerOne);
    when(
        this.secondChild.getListener(any(Properties.class),
            any(ListenerArguments.class))).thenReturn(listenerTwo);
    this.testee.getListener(null, null).runStart();
    verify(listenerOne, times(1)).runStart();
    verify(listenerTwo, times(1)).runStart();
  }

}
