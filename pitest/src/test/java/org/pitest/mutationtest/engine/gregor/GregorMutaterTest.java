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
package org.pitest.mutationtest.engine.gregor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.classinfo.ClassName;
import org.pitest.functional.Option;
import org.pitest.functional.predicate.True;
import org.pitest.internal.ClassByteArraySource;
import org.pitest.mutationtest.Mutator;

public class GregorMutaterTest {

  @Mock
  private ClassByteArraySource byteSource;

  private GregorMutater        testee;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.testee = new GregorMutater(this.byteSource, True.<MethodInfo> all(),
        Mutator.RETURN_VALS.asCollection(), Collections.<String> emptyList());
  }

  @Test
  public void shouldReturnUnmutatedClassWhenRequested() {
    byte[] bytes = {};
    when(this.byteSource.apply(anyString())).thenReturn(Option.some(bytes));
    byte[] actual = this.testee.getOriginalClass(new ClassName("foo"));
    assertEquals(bytes, actual);
  }
}
