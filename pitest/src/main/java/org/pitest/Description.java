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

package org.pitest;

import static org.pitest.util.Functions.classToName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.FunctionalIterable;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.Option;
import org.pitest.functional.SideEffect1;

public final class Description implements FunctionalIterable<Class<?>>,
    Serializable {

  private static final long          serialVersionUID = 1L;
  private final Collection<Class<?>> testClasses      = new ArrayList<Class<?>>(
                                                          1);
  private final String               name;
  private final Option<TestMethod>   method;

  public Description(final String name, final Collection<Class<?>> testClass,
      final TestMethod method) {
    this.testClasses.addAll(testClass);
    this.name = name;
    this.method = Option.some(method);
  }

  public Description(final String name, final Class<?> testClass,
      final TestMethod method) {
    this(name, Collections.<Class<?>> singleton(testClass), method);
  }

  public Collection<Class<?>> getTestClasses() {
    return this.testClasses;
  }

  public Class<?> getFirstTestClass() {
    return this.testClasses.iterator().next();
  }

  public String getName() {
    return this.name;
  }

  public Option<TestMethod> getMethod() {
    return this.method;
  }

  @Override
  public String toString() {
    if (!this.testClasses.isEmpty()) {
      return this.getFirstTestClass() + "." + this.name;
    } else {
      return this.name;
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((this.method == null) ? 0 : this.method.hashCode());
    result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
    result = prime * result
        + ((this.testClasses == null) ? 0 : this.testClasses.hashCode());
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
    final Description other = (Description) obj;
    if (this.method == null) {
      if (other.method != null) {
        return false;
      }
    } else if (!this.method.equals(other.method)) {
      return false;
    }
    if (this.name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!this.name.equals(other.name)) {
      return false;
    }
    if (this.testClasses == null) {
      if (other.testClasses != null) {
        return false;
      }
    } else if (!this.testClasses.equals(other.testClasses)) {
      return false;
    }
    return true;
  }

  public Iterator<Class<?>> iterator() {
    return this.testClasses.iterator();
  }

  public boolean contains(final F<Class<?>, Boolean> predicate) {
    return FCollection.contains(this.testClasses, predicate);
  }

  public Collection<String> getTestClassNames() {
    return FCollection.map(this.testClasses, classToName());
  }

  public void forEach(final SideEffect1<Class<?>> e) {
    FCollection.forEach(this, e);
  }

  public <B> FunctionalList<B> map(final F<Class<?>, B> f) {
    return FCollection.map(this, f);
  }

  public <B> void mapTo(final F<Class<?>, B> f, final Collection<? super B> bs) {
    FCollection.map(this, f, bs);
  }

  public <B> FunctionalList<B> flatMap(
      final F<Class<?>, ? extends Iterable<B>> f) {
    return FCollection.flatMap(this, f);
  }

  public FunctionalList<Class<?>> filter(final F<Class<?>, Boolean> predicate) {
    return null;
  }

}
