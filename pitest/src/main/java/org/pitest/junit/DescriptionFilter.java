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

}
