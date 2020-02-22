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
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.pitest.functional.predicate.And;
import org.pitest.functional.predicate.Or;

/**
 * @author henry
 *
 */
public abstract class Prelude {

  @SafeVarargs
  public static final <A> And<A> and(final Predicate<A>... ps) {
    return new And<>(Arrays.asList(ps));
  }

  public static final <A> And<A> and(final Iterable<? extends Predicate<A>> ps) {
    return new And<>(ps);
  }

  public static final <A> Predicate<A> not(final Predicate<A> p) {
    return p.negate();
  }

  @SafeVarargs
  public static final <A> Or<A> or(final Predicate<A>... ps) {
    return new Or<>(Arrays.asList(ps));
  }

  public static final <A> Or<A> or(final Iterable<Predicate<A>> ps) {
    return new Or<>(ps);
  }

  public static final <T> Consumer<T> print() {
    return printTo(System.out);
  }

  public static final <T> Consumer<T> print(final Class<T> type) {
    return print();
  }

  public static final <T> Consumer<T> printTo(final Class<T> type,
      final PrintStream stream) {
    return printTo(stream);
  }

  public static final <T> Consumer<T> printTo(final PrintStream stream) {
    return a -> stream.print(a);
  }

  public static <T> Consumer<T> printWith(final T t) {
    return a -> System.out.print(t + " : " + a);
  }

}
