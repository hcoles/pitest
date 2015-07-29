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
package org.pitest.simpletest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author henry
 *
 */
public class EqualitySet<T> implements Iterable<T> {

  private final List<T>             members = new ArrayList<T>();
  private final EqualityStrategy<T> equality;

  public EqualitySet(final EqualityStrategy<T> equality) {
    this.equality = equality;
  }

  public boolean add(final T arg0) {
    if (!contains(arg0)) {
      return this.members.add(arg0);
    }
    return false;
  }

  public boolean addAll(final Collection<? extends T> arg0) {
    boolean isAdded = false;
    for (final T each : arg0) {
      isAdded |= add(each);
    }
    return isAdded;
  }

  public boolean contains(final T arg0) {
    for (final T each : this.members) {
      if (this.equality.isEqual(arg0, each)) {
        return true;
      }
    }
    return false;
  }

  public boolean isEmpty() {
    return this.members.isEmpty();
  }

  @Override
  public Iterator<T> iterator() {
    return this.members.iterator();
  }

  public Collection<T> toCollection() {
    return this.members;
  }

}
