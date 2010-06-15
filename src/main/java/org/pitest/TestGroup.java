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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.pitest.extension.TestUnit;

public class TestGroup implements Iterable<TestUnit>, Serializable {

  private static final long    serialVersionUID = 1L;

  private final List<TestUnit> children         = new ArrayList<TestUnit>();

  public void add(final TestUnit tu) {
    this.children.add(tu);
  }

  public boolean contains(final TestUnit value) {
    for (final TestUnit each : this.children) {
      if (each.description().equals(value.description())) {
        return true;
      }
    }
    return false;
  }

  public Iterator<TestUnit> iterator() {
    return this.children.iterator();
  }

}
