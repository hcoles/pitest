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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public final class MutableList<A> implements FunctionalList<A> {

  private final List<A> impl;

  public MutableList(final A... as) { // NO_UCD
    this(Arrays.asList(as));
  }

  public MutableList(final List<A> impl) { // NO_UCD
    this.impl = impl;
  }

  public MutableList() {
    this(new ArrayList<A>());
  }

  @Override
  public boolean add(final A o) {
    return this.impl.add(o);
  }

  @Override
  public boolean addAll(final Collection<? extends A> c) {
    return this.impl.addAll(c);
  }

  @Override
  public void clear() {
    this.impl.clear();
  }

  @Override
  public boolean contains(final Object o) {
    return this.impl.contains(o);
  }

  @Override
  public boolean containsAll(final Collection<?> c) {
    return this.impl.containsAll(c);
  }

  @Override
  public boolean isEmpty() {
    return this.impl.isEmpty();
  }

  @Override
  public Iterator<A> iterator() {
    return this.impl.iterator();
  }

  @Override
  public boolean remove(final Object o) {
    return this.impl.remove(o);
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    return this.impl.removeAll(c);
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    return this.impl.retainAll(c);
  }

  @Override
  public int size() {
    return this.impl.size();
  }

  @Override
  public Object[] toArray() {
    return this.impl.toArray();
  }

  @Override
  public <T> T[] toArray(final T[] a) {
    return this.impl.toArray(a);
  }

  @Override
  public boolean contains(final F<A, Boolean> predicate) {
    return FCollection.contains(this, predicate);
  }

  @Override
  public FunctionalList<A> filter(final F<A, Boolean> predicate) {
    return FCollection.filter(this, predicate);
  }

  @Override
  public <B> FunctionalList<B> flatMap(final F<A, ? extends Iterable<B>> f) {
    return FCollection.flatMap(this, f);
  }

  @Override
  public void forEach(final SideEffect1<A> e) {
    FCollection.forEach(this, e);
  }

  @Override
  public <B> FunctionalList<B> map(final F<A, B> f) {
    return FCollection.map(this, f);
  }

  @Override
  public <B> void mapTo(final F<A, B> f, final Collection<? super B> bs) {
    FCollection.mapTo(this, f, bs);
  }
  
  @Override
  public Option<A> findFirst(F<A, Boolean> predicate) {
    return FCollection.findFirst(impl, predicate);
  }


  @Override
  public void add(final int arg0, final A arg1) {
    this.impl.add(arg0, arg1);
  }

  @Override
  public boolean addAll(final int arg0, final Collection<? extends A> arg1) {
    return this.impl.addAll(arg0, arg1);
  }

  @Override
  public A get(final int index) {
    return this.impl.get(index);
  }

  @Override
  public int indexOf(final Object arg0) {
    return this.impl.indexOf(arg0);
  }

  @Override
  public int lastIndexOf(final Object arg0) {
    return this.impl.lastIndexOf(arg0);
  }

  @Override
  public ListIterator<A> listIterator() {
    return this.impl.listIterator();
  }

  @Override
  public ListIterator<A> listIterator(final int index) {
    return this.impl.listIterator(index);
  }

  @Override
  public A remove(final int index) {
    return this.impl.remove(index);
  }

  @Override
  public A set(final int index, final A element) {
    return this.impl.set(index, element);
  }

  @Override
  public List<A> subList(final int fromIndex, final int toIndex) {
    return this.impl.subList(fromIndex, toIndex);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result)
        + ((this.impl == null) ? 0 : this.impl.hashCode());
    return result;
  }

  @SuppressWarnings({ "rawtypes" })
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
    final MutableList other = (MutableList) obj;
    if (this.impl == null) {
      if (other.impl != null) {
        return false;
      }
    } else if (!this.impl.equals(other.impl)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return this.impl.toString();
  }

}
