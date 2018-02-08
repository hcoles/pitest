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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Functional programming style operations for plain old Java iterables.
 */
public abstract class FCollection {

  public static <A> void forEach(final Iterable<? extends A> as,
      final SideEffect1<A> e) {
    for (final A a : as) {
      e.apply(a);
    }
  }

  public static <A, B> void mapTo(final Iterable<? extends A> as,
      final Function<A, B> f, final Collection<? super B> bs) {
    if (as != null) {
      for (final A a : as) {
        bs.add(f.apply(a));
      }
    }
  }

  public static <A, B> List<B> map(final Iterable<? extends A> as,
      final Function<A, B> f) {
    final List<B> bs = emptyList();
    mapTo(as, f, bs);
    return bs;
  }

  public static <A, B> void flatMapTo(final Iterable<? extends A> as,
      final Function<A, ? extends Iterable<B>> f, final Collection<? super B> bs) {
    if (as != null) {
      for (final A a : as) {
        for (final B each : f.apply(a)) {
          bs.add(each);
        }
      }
    }
  }

  public static <A, B> List<B> flatMap(
      final Iterable<? extends A> as, final Function<A, ? extends Iterable<B>> f) {
    final List<B> bs = emptyList();
    flatMapTo(as, f, bs);
    return bs;
  }

  private static <T> List<T> emptyList() {
    return new ArrayList<>();
  }

  public static <T> List<T> filter(final Iterable<? extends T> xs,
      final Predicate<T> predicate) {
    final List<T> dest = emptyList();
    filter(xs, predicate, dest);
    return dest;
  }

  public static <T> void filter(final Iterable<? extends T> xs,
      final Predicate<T> predicate, final Collection<? super T> dest) {
    for (final T x : xs) {
      if (predicate.test(x)) {
        dest.add(x);
      }
    }
  }


  public static <T> java.util.Optional<T> findFirst(final Iterable<? extends T> xs,
      final Predicate<T> predicate) {
    for (final T x : xs) {
      if (predicate.test(x)) {
        return java.util.Optional.ofNullable(x);
      }
    }
    return java.util.Optional.empty();
  }


  public static <T> boolean contains(final Iterable<? extends T> xs,
      final Predicate<T> predicate) {
    for (final T x : xs) {
      if (predicate.test(x)) {
        return true;
      }
    }
    return false;

  }

  public static <A, B> A fold(final BiFunction<A, B, A> f, final A z,
      final Iterable<? extends B> xs) {
    A p = z;
    for (final B x : xs) {
      p = f.apply(p, x);
    }
    return p;
  }

  public static <T> Collection<T> flatten(
      final Iterable<? extends Iterable<? extends T>> ts) {
    final List<T> list = new ArrayList<>();
    for (final Iterable<? extends T> it : ts) {
      for (final T each : it) {
        list.add(each);
      }
    }
    return list;
  }

  public static <T> List<List<T>> splitToLength(
      final int targetLength, final Iterable<T> ts) {
    final List<List<T>> list = new ArrayList<>();
    List<T> temp = new ArrayList<>();
    int i = 0;
    for (final T each : ts) {
      if (i == targetLength) {
        list.add(temp);
        temp = new ArrayList<>();
        i = 0;
      }
      temp.add(each);
      i++;
    }
    if (!temp.isEmpty()) {
      list.add(temp);
    }
    return list;
  }

  public static <A, B> Map<A, Collection<B>> bucket(final Iterable<B> bs,
      final Function<B, A> f) {
    final Map<A, Collection<B>> bucketed = new HashMap<>();
    for (final B each : bs) {
      final A key = f.apply(each);
      Collection<B> existing = bucketed.get(key);
      if (existing == null) {
        existing = new ArrayList<>();
        bucketed.put(key, existing);
      }
      existing.add(each);
    }
    return bucketed;
  }

}
