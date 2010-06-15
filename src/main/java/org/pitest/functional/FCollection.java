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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author henry
 * 
 */
public abstract class FCollection {

  public static <A> void forEach(final Iterable<? extends A> as,
      final SideEffect1<A> e) {
    for (final A a : as) {
      e.apply(a);
    }
  }

  public static <A, B> void map(final Iterable<? extends A> as,
      final F<A, B> f, final Collection<? super B> bs) {
    for (final A a : as) {
      bs.add(f.apply(a));
    }
  }

  public static <A, B> List<B> map(final Iterable<? extends A> as,
      final F<A, B> f) {
    final List<B> bs = emptyList();
    map(as, f, bs);
    return bs;
  }

  public static <A, B> void flatMap(final Iterable<? extends A> as,
      final F<A, ? extends Iterable<B>> f, final SideEffect1<B> effect) {
    for (final A a : as) {
      for (final B each : f.apply(a)) {
        effect.apply(each);
      }
    }
  }

  public static <A, B> void flatMap(final Iterable<? extends A> as,
      final F<A, ? extends Iterable<B>> f, final Collection<? super B> bs) {
    for (final A a : as) {
      for (final B each : f.apply(a)) {
        bs.add(each);
      }
    }
  }

  public static <A, B> List<B> flatMap(final Iterable<? extends A> as,
      final F<A, ? extends Iterable<B>> f) {
    final List<B> bs = emptyList();
    flatMap(as, f, bs);
    return bs;
  }

  private static <T> List<T> emptyList() {
    return new ArrayList<T>();
  }

  public static <T> List<T> filter(final Iterable<? extends T> xs,
      final F<T, Boolean> predicate) {
    final List<T> dest = emptyList();
    filter(xs, predicate, dest);
    return dest;
  }

  public static <T> void filter(final Iterable<? extends T> xs,
      final F<T, Boolean> predicate, final Collection<? super T> dest) {
    for (final T x : xs) {
      if (predicate.apply(x)) {
        dest.add(x);
      }
    }
  }

}
