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

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;

public class ClassInfo {

  private final int                   access;
  private final ClassName             name;
  private final Set<Integer>          codeLines;
  private final ClassPointer          outerClass;
  private final ClassPointer          superClass;
  private final Collection<ClassName> annotations;
  private final String                sourceFile;

  public ClassInfo(final ClassPointer superClass,
      final ClassPointer outerClass, final ClassInfoBuilder builder) {
    this.superClass = superClass;
    this.outerClass = outerClass;
    this.name = builder.name;
    this.access = builder.access;
    this.codeLines = builder.codeLines;
    this.annotations = FCollection.map(builder.annotations,
        ClassName.stringToClassName());
    this.sourceFile = builder.sourceFile;
  }

  public int getNumberOfCodeLines() {
    return this.codeLines.size();
  }

  public boolean isCodeLine(final int line) {
    return this.codeLines.contains(line);
  }

  public ClassName getName() {
    return this.name;
  }

  public boolean isInterface() {
    return (this.access & Opcodes.ACC_INTERFACE) != 0;
  }

  public boolean isAbstract() {
    return (this.access & Opcodes.ACC_ABSTRACT) != 0;
  }

  public boolean isTopLevelClass() {
    return getOuterClass().hasNone();
  }

  public Option<ClassInfo> getOuterClass() {
    return this.outerClass.fetch();
  }

  public Option<ClassInfo> getSuperClass() {
    return this.superClass.fetch();
  }

  public String getSourceFileName() {
    return this.sourceFile;
  }

  public boolean hasAnnotation(final Class<? extends Annotation> annotation) {
    return hasAnnotation(new ClassName(annotation));
  }

  public boolean hasAnnotation(final ClassName annotation) {
    return this.annotations.contains(annotation);
  }

  public boolean descendsFrom(final Class<?> clazz) {
    return descendsFrom(new ClassName(clazz.getName()));
  }

  private boolean descendsFrom(final ClassName clazz) {

    if (this.getSuperClass().hasNone()) {
      return false;
    }

    if (this.getSuperClass().value().getName().equals(clazz)) {
      return true;
    }

    return getSuperClass().value().descendsFrom(clazz);
  }

  public static F<ClassInfo, Boolean> matchIfAbstract() {
    return new F<ClassInfo, Boolean>() {
      public Boolean apply(final ClassInfo a) {
        return a.isAbstract();
      }

    };
  }

  public static F<ClassInfo, Boolean> matchIfInterface() {
    return new F<ClassInfo, Boolean>() {
      public Boolean apply(final ClassInfo a) {
        return a.isInterface();
      }

    };
  }

  public static F<ClassInfo, Boolean> matchIfTopLevelClass() {
    return new F<ClassInfo, Boolean>() {
      public Boolean apply(final ClassInfo a) {
        return a.isTopLevelClass();
      }

    };
  }


  @Override
  public String toString() {
    return this.name.asJavaName();
  }

  public static F<ClassInfo, ClassName> toClassName() {
    return new F<ClassInfo, ClassName>() {
      public ClassName apply(final ClassInfo a) {
        return a.getName();
      }

    };
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ClassInfo other = (ClassInfo) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }

  
  
}
