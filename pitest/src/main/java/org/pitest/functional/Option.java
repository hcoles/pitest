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

import java.util.Collections;
import java.util.Iterator;

import org.pitest.PitError;

public abstract class Option<T> implements FunctionalIterable<T> {

  @SuppressWarnings({ "rawtypes" })
  private final static None NONE = new None();

  private Option() {
  }

  public abstract T value();

  public abstract T getOrElse(T defaultValue);

  public abstract boolean hasSome();

  public boolean contains(final F<T, Boolean> predicate) {
    return FCollection.contains(this, predicate);
  }

  public FunctionalList<T> filter(final F<T, Boolean> predicate) {
    return FCollection.filter(this, predicate);
  }

  public <B> FunctionalList<B> flatMap(final F<T, ? extends Iterable<B>> f) {
    return FCollection.flatMap(this, f);
  }

  public void forEach(final SideEffect1<T> e) {
    FCollection.forEach(this, e);
  }

  public <B> FunctionalList<B> map(final F<T, B> f) {
    return FCollection.map(this, f);
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

  public final static class None<T> extends Option<T> {

    private static final long serialVersionUID = 1L;

    private None() {

    }

    public Iterator<T> iterator() {
      return Collections.<T> emptySet().iterator();
    }

    @Override
    public T value() {
      throw new PitError("Tried to retrieve value but had None.");
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

  public final static class Some<T> extends Option<T> {

    private static final long serialVersionUID = 1L;

    final T                   _value;

    private Some(final T value) {
      this._value = value;
    }

    @Override
    public T value() {
      return this._value;
    }

    public Iterator<T> iterator() {
      return Collections.singleton(this._value).iterator();

    }

    @Override
    public T getOrElse(final T defaultValue) {
      return this._value;
    }

    @Override
    public boolean hasSome() {
      return true;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result
          + ((this._value == null) ? 0 : this._value.hashCode());
      return result;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      @SuppressWarnings("rawtypes")
      final Some other = (Some) obj;
      if (this._value == null) {
        if (other._value != null) {
          return false;
        }
      } else if (!this._value.equals(other._value)) {
        return false;
      }
      return true;
    }

    @Override
    public String toString() {
      return "Some(" + this._value + ")";
    }

  }

}