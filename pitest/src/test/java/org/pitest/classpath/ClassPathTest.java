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
package org.pitest.classpath;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import java.util.Optional;
import org.pitest.functional.prelude.Prelude;

public class ClassPathTest {

  private ClassPath     testee;

  @Mock
  private ClassPathRoot firstComponent;

  @Mock
  private ClassPathRoot secondComponent;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.testee = new ClassPath(this.firstComponent, this.secondComponent);
    when(this.firstComponent.cacheLocation()).thenReturn(Optional.ofNullable("foo"));
    when(this.firstComponent.classNames()).thenReturn(
        Collections.singletonList("FooClass"));
    when(this.secondComponent.cacheLocation()).thenReturn(Optional.ofNullable("bar"));
    when(this.secondComponent.classNames()).thenReturn(
        Collections.singletonList("BarClass"));
  }

  @Test
  public void shouldReturnBytesFromClasspathInputStream() throws IOException {
    final InputStream stream = Mockito.mock(InputStream.class);
    when(this.firstComponent.getData(any(String.class))).thenReturn(stream);
    when(stream.read(any(byte[].class), anyInt(), anyInt())).thenReturn(-1);
    this.testee.getClassData("foo");
    verify(stream).read(any(byte[].class), anyInt(), anyInt());
    verify(stream).close();
  }

  @Test
  public void shouldReturnAllClassNames() {
    assertEquals(Arrays.asList("FooClass", "BarClass"),
        this.testee.classNames());
  }

  @Test
  public void shouldFindMatchingClasses() {
    assertEquals(Arrays.asList("FooClass"),
        this.testee.findClasses(Prelude.isEqualTo("FooClass")));
  }

  @Test
  public void shouldAllowSubComponentsToBeSelected() {
    assertEquals(Collections.singletonList("FooClass"), this.testee
        .getComponent(rootIsEqualTo("foo")).classNames());
  }

  private Predicate<ClassPathRoot> rootIsEqualTo(final String value) {
    return a -> a.cacheLocation().get().equals(value);
  }

}
