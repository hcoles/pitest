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

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.pitest.bytecode.NullVisitor;

public final class ClassInfoVisitor extends MethodFilteringAdapter {

  private final ClassInfoBuilder classInfo;

  private ClassInfoVisitor(final ClassInfoBuilder classInfo,
      final ClassVisitor writer) {
    super(writer, BridgeMethodFilter.INSTANCE);
    this.classInfo = classInfo;
  }

  public static ClassInfoBuilder getClassInfo(final ClassName name,
      final byte[] bytes, final long hash) {
    final ClassReader reader = new ClassReader(bytes);
    final ClassVisitor writer = new NullVisitor();

    final ClassInfoBuilder info = new ClassInfoBuilder();
    info.id = new ClassIdentifier(hash, name);
    reader.accept(new ClassInfoVisitor(info, writer), 0);
    return info;
  }

  @Override
  public void visitSource(final String source, final String debug) {
    super.visitSource(source, debug);
    this.classInfo.sourceFile = source;
  }

  @Override
  public void visit(final int version, final int access, final String name,
      final String signature, final String superName, final String[] interfaces) {
    super.visit(version, access, name, signature, superName, interfaces);
    this.classInfo.access = access;
    this.classInfo.superClass = superName;
  }

  @Override
  public void visitOuterClass(final String owner, final String name,
      final String desc) {
    super.visitOuterClass(owner, name, desc);
    this.classInfo.outerClass = owner;
  }

  @Override
  public void visitInnerClass(final String name, final String outerName,
      final String innerName, final int access) {
    super.visitInnerClass(name, outerName, innerName, access);
    if ((outerName != null)
        && this.classInfo.id.getName().equals(ClassName.fromString(name))) {
      this.classInfo.outerClass = outerName;
    }
  }

  @Override
  public AnnotationVisitor visitAnnotation(final String desc,
      final boolean visible) {
    final String type = desc.substring(1, desc.length() - 1);
    this.classInfo.registerAnnotation(type);
    return new ClassAnnotationValueVisitor(this.classInfo, ClassName.fromString(type));
  }

  @Override
  public MethodVisitor visitMethodIfRequired(final int access,
      final String name, final String desc, final String signature,
      final String[] exceptions, final MethodVisitor methodVisitor) {

    return new InfoMethodVisitor(this.classInfo, methodVisitor);

  }

  private static class ClassAnnotationValueVisitor extends AnnotationVisitor {
    private final ClassInfoBuilder classInfo;
    private final ClassName        annotation;

    ClassAnnotationValueVisitor(ClassInfoBuilder classInfo,
        ClassName annotation) {
      super(Opcodes.ASM6, null);
      this.classInfo = classInfo;
      this.annotation = annotation;
    }

    @Override
    public void visit(String name, Object value) {
      if (name.equals("value")) {
        this.classInfo.registerClassAnnotationValue(this.annotation,
            simplify(value));
      }
      super.visit(name, value);
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
      if (name.equals("value")) {
        final List<Object> arrayValue = new ArrayList<Object>();

        return new AnnotationVisitor(Opcodes.ASM6, null) {
          @Override
          public void visit(String name, Object value) {
            arrayValue.add(simplify(value));
            super.visit(name, value);
          }

          @Override
          public void visitEnd() {
            ClassAnnotationValueVisitor.this.classInfo
                .registerClassAnnotationValue(
                    ClassAnnotationValueVisitor.this.annotation,
                    arrayValue.toArray());
          }
        };
      }
      return super.visitArray(name);
    }

    private static Object simplify(Object value) {
      Object newValue = value;
      if (value instanceof Type) {
        newValue = ((Type) value).getClassName();
      }
      return newValue;
    }
  }
}

class InfoMethodVisitor extends MethodVisitor {
  private final ClassInfoBuilder classInfo;

  InfoMethodVisitor(final ClassInfoBuilder classInfo,
      final MethodVisitor writer) {
    super(Opcodes.ASM6, writer);
    this.classInfo = classInfo;
  }

  @Override
  public void visitLineNumber(final int line, final Label start) {

    this.classInfo.registerCodeLine(line);

  }

  @Override
  public AnnotationVisitor visitAnnotation(final String desc,
      final boolean visible) {
    final String type = desc.substring(1, desc.length() - 1);
    this.classInfo.registerAnnotation(type);
    return super.visitAnnotation(desc, visible);
  }

}
