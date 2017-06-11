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
package org.pitest.mutationtest.report.html;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Iterator;

import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.FunctionalIterable;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.SideEffect1;

public class InputStreamLineIterable implements FunctionalIterable<String> {

  private final BufferedReader reader;
  private String               next;

  public InputStreamLineIterable(final Reader reader) {
    this.reader = new BufferedReader(reader);
    advance();
  }

  private void advance() {
    try {
      this.next = this.reader.readLine();
    } catch (final IOException e) {
      this.next = null;
    }
  }

  public String next() {
    final String t = this.next;
    advance();
    return t;
  }

  @Override
  public Iterator<String> iterator() {
    return new Iterator<String>() {

      @Override
      public boolean hasNext() {
        return InputStreamLineIterable.this.next != null;
      }

      @Override
      public String next() {
        return InputStreamLineIterable.this.next();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

    };
  }

  @Override
  public FunctionalList<String> filter(final F<String, Boolean> predicate) {
    return FCollection.filter(this, predicate);
  }

  @Override
  public void forEach(final SideEffect1<String> e) {
    FCollection.forEach(this, e);
  }

  @Override
  public <B> FunctionalList<B> map(final F<String, B> f) {
    return FCollection.map(this, f);
  }

  @Override
  public <B> void mapTo(final F<String, B> f, final Collection<? super B> bs) {
    FCollection.mapTo(this, f, bs);
  }

  @Override
  public <B> FunctionalList<B> flatMap(final F<String, ? extends Iterable<B>> f) {
    return FCollection.flatMap(this, f);
  }

  @Override
  public boolean contains(final F<String, Boolean> predicate) {
    return FCollection.contains(this, predicate);
  }

}
