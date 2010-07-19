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

import junit.framework.TestCase;

import org.junit.Ignore;
import org.pitest.annotations.MutationTest;
import org.pitest.annotations.TestClass;
import org.pitest.mutationtest.Mutation;
import org.pitest.mutationtest.MutationConfig;

@TestClass(TestA1.Testee.class)
public class TestA1 extends TestCase {

  public static class Testee {
    public static int returnOne() {
      return 1;
    }

    public static int returnTwo() {
      return 2;
    }
  }

  @MutationTest(mutateClass = Testee.class)
  public static MutationConfig config() {
    return new MutationConfig(Mutation.RETURN_VALS);
  }

  public void test1() {
    assertEquals(1, Testee.returnOne());
  }

  @Ignore
  public void test2() {

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
