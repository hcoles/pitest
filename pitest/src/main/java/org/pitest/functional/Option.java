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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public abstract class Option<T> implements FunctionalIterable<T> {

  @SuppressWarnings({ "rawtypes" })
  private static final None NONE = new None();

  private Option() {
  }

  public abstract T value();

  public abstract T getOrElse(T defaultValue);

  public abstract boolean hasSome();

  @Override
  public boolean contains(final F<T, Boolean> predicate) {
    return FCollection.contains(this, predicate);
  }

  @Override
  public FunctionalList<T> filter(final F<T, Boolean> predicate) {
    return FCollection.filter(this, predicate);
  }

  @Override
  public <B> FunctionalList<B> flatMap(final F<T, ? extends Iterable<B>> f) {
    return FCollection.flatMap(this, f);
  }

  @Override
  public void forEach(final SideEffect1<T> e) {
    FCollection.forEach(this, e);
  }

  @Override
  public <B> FunctionalList<B> map(final F<T, B> f) {
    return FCollection.map(this, f);
  }

  @Override
  public <B> void mapTo(final F<T, B> f, final Collection<? super B> bs) {
    FCollection.mapTo(this, f, bs);
  }
  
  @SuppressWarnings("unchecked")
  public static <T> Option<T> some(final T value) {
    if (value == null) {
      return NONE;
    } else {
      return new Some<T>(value);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> None<T> none() {
    return NONE;
  }

  public boolean hasNone() {
    return !hasSome();
  }

  public static final class None<T> extends Option<T> {

    private None() {

    }

    @Override
    public Iterator<T> iterator() {
      return Collections.<T> emptySet().iterator();
    }

    @Override
    public T value() {
      throw new UnsupportedOperationException(
          "Tried to retrieve value but had None.");
    }

    @Override
    public T getOrElse(final T defaultValue) {
      return defaultValue;
    }

    @Override
    public boolean hasSome() {
      return false;
    }

  }

  public static final class Some<T> extends Option<T> {

    private final T value;

    private Some(final T value) {
      this.value = value;
    }

    @Override
    public T value() {
      return this.value;
    }

    @Override
    public Iterator<T> iterator() {
      return Collections.singleton(this.value).iterator();

    }

    @Override
    public T getOrElse(final T defaultValue) {
      return this.value;
    }

    @Override
    public boolean hasSome() {
      return true;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = (prime * result)
          + ((this.value == null) ? 0 : this.value.hashCode());
      return result;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      @SuppressWarnings("rawtypes")
      final Some other = (Some) obj;
      if (this.value == null) {
        if (other.value != null) {
          return false;
        }
      } else if (!this.value.equals(other.value)) {
        return false;
      }
      return true;
    }

    @Override
    public String toString() {
      return "Some(" + this.value + ")";
    }

  }

}