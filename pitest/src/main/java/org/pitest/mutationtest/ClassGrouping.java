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
package org.pitest.mutationtest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ClassGrouping implements Iterable<String> {

  private final static int   PARENT_INDEX = 0;
  private final List<String> children     = new ArrayList<String>(1);

  public ClassGrouping(final String parent, final Collection<String> children) {
    this.children.add(parent);
    this.children.addAll(children);
  }

  public String getParent() {
    return this.children.get(PARENT_INDEX);
  }

  public Iterator<String> iterator() {
    return this.children.iterator();
  }

  public void addChild(final Class<?> clazz) {
    this.children.add(clazz.getName());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((this.children == null) ? 0 : this.children.hashCode());
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
    final ClassGrouping other = (ClassGrouping) obj;
    if (this.children == null) {
      if (other.children != null) {
        return false;
      }
    } else if (!this.children.equals(other.children)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "Group of " + getParent().toString() + " and "
        + (this.children.size() - 1) + " children";
  }

}
