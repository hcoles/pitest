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
package org.pitest.reflection;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.pitest.internal.ClassByteArraySource;
import org.pitest.internal.ClassPathByteArraySource;

public class ReflectionTest {

  private ClassByteArraySource source;

  @Before
  public void setUp() {
    this.source = new ClassPathByteArraySource();
  }

  private static class PrivateStatic {
    private class InnerStatic {

    }
  }

  @Test
  public void allInnerClassesShouldReturnPrivateStaticInnerClasses() {
    assertTrue(Reflection.allInnerClasses(ReflectionTest.class, this.source)
        .contains(PrivateStatic.class.getName()));
  }

  private class PrivateNonStatic {
    private class InnerNonStatic {

    }
  }

  @Test
  public void allInnerClassesShouldReturnPrivateNonStaticInnerClasses() {
    assertTrue(Reflection.allInnerClasses(ReflectionTest.class, this.source)
        .contains(PrivateNonStatic.class.getName()));
  }

  @Test
  public void allInnerClassesShouldReturnNestedInnerClasses() {
    assertTrue(Reflection.allInnerClasses(ReflectionTest.class, this.source)
        .contains(PrivateNonStatic.InnerNonStatic.class.getName()));
    assertTrue(Reflection.allInnerClasses(ReflectionTest.class, this.source)
        .contains(PrivateStatic.InnerStatic.class.getName()));
  }

  @Test
  public void allInnerClassesShouldReturnLocalClasses() {
    final Runnable r = new Runnable() {

      public void run() {

      }

    };
    assertTrue(Reflection.allInnerClasses(ReflectionTest.class, this.source)
        .contains(r.getClass().getName()));
  }

}
