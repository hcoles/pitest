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
package org.pitest.functional.prelude;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.pitest.functional.predicate.And;
import org.pitest.functional.predicate.Or;

/**
 * @author henry
 *
 */
public abstract class Prelude {

  @SafeVarargs
  public static <A> And<A> and(final Predicate<A>... ps) {
    return new And<>(Arrays.asList(ps));
  }

  public static <A> And<A> and(final Iterable<? extends Predicate<A>> ps) {
    return new And<>(ps);
  }

  public static <A> Predicate<A> not(final Predicate<A> p) {
    return p.negate();
  }

  @SafeVarargs
  public static <A> Or<A> or(final Predicate<A>... ps) {
    return new Or<>(Arrays.asList(ps));
  }

  public static <A> Or<A> or(final Iterable<Predicate<A>> ps) {
    return new Or<>(ps);
  }

  public static <A, B> Consumer<A> putToMap(final Map<A, B> map,
      final B value) {
    return key -> map.put(key, value);
  }

  public static <A> Function<A, A> id() {
    return a -> a;
  }

  public static <A> Function<A, A> id(final Class<A> type) {
    return id();
  }

  public static <T> Consumer<T> println() {
    return printlnTo(System.out);
  }

  public static <T> Consumer<T> println(final Class<T> type) {
    return println();
  }

  public static <T> Consumer<T> printlnTo(final Class<T> type,
                                          final PrintStream stream) {
    return printlnTo(stream);
  }

  public static <T> Consumer<T> printlnTo(final PrintStream stream) {
    return stream::println;
  }

  public static <T> Consumer<T> printlnWith(final T t) {
    return a -> System.out.println(t + " : " + a);
  }

  public static <T> Function<T, Iterable<T>> asList(final Class<T> type) {
    return Collections::singletonList;
  }

  public static <T> Consumer<T> noSideEffect(final Class<T> clazz) {
    return a -> {
    };
  }
}
