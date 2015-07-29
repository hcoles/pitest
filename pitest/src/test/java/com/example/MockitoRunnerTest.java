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
package com.example;

import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MockitoRunnerTest {

  @Mock
  MockitoFoo foo;

  @Test
  public void testThings() {
    final MockitoCallFoo testee = new MockitoCallFoo(this.foo);
    testee.call();
    verify(this.foo).foo();

  }

}

class MockitoCallFoo {
  final MockitoFoo foo;

  public MockitoCallFoo(final MockitoFoo foo) {
    this.foo = foo;
  }

  public void call() {
    this.foo.foo();
  }
}

class MockitoFoo {

  public void foo() {
    System.out.println("foo");
  }
}