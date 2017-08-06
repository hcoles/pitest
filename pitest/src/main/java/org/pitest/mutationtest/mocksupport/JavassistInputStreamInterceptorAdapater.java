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
package org.pitest.mutationtest.mocksupport;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.functional.F;

public class JavassistInputStreamInterceptorAdapater extends ClassVisitor {
  
  private final String interceptorClass;

  public JavassistInputStreamInterceptorAdapater(final ClassVisitor arg0, Class<?> interceptor) {
    super(Opcodes.ASM6, arg0);
    this.interceptorClass = classToName(interceptor);
  }

  public static F<ClassWriter, ClassVisitor> inputStreamAdapterSupplier(final Class<?> interceptor) {
    return new F<ClassWriter, ClassVisitor>() {
      @Override
      public ClassVisitor apply(ClassWriter a) {
        return new JavassistInputStreamInterceptorAdapater(a, interceptor);
      }
    };
  }
  
  
  @Override
  public MethodVisitor visitMethod(final int access, final String name,
      final String desc, final String signature, final String[] exceptions) {
    return new JavassistInputStreamInterceptorMethodVisitor(
        this.cv.visitMethod(access, name, desc, signature, exceptions), interceptorClass);
  }
  
  private static String classToName(final Class<?> clazz) {
    return clazz.getName().replace(".", "/");
  }

}

class JavassistInputStreamInterceptorMethodVisitor extends MethodVisitor {

  private final String interceptorClass;

  JavassistInputStreamInterceptorMethodVisitor(final MethodVisitor mv, String interceptor) {
    super(Opcodes.ASM6, mv);
    interceptorClass = interceptor;
  }

  @Override
  public void visitMethodInsn(final int opcode, final String owner,
      final String name, final String desc, boolean itf) {
    if ((opcode == Opcodes.INVOKEINTERFACE)
        && owner.equals("javassist/ClassPath") && name.equals("openClassfile")) {
      this.mv.visitMethodInsn(Opcodes.INVOKESTATIC, interceptorClass, name,
          "(Ljava/lang/Object;Ljava/lang/String;)Ljava/io/InputStream;", false);
    } else {
      this.mv.visitMethodInsn(opcode, owner, name, desc, itf);
    }

  }

}
