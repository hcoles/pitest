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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Foo.class })
public class PowerMockTest {

  @Test
  public void testWithMockito() {
    PowerMockito.mockStatic(Foo.class);
    // use Mockito to set up your expectation
    // Mockito.when(Foo.returnOne()).thenReturn(2);

    new CallFoo().call();

    PowerMockito.verifyStatic();
    Foo.returnOne();

  }

}

class CallFoo {
  public void call() {
    System.out.println("Call called");
    Foo.returnOne();
  }
}

class Foo {

  public static int returnOne() {
    System.out.println("static method called");
    return 1;
  }
}