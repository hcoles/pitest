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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.mutationtest.ListenerArguments;
import org.pitest.mutationtest.MutationResultListener;
import org.pitest.mutationtest.MutationResultListenerFactory;

import java.util.Collections;
import java.util.Properties;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CompoundListenerFactoryTest {

  private CompoundListenerFactory       testee;

  @Mock
  private MutationResultListenerFactory firstChild;

  @Mock
  private MutationResultListenerFactory secondChild;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);

    when(firstChild.provides()).thenReturn(MutationResultListenerFactory.LEGACY_MODE);
    when(secondChild.provides()).thenReturn(MutationResultListenerFactory.LEGACY_MODE);

    this.testee = new CompoundListenerFactory(emptyList(),asList(this.firstChild,
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
    this.testee.getListener(new Properties(), someArgs()).runStart();
    verify(listenerOne, times(1)).runStart();
    verify(listenerTwo, times(1)).runStart();
  }

  private ListenerArguments someArgs() {
    return new ListenerArguments(null, null, null, null,
            0, false, null, Collections.emptyList());
  }

}
