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
package org.pitest.execute.containers;

import java.util.HashMap;
import java.util.Map;

import org.pitest.functional.Option;
import org.pitest.testapi.Description;
import org.pitest.testapi.MetaData;
import org.pitest.testapi.TestResult;
import org.pitest.testapi.TestUnitState;

public class ExtendedTestResult extends TestResult {

  private final Map<Class<? extends MetaData>, Object> values = new HashMap<Class<? extends MetaData>, Object>(
                                                                  1);

  public ExtendedTestResult(final Description description, final Throwable t,
      final MetaData... value) {
    super(description, t, TestUnitState.FINISHED);
    for (final MetaData each : value) {
      this.values.put(each.getClass(), each);
    }

  }

  @Override
  public <T extends MetaData> Option<T> getValue(final Class<T> type) {
    return Option.some(type.cast(this.values.get(type)));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = (prime * result)
        + ((this.values == null) ? 0 : this.values.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ExtendedTestResult other = (ExtendedTestResult) obj;
    if (this.values == null) {
      if (other.values != null) {
        return false;
      }
    } else if (!this.values.equals(other.values)) {
      return false;
    }
    return true;
  }

}
