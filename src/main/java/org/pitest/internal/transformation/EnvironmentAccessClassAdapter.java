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
package org.pitest.internal.transformation;

import java.util.Set;
import java.util.TreeSet;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class EnvironmentAccessClassAdapter extends ClassAdapter {

  public EnvironmentAccessClassAdapter(final ClassVisitor arg0) {
    super(arg0);
  }

  @Override
  public MethodVisitor visitMethod(final int access, final String name,
      final String desc, final String signature, final String[] exceptions) {
    return new EnvironmentAccessMethodVisitor(this.cv.visitMethod(access, name,
        desc, signature, exceptions));
  }

}

class EnvironmentAccessMethodVisitor extends MethodAdapter {

  private final static Set<String> replacedMethods = new TreeSet<String>();

  static {
    replacedMethods.add("getProperty");
    replacedMethods.add("setProperty");
    replacedMethods.add("getProperties");
    replacedMethods.add("setProperties");
  }

  public EnvironmentAccessMethodVisitor(final MethodVisitor mv) {
    super(mv);
  }

  @Override
  public void visitMethodInsn(final int opcode, final String owner,
      final String name, final String desc) {
    if ((opcode == Opcodes.INVOKESTATIC) && owner.equals("java/lang/System")
        && shouldReplaceMethodCall(name)) {
      this.mv.visitMethodInsn(opcode, classToName(IsolatedSystem.class), name,
          desc);
    } else {
      this.mv.visitMethodInsn(opcode, owner, name, desc);
    }
  }

  private boolean shouldReplaceMethodCall(final String method) {
    return replacedMethods.contains(method);
  }

  private String classToName(final Class<?> clazz) {
    return clazz.getName().replace(".", "/");
  }

}
