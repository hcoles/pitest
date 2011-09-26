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

public class NullVisitor implements ClassVisitor {

  public static class NullAnnotationVisitor implements AnnotationVisitor {

    public void visit(final String arg0, final Object arg1) {
    }

    public AnnotationVisitor visitAnnotation(final String arg0,
        final String arg1) {
      return new NullAnnotationVisitor();
    }

    public AnnotationVisitor visitArray(final String arg0) {
      return new NullAnnotationVisitor();
    }

    public void visitEnd() {
    }

    public void visitEnum(final String arg0, final String arg1,
        final String arg2) {
    }

  }

  public static class NullMethodVisitor implements MethodVisitor {

    public AnnotationVisitor visitAnnotation(final String arg0,
        final boolean arg1) {
      return new NullAnnotationVisitor();
    }

    public AnnotationVisitor visitAnnotationDefault() {
      return new NullAnnotationVisitor();
    }

    public void visitAttribute(final Attribute arg0) {
    }

    public void visitCode() {
    }

    public void visitEnd() {
    }

    public void visitFieldInsn(final int arg0, final String arg1,
        final String arg2, final String arg3) {
    }

    public void visitFrame(final int arg0, final int arg1, final Object[] arg2,
        final int arg3, final Object[] arg4) {
    }

    public void visitIincInsn(final int arg0, final int arg1) {
    }

    public void visitInsn(final int arg0) {
    }

    public void visitIntInsn(final int arg0, final int arg1) {
    }

    public void visitJumpInsn(final int arg0, final Label arg1) {
    }

    public void visitLabel(final Label arg0) {
    }

    public void visitLdcInsn(final Object arg0) {
    }

    public void visitLineNumber(final int arg0, final Label arg1) {
    }

    public void visitLocalVariable(final String arg0, final String arg1,
        final String arg2, final Label arg3, final Label arg4, final int arg5) {
    }

    public void visitLookupSwitchInsn(final Label arg0, final int[] arg1,
        final Label[] arg2) {
    }

    public void visitMaxs(final int arg0, final int arg1) {
    }

    public void visitMethodInsn(final int arg0, final String arg1,
        final String arg2, final String arg3) {
    }

    public void visitMultiANewArrayInsn(final String arg0, final int arg1) {
    }

    public AnnotationVisitor visitParameterAnnotation(final int arg0,
        final String arg1, final boolean arg2) {
      return new NullAnnotationVisitor();
    }

    public void visitTableSwitchInsn(final int arg0, final int arg1,
        final Label arg2, final Label[] arg3) {
    }

    public void visitTryCatchBlock(final Label arg0, final Label arg1,
        final Label arg2, final String arg3) {
    }

    public void visitTypeInsn(final int arg0, final String arg1) {
    }

    public void visitVarInsn(final int arg0, final int arg1) {
    }

  };

  public void visit(final int arg0, final int arg1, final String arg2,
      final String arg3, final String arg4, final String[] arg5) {
  }

  public AnnotationVisitor visitAnnotation(final String arg0, final boolean arg1) {
    return new NullAnnotationVisitor();
  }

  public void visitAttribute(final Attribute arg0) {
  }

  public void visitEnd() {
  }

  public FieldVisitor visitField(final int arg0, final String arg1,
      final String arg2, final String arg3, final Object arg4) {
    return new FieldVisitor() {

      public AnnotationVisitor visitAnnotation(final String arg0,
          final boolean arg1) {
        return new NullAnnotationVisitor();
      }

      public void visitAttribute(final Attribute arg0) {
      }

      public void visitEnd() {
      }

    };
  }

  public void visitInnerClass(final String arg0, final String arg1,
      final String arg2, final int arg3) {
  }

  public MethodVisitor visitMethod(final int arg0, final String arg1,
      final String arg2, final String arg3, final String[] arg4) {

    return new NullMethodVisitor();
  }

  public void visitOuterClass(final String arg0, final String arg1,
      final String arg2) {
  }

  public void visitSource(final String arg0, final String arg1) {
  }

}
