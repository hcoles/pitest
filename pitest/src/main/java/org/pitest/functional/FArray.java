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
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Slightly functional style operations for arrays.
 */
public abstract class FArray {

  public static <T> void filter(final T[] xs, final Predicate<T> predicate,
      final Collection<T> dest) {
    if (xs != null) {
      for (final T x : xs) {
        if (predicate.test(x)) {
          dest.add(x);
        }
      }
    }
  }

  public static <T> List<T> filter(final T[] xs, final Predicate<T> predicate) {
    final List<T> dest = new ArrayList<>();
    filter(xs, predicate, dest);
    return dest;
  }

  public static <T> boolean contains(final T[] xs, final Predicate<T> predicate) {
    for (final T x : xs) {
      if (predicate.test(x)) {
        return true;
      }
    }
    return false;

  }

  public static <A, B> void flatMapTo(final A[] as,
      final Function<A, ? extends Iterable<B>> f, final Collection<? super B> bs) {
    if (as != null) {
      for (final A a : as) {
        for (final B each : f.apply(a)) {
          bs.add(each);
        }
      }
    }
  }

  public static <A, B> List<B> flatMap(final A[] as,
      final Function<A, ? extends Iterable<B>> f) {
    final List<B> bs = emptyList();
    flatMapTo(as, f, bs);
    return bs;
  }

  private static <T> List<T> emptyList() {
    return new ArrayList<>();
  }

}
