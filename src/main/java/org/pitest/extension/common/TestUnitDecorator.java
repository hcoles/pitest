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
package org.pitest.extension.common;

import org.pitest.Description;
import org.pitest.extension.TestUnit;
import org.pitest.functional.Option;

public abstract class TestUnitDecorator implements TestUnit {

  private static final long serialVersionUID = 1L;

  private final TestUnit    child;

  protected TestUnitDecorator(final TestUnit child) {
    this.child = child;
  }

  // public Configuration configuration() {
  // return this.child.configuration();
  // }

  public Option<TestUnit> dependsOn() {
    return this.child.dependsOn();
  }

  public Description description() {
    return this.child.description();
  }

  public void setDependency(final TestUnit dependsOn) {
    this.child.setDependency(dependsOn);

  }

  protected TestUnit child() {
    return this.child;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((this.child == null) ? 0 : this.child.hashCode());
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
    final TestUnitDecorator other = (TestUnitDecorator) obj;
    if (this.child == null) {
      if (other.child != null) {
        return false;
      }
    } else if (!this.child.equals(other.child)) {
      return false;
    }
    return true;
  }

}
