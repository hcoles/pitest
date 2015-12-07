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
package org.pitest.classinfo;

import org.objectweb.asm.Opcodes;
import org.pitest.functional.F5;

public enum BridgeMethodFilter implements
F5<Integer, String, String, String, String[], Boolean> {

  INSTANCE;

  @Override
  public Boolean apply(final Integer access, final String name,
      final String desc, final String signature, final String[] exceptions) {
    return (isSynthetic(access));
  }

  private static boolean isSynthetic(final Integer access) {
    return (access & Opcodes.ACC_BRIDGE) == 0;
  }

}
