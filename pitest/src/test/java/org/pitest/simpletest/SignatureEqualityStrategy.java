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

import java.lang.reflect.Method;

public class SignatureEqualityStrategy implements EqualityStrategy<TestMethod> {

  @Override
  public boolean isEqual(final TestMethod lhs, final TestMethod rhs) {
    final Method m1 = lhs.getMethod();
    final Method m2 = rhs.getMethod();

    return haveSameSignature(m1, m2);
  }

  private boolean haveSameSignature(final Method me, final Method other) {

    if ((me.getName().equals(other.getName()))) {
      if (!me.getReturnType().equals(other.getReturnType())) {
        return false;
      }

      final Class<?>[] params1 = me.getParameterTypes();
      final Class<?>[] params2 = other.getParameterTypes();
      if (params1.length == params2.length) {
        for (int i = 0; i < params1.length; i++) {
          if (params1[i] != params2[i]) {
            return false;
          }
        }
        return true;
      }
    }

    return false;
  }

};