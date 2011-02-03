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
package org.pitest.mutationtest.loopbreak;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class LoopBreakClassAdapter extends ClassAdapter {

  public LoopBreakClassAdapter(final ClassVisitor arg0) {
    super(arg0);
  }

  @Override
  public MethodVisitor visitMethod(final int access, final String name,
      final String desc, final String signature, final String[] exceptions) {
    return new LoopBreakMethodVisitor(this.cv.visitMethod(access, name, desc,
        signature, exceptions));
  }

}

class LoopBreakMethodVisitor extends MethodAdapter {

  public LoopBreakMethodVisitor(final MethodVisitor mv) {
    super(mv);
  }

  @Override
  public void visitJumpInsn(final int opcode, final Label label) {
    this.mv.visitMethodInsn(Opcodes.INVOKESTATIC,
        classToName(PerProcessTimelimitCheck.class),
        "breakIfTimelimitExceeded", "()V");
    this.mv.visitJumpInsn(opcode, label);
  }

  private String classToName(final Class<?> clazz) {
    return clazz.getName().replace(".", "/");
  }

}
