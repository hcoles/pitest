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
package org.pitest.classinfo;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Optional;

public class TestToClassMapperTest {

  @Mock
  private ClassByteArraySource source;

  private Repository           repository;

  private TestToClassMapper    testee;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.repository = new Repository(this.source);
    this.testee = new TestToClassMapper(this.repository);
  }

  @Test
  public void shouldMapTestsPostfixedWithTestToTesteeWhenTesteeExists() {
    final byte[] bytes = { 0 };
    when(this.source.getBytes("com.example.Foo"))
        .thenReturn(Optional.ofNullable(bytes));
    assertEquals(ClassName.fromString("com.example.Foo"),
        this.testee.findTestee("com.example.FooTest").get());
  }

  @Test
  public void shouldMapTestsPrefixedWithTestToTesteeWhenTesteeExists() {
    final byte[] bytes = { 0 };
    when(this.source.getBytes("com.example.Foo"))
        .thenReturn(Optional.ofNullable(bytes));
    assertEquals(ClassName.fromString("com.example.Foo"),
        this.testee.findTestee("com.example.TestFoo").get());
  }

  @Test
  public void shouldReturnNoneWhenNoTesteeExistsMatchingNamingConvention() {
    final byte[] bytes = null;
    when(this.source.getBytes("com.example.Foo"))
        .thenReturn(Optional.ofNullable(bytes));
    assertEquals(Optional.empty(), this.testee.findTestee("com.example.TestFoo"));
  }

}
