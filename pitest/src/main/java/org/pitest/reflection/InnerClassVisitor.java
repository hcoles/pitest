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
package org.pitest.reflection;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.pitest.bytecode.NullVisitor;
import org.pitest.functional.SideEffect1;

public class InnerClassVisitor extends ClassAdapter {

  private static class NameCollector implements SideEffect1<String> {

    List<String> innerClasses = new ArrayList<String>();

    public void apply(final String a) {
      this.innerClasses.add(a);

    }

  }

  private final SideEffect1<String> collector;

  public InnerClassVisitor(final ClassVisitor cv,
      final SideEffect1<String> collector) {
    super(cv);
    this.collector = collector;
  }

  @Override
  public void visitInnerClass(final String name, final String outerName,
      final String innerName, final int access) {
    this.collector.apply(name);
  }

  public final static List<String> getInnerClasses(final byte[] bytes) {
    final ClassReader reader = new ClassReader(bytes);
    final NullVisitor nv = new NullVisitor();

    final NameCollector nameCollector = new NameCollector();
    reader.accept(new InnerClassVisitor(nv, nameCollector), 0);

    return nameCollector.innerClasses;
  }

}
