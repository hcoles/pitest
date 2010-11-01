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
package com.example;

import org.jmock.MockObjectTestCase;
import org.pitest.annotations.TestClass;

@TestClass(TestA1.Testee.class)
public class TestA1 extends MockObjectTestCase {

  public static class Testee {
    public static int returnOne() {
      return 1;
    }

    public static int returnTwo() {
      return 2;
    }

    public static int returnThree() {
      return 3;
    }

  }

  public void test1() throws InterruptedException {
    System.out.println("returnOne = " + Testee.returnOne());
    assertEquals(1, Testee.returnOne());

  }

  public void test2() throws InterruptedException {
    System.out.println("TestA1.test2 A1 returnTwo = " + Testee.returnTwo());
    assertEquals(2, Testee.returnTwo());

  }

  public void test3() {

  }

  public void test4() {

  }

  public void test5() {

  }

  public void test6() {

  }

}
