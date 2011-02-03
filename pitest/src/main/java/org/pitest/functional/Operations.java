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

public class Operations {

  public static <A, B, C, D, E, FF, G> F5<A, B, C, D, E, G> uncurry(
      final F6<A, B, C, D, E, FF, G> func, final FF f) {
    return new F5<A, B, C, D, E, G>() {
      public G apply(final A a, final B b, final C c, final D d, final E e) {
        return func.apply(a, b, c, d, e, f);
      }
    };
  }

  public static <A, B, C, D, E, FF> F4<A, B, C, D, FF> uncurry(
      final F5<A, B, C, D, E, FF> func, final E e) {
    return new F4<A, B, C, D, FF>() {
      public FF apply(final A a, final B b, final C c, final D d) {
        return func.apply(a, b, c, d, e);
      }
    };
  }

  public static <A, B, C, D, E> F3<A, B, C, E> uncurry(
      final F4<A, B, C, D, E> func, final D d) {
    return new F3<A, B, C, E>() {
      public E apply(final A a, final B b, final C c) {
        return func.apply(a, b, c, d);
      }
    };
  }

  public static <A, B, C, D> F2<A, B, D> uncurry(final F3<A, B, C, D> func,
      final C c) {
    return new F2<A, B, D>() {
      public D apply(final A a, final B b) {
        return func.apply(a, b, c);
      }
    };
  }

  public static <A, B, C> F<A, C> uncurry(final F2<A, B, C> func, final B b) {
    return new F<A, C>() {
      public C apply(final A a) {
        return func.apply(a, b);
      }
    };
  }

  public static <A, B, C> F<A, C> combine(final F<A, B> f1, final F<B, C> f2) {
    return new F<A, C>() {
      public C apply(final A a) {
        return f2.apply(f1.apply(a));
      }
    };
  }

}
