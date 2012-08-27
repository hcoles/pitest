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

public class MutableList<A> implements FunctionalList<A> {

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

  public boolean add(final A o) {
    return this.impl.add(o);
  }

  public boolean addAll(final Collection<? extends A> c) {
    return this.impl.addAll(c);
  }

  public void clear() {
    this.impl.clear();
  }

  public boolean contains(final Object o) {
    return this.impl.contains(o);
  }

  public boolean containsAll(final Collection<?> c) {
    return this.impl.containsAll(c);
  }

  public boolean isEmpty() {
    return this.impl.isEmpty();
  }

  public Iterator<A> iterator() {
    return this.impl.iterator();
  }

  public boolean remove(final Object o) {
    return this.impl.remove(o);
  }

  public boolean removeAll(final Collection<?> c) {
    return this.impl.removeAll(c);
  }

  public boolean retainAll(final Collection<?> c) {
    return this.impl.retainAll(c);
  }

  public int size() {
    return this.impl.size();
  }

  public Object[] toArray() {
    return this.impl.toArray();
  }

  public <T> T[] toArray(final T[] a) {
    return this.impl.toArray(a);
  }

  public boolean contains(final F<A, Boolean> predicate) {
    return FCollection.contains(this, predicate);
  }

  public FunctionalList<A> filter(final F<A, Boolean> predicate) {
    return FCollection.filter(this, predicate);
  }

  public <B> FunctionalList<B> flatMap(final F<A, ? extends Iterable<B>> f) {
    return FCollection.flatMap(this, f);
  }

  public void forEach(final SideEffect1<A> e) {
    FCollection.forEach(this, e);
  }

  public <B> FunctionalList<B> map(final F<A, B> f) {
    return FCollection.map(this, f);
  }

  public <B> void mapTo(final F<A, B> f, final Collection<? super B> bs) {
    FCollection.mapTo(this, f, bs);
  }

  public void add(final int arg0, final A arg1) {
    this.impl.add(arg0, arg1);
  }

  public boolean addAll(final int arg0, final Collection<? extends A> arg1) {
    return this.impl.addAll(arg0, arg1);
  }

  public A get(final int index) {
    return this.impl.get(index);
  }

  public int indexOf(final Object arg0) {
    return this.impl.indexOf(arg0);
  }

  public int lastIndexOf(final Object arg0) {
    return this.impl.lastIndexOf(arg0);
  }

  public ListIterator<A> listIterator() {
    return this.impl.listIterator();
  }

  public ListIterator<A> listIterator(final int index) {
    return this.impl.listIterator(index);
  }

  public A remove(final int index) {
    return this.impl.remove(index);
  }

  public A set(final int index, final A element) {
    return this.impl.set(index, element);
  }

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
