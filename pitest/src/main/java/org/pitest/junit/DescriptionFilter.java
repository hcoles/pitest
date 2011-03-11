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

package org.pitest.junit;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

public class DescriptionFilter extends Filter {

  private final String desc;

  public DescriptionFilter(final String description) {
    this.desc = description;
  }

  @Override
  public boolean shouldRun(final Description description) {
    return description.toString().equals(this.desc);
  }

  @Override
  public String describe() {
    return this.desc;
  }

  @Override
  public String toString() {
    return describe();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.desc == null) ? 0 : this.desc.hashCode());
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
    final DescriptionFilter other = (DescriptionFilter) obj;
    if (this.desc == null) {
      if (other.desc != null) {
        return false;
      }
    } else if (!this.desc.equals(other.desc)) {
      return false;
    }
    return true;
  }

}
