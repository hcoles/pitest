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

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.pitest.bytecode.ASMVersion;
import org.pitest.functional.F5;

public final class ClassInfoVisitor extends ClassVisitor {

  private final F5<Integer, String, String, String, String[], Boolean> filter = BridgeMethodFilter.INSTANCE;
  private final ClassInfoBuilder classInfo;

  private ClassInfoVisitor(final ClassInfoBuilder classInfo,
      final ClassVisitor writer) {
    super(ASMVersion.ASM_VERSION, writer);
    this.classInfo = classInfo;
  }

  public static ClassInfoBuilder getClassInfo(final ClassName name,
      final byte[] bytes, final long hash) {
    final ClassReader reader = new ClassReader(bytes);
    final ClassInfoBuilder info = new ClassInfoBuilder();
    info.id = new ClassIdentifier(hash, name);
    reader.accept(new ClassInfoVisitor(info, null), 0);
    return info;
  }

  @Override
  public void visitSource(final String source, final String debug) {
    super.visitSource(source, debug);
  }

  @Override
  public void visit(final int version, final int access, final String name,
      final String signature, final String superName, final String[] interfaces) {
    super.visit(version, access, name, signature, superName, interfaces);
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
  public MethodVisitor visitMethod(final int access, final String name,
                                         final String desc, final String signature, final String[] exceptions) {
    if (shouldInstrument(access, name, desc, signature, exceptions)) {
      return visitMethodIfRequired(access, name, desc, signature, exceptions);
    } else {
      return super.visitMethod(access, name, desc, signature, exceptions);
    }
  }

  public MethodVisitor visitMethodIfRequired(final int access,
      final String name, final String desc, final String signature,
      final String[] exceptions) {

    return new InfoMethodVisitor(this.classInfo);

  }

  private boolean shouldInstrument(final int access, final String name,
                                   final String desc, final String signature, final String[] exceptions) {
    return this.filter.apply(access, name, desc, signature, exceptions);
  }

}

class InfoMethodVisitor extends MethodVisitor {
  private final ClassInfoBuilder classInfo;

  InfoMethodVisitor(final ClassInfoBuilder classInfo) {
    super(ASMVersion.ASM_VERSION, null);
    this.classInfo = classInfo;
  }

}
