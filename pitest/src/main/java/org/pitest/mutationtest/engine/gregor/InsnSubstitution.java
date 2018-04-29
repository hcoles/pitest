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
package org.pitest.mutationtest.engine.gregor;
import org.objectweb.asm.MethodVisitor;

public class InsnSubstitution implements ZeroOperandMutation  {

  private final int    replacementOpcode;
  private final int    replacementOpcode1;
  private final int    replacementOpcode2;

  private final String message;

  public InsnSubstitution(final int replacementOpcode, final String message) {
    super();
    this.replacementOpcode = replacementOpcode;
    this.replacementOpcode1 = -1;
    this.replacementOpcode2 = -1;

    this.message = message;
  }
  public InsnSubstitution(final int replacementOpcode1, final int replacementOpcode, final String message) {
    this.replacementOpcode1 = replacementOpcode1;
    this.replacementOpcode = replacementOpcode;
    this.replacementOpcode2 = -1;

    this.message = message;
  }


  public InsnSubstitution(final int replacementOpcode2, final int replacementOpcode1, final int replacementOpcode, final String message) {
    this.replacementOpcode2 = replacementOpcode2;
    this.replacementOpcode1 = replacementOpcode1;
    this.replacementOpcode = replacementOpcode;

    this.message = message;
  }

  @Override
  public void apply(final int opCode, final MethodVisitor mv) {

    if (replacementOpcode2 != -1) {
      mv.visitInsn(this.replacementOpcode2);
    }


    if (replacementOpcode1 != -1) {
      mv.visitInsn(this.replacementOpcode1);
    }

    mv.visitInsn(this.replacementOpcode);
  }

  @Override
  public String decribe(final int opCode, final MethodInfo methodInfo) {
    return this.message;
  }

}
