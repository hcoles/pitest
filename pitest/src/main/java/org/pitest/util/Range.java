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
package org.pitest.util;

import java.util.Iterator;

import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.FunctionalIterable;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.SideEffect1;

public class Range implements FunctionalIterable<Integer> {

  private final int start;
  private final int end;

  public Range(final int start, final int end) {
    this.start = start;
    this.end = end;
  }

  public Iterator<Integer> iterator() {
    return new Iterator<Integer>() {
      int value = Range.this.start;

      public boolean hasNext() {
        return this.value <= Range.this.end;
      }

      public Integer next() {
        this.value++;
        return this.value - 1;
      }

      public void remove() {
        throw new UnsupportedOperationException();

      }

    };
  }

  public int getLastNumberInRange() {
    return this.end;
  }

  public boolean contains(final F<Integer, Boolean> predicate) {
    return FCollection.contains(this, predicate);
  }

  public FunctionalList<Integer> filter(final F<Integer, Boolean> predicate) {
    return FCollection.filter(this, predicate);
  }

  public <B> FunctionalList<B> flatMap(final F<Integer, ? extends Iterable<B>> f) {
    return FCollection.flatMap(this, f);
  }

  public void forEach(final SideEffect1<Integer> e) {
    FCollection.forEach(this, e);
  }

  public <B> FunctionalList<B> map(final F<Integer, B> f) {
    return FCollection.map(this, f);
  }

}
