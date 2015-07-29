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

import java.util.Collections;
import java.util.List;

import org.pitest.simpletest.steps.NoArgsInstantiateStep;

public class NoArgsConstructorInstantiationStrategy implements
InstantiationStrategy {

  @Override
  public List<TestStep> instantiations(final Class<?> clazz) {
    return Collections.<TestStep> singletonList(NoArgsInstantiateStep
        .instantiate(clazz));
  }

  @Override
  public boolean canInstantiate(final Class<?> clazz) {
    // unwise premature optimization
    // don't check if a no-args constructor is present
    return true;
  }

}
