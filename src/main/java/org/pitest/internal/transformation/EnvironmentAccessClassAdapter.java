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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.internal.isolation.IsolatedBoolean;
import org.pitest.internal.isolation.IsolatedInteger;
import org.pitest.internal.isolation.IsolatedLong;
import org.pitest.internal.isolation.IsolatedSystem;

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

  private static class ReplacementClass {
    ReplacementClass(final String name, final Set<String> replacedMethods) {
      this.name = name;
      this.replacedMethods = replacedMethods;
    }

    public String      name;
    public Set<String> replacedMethods;
  }

  private final static Map<String, ReplacementClass> replacements = new HashMap<String, ReplacementClass>();

  static {
    replaceCalls(System.class, IsolatedSystem.class, "getProperty",
        "setProperty", "getProperties", "setProperties");
    replaceCalls(Boolean.class, IsolatedBoolean.class, "getBoolean");
    replaceCalls(Long.class, IsolatedLong.class, "getLong");
    replaceCalls(Integer.class, IsolatedInteger.class, "getInteger");
  }

  private static void replaceCalls(final Class<?> oldClass,
      final Class<?> newClass, final String... replacedMethodNames) {
    final Set<String> replacedMethods = new TreeSet<String>();
    replacedMethods.addAll(Arrays.asList(replacedMethodNames));
    replacements.put(classToName(oldClass), new ReplacementClass(
        classToName(newClass), replacedMethods));
  }

  public EnvironmentAccessMethodVisitor(final MethodVisitor mv) {
    super(mv);
  }

  @Override
  public void visitMethodInsn(final int opcode, final String owner,
      final String name, final String desc) {
    if ((opcode == Opcodes.INVOKESTATIC) && replacements.containsKey(owner)) {
      final ReplacementClass replacement = replacements.get(owner);
      if (replacement.replacedMethods.contains(name)) {
        this.mv.visitMethodInsn(opcode, replacement.name, name, desc);
      } else {
        this.mv.visitMethodInsn(opcode, owner, name, desc);
      }

    } else {
      this.mv.visitMethodInsn(opcode, owner, name, desc);
    }
  }

  private static String classToName(final Class<?> clazz) {
    return clazz.getName().replace(".", "/");
  }

}
