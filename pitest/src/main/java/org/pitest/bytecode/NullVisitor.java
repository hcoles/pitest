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
package org.pitest.bytecode;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class NullVisitor extends ClassVisitor {

  public NullVisitor() {
    super(Opcodes.ASM6);
  }

  public static class NullAnnotationVisitor extends AnnotationVisitor {

    NullAnnotationVisitor() {
      super(Opcodes.ASM6);
    }

    @Override
    public void visit(final String arg0, final Object arg1) {
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String arg0,
        final String arg1) {
      return new NullAnnotationVisitor();
    }

    @Override
    public AnnotationVisitor visitArray(final String arg0) {
      return new NullAnnotationVisitor();
    }

    @Override
    public void visitEnd() {
    }

    @Override
    public void visitEnum(final String arg0, final String arg1,
        final String arg2) {
    }

  }

  public static class NullMethodVisitor extends MethodVisitor {

    NullMethodVisitor() {
      super(Opcodes.ASM6);
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String arg0,
        final boolean arg1) {
      return new NullAnnotationVisitor();
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
      return new NullAnnotationVisitor();
    }

    @Override
    public void visitAttribute(final Attribute arg0) {
    }

    @Override
    public void visitCode() {
    }

    @Override
    public void visitEnd() {
    }

    @Override
    public void visitFieldInsn(final int arg0, final String arg1,
        final String arg2, final String arg3) {
    }

    @Override
    public void visitFrame(final int arg0, final int arg1, final Object[] arg2,
        final int arg3, final Object[] arg4) {
    }

    @Override
    public void visitIincInsn(final int arg0, final int arg1) {
    }

    @Override
    public void visitInsn(final int arg0) {
    }

    @Override
    public void visitIntInsn(final int arg0, final int arg1) {
    }

    @Override
    public void visitJumpInsn(final int arg0, final Label arg1) {
    }

    @Override
    public void visitLabel(final Label arg0) {
    }

    @Override
    public void visitLdcInsn(final Object arg0) {
    }

    @Override
    public void visitLineNumber(final int arg0, final Label arg1) {
    }

    @Override
    public void visitLocalVariable(final String arg0, final String arg1,
        final String arg2, final Label arg3, final Label arg4, final int arg5) {
    }

    @Override
    public void visitLookupSwitchInsn(final Label arg0, final int[] arg1,
        final Label[] arg2) {
    }

    @Override
    public void visitMaxs(final int arg0, final int arg1) {
    }

    @Override
    public void visitMethodInsn(final int arg0, final String arg1,
        final String arg2, final String arg3) {
    }

    @Override
    public void visitMultiANewArrayInsn(final String arg0, final int arg1) {
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(final int arg0,
        final String arg1, final boolean arg2) {
      return new NullAnnotationVisitor();
    }

    @Override
    public void visitTableSwitchInsn(final int arg0, final int arg1,
        final Label arg2, final Label... labels) {
    }

    @Override
    public void visitTryCatchBlock(final Label arg0, final Label arg1,
        final Label arg2, final String arg3) {
    }

    @Override
    public void visitTypeInsn(final int arg0, final String arg1) {
    }

    @Override
    public void visitVarInsn(final int arg0, final int arg1) {
    }

  };

  @Override
  public void visit(final int arg0, final int arg1, final String arg2,
      final String arg3, final String arg4, final String[] arg5) {
  }

  @Override
  public AnnotationVisitor visitAnnotation(final String arg0, final boolean arg1) {
    return new NullAnnotationVisitor();
  }

  @Override
  public void visitAttribute(final Attribute arg0) {
  }

  @Override
  public void visitEnd() {
  }

  @Override
  public FieldVisitor visitField(final int arg0, final String arg1,
      final String arg2, final String arg3, final Object arg4) {
    return new FieldVisitor(Opcodes.ASM6) {

      @Override
      public AnnotationVisitor visitAnnotation(final String arg0,
          final boolean arg1) {
        return new NullAnnotationVisitor();
      }

      @Override
      public void visitAttribute(final Attribute arg0) {
      }

      @Override
      public void visitEnd() {
      }

    };
  }

  @Override
  public void visitInnerClass(final String arg0, final String arg1,
      final String arg2, final int arg3) {
  }

  @Override
  public MethodVisitor visitMethod(final int arg0, final String arg1,
      final String arg2, final String arg3, final String[] arg4) {

    return new NullMethodVisitor();
  }

  @Override
  public void visitOuterClass(final String arg0, final String arg1,
      final String arg2) {
  }

  @Override
  public void visitSource(final String arg0, final String arg1) {
  }

}
