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
package org.pitest.functional;

/**
 * @author henry
 * 
 */
public abstract class Common {

  public final static <A> F<A, A> id() {
    return new F<A, A>() {
      public A apply(final A a) {
        return a;
      }
    };
  }

  public final static <T> SideEffect1<T> print() {
    return new SideEffect1<T>() {
      public void apply(final T a) {
        System.out.println(a);
      }
    };
  }

  public static <T> SideEffect1<T> printWith(final T t) {
    return new SideEffect1<T>() {
      public void apply(final T a) {
        System.out.println(t + " : " + a);
      }
    };
  }

}
