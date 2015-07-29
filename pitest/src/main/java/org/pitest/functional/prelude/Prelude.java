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
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.pitest.functional.F;
import org.pitest.functional.SideEffect1;
import org.pitest.functional.predicate.And;
import org.pitest.functional.predicate.Not;
import org.pitest.functional.predicate.Or;
import org.pitest.functional.predicate.Predicate;

/**
 * @author henry
 *
 */
public abstract class Prelude {

  public static final <A> And<A> and(final F<A, Boolean>... ps) {
    return new And<A>(Arrays.asList(ps));
  }

  public static final <A> And<A> and(final Iterable<? extends F<A, Boolean>> ps) {
    return new And<A>(ps);
  }

  public static final <A> Not<A> not(final F<A, Boolean> p) {
    return new Not<A>(p);
  }

  public static final <A> Or<A> or(final Predicate<A>... ps) {
    return new Or<A>(Arrays.asList(ps));
  }

  public static final <A> Or<A> or(final Iterable<Predicate<A>> ps) {
    return new Or<A>(ps);
  }

  public static final <A> SideEffect1<A> accumulateTo(
      final Collection<A> collection) {
    return new SideEffect1<A>() {

      @Override
      public void apply(final A a) {
        collection.add(a);
      }

    };

  }

  public static <A, B> SideEffect1<A> putToMap(final Map<A, B> map,
      final B value) {
    return new SideEffect1<A>() {
      @Override
      public void apply(final A key) {
        map.put(key, value);
      }
    };
  }

  public static final <A> F<A, A> id() {
    return new F<A, A>() {
      @Override
      public A apply(final A a) {
        return a;
      }
    };
  }

  public static final <A> F<A, A> id(final Class<A> type) {
    return id();
  }

  public static final <T> SideEffect1<T> print() {
    return printTo(System.out);
  }

  public static final <T> SideEffect1<T> print(final Class<T> type) {
    return print();
  }

  public static final <T> SideEffect1<T> printTo(final Class<T> type,
      final PrintStream stream) {
    return printTo(stream);
  }

  public static final <T> SideEffect1<T> printTo(final PrintStream stream) {
    return new SideEffect1<T>() {
      @Override
      public void apply(final T a) {
        stream.print(a);
      }
    };
  }

  public static <T> SideEffect1<T> printWith(final T t) {
    return new SideEffect1<T>() {
      @Override
      public void apply(final T a) {
        System.out.print(t + " : " + a);
      }
    };
  }

  public static <T extends Number> Predicate<T> isGreaterThan(final T value) {
    return new Predicate<T>() {
      @Override
      public Boolean apply(final T o) {
        return o.longValue() > value.longValue();
      }
    };
  }

  public static <T> Predicate<T> isEqualTo(final T value) {
    return new Predicate<T>() {
      @Override
      public Boolean apply(final T o) {
        return o.equals(value);
      }
    };
  }

  public static <T> Predicate<T> isNotNull() {
    return new Predicate<T>() {
      @Override
      public Boolean apply(final T o) {
        return (o != null);
      }
    };
  }

  public static <T> Predicate<T> isNull() {
    return new Predicate<T>() {
      @Override
      public Boolean apply(final T o) {
        return (o == null);
      }
    };
  }

  public static <T> F<T, Iterable<T>> asList(final Class<T> type) {
    return new F<T, Iterable<T>>() {

      @Override
      public Iterable<T> apply(final T a) {
        return Collections.singletonList(a);
      }

    };
  }

  public static <T> SideEffect1<T> noSideEffect(final Class<T> clazz) {
    return new SideEffect1<T>() {

      @Override
      public void apply(final T a) {
      }

    };
  }
}
