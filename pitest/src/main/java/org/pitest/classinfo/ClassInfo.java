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
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.objectweb.asm.Opcodes;
import org.pitest.functional.FCollection;
import java.util.Optional;

public class ClassInfo {

  private final ClassIdentifier        id;

  private final int                    access;
  private final Set<Integer>           codeLines;
  private final ClassPointer           outerClass;
  private final ClassPointer           superClass;
  private final Collection<ClassName>  annotations;
  private final String                 sourceFile;
  private final Map<ClassName, Object> classAnnotationValues;

  public ClassInfo(final ClassPointer superClass,
      final ClassPointer outerClass, final ClassInfoBuilder builder) {
    this.superClass = superClass;
    this.outerClass = outerClass;
    this.id = builder.id;
    this.access = builder.access;
    this.codeLines = builder.codeLines;
    this.annotations = FCollection.map(builder.annotations,
        ClassName.stringToClassName());
    this.sourceFile = builder.sourceFile;
    this.classAnnotationValues = builder.classAnnotationValues;
  }

  public int getNumberOfCodeLines() {
    return this.codeLines.size();
  }

  public boolean isCodeLine(final int line) {
    return this.codeLines.contains(line);
  }

  public ClassIdentifier getId() {
    return this.id;
  }

  public ClassName getName() {
    return this.id.getName();
  }

  public boolean isInterface() {
    return (this.access & Opcodes.ACC_INTERFACE) != 0;
  }

  public boolean isAbstract() {
    return (this.access & Opcodes.ACC_ABSTRACT) != 0;
  }

  public boolean isSynthetic() {
    return (this.access & Opcodes.ACC_SYNTHETIC) != 0;
  }

  public boolean isTopLevelClass() {
    return !getOuterClass().isPresent();
  }

  public Optional<ClassInfo> getOuterClass() {
    return this.outerClass.fetch();
  }

  public Optional<ClassInfo> getSuperClass() {
    return getParent();
  }

  public String getSourceFileName() {
    return this.sourceFile;
  }

  public boolean hasAnnotation(final Class<? extends Annotation> annotation) {
    return hasAnnotation(ClassName.fromClass(annotation));
  }

  public boolean hasAnnotation(final ClassName annotation) {
    return this.annotations.contains(annotation);
  }

  public Object getClassAnnotationValue(final ClassName annotation) {
    return this.classAnnotationValues.get(annotation);
  }

  public boolean descendsFrom(final Class<?> clazz) {
    return descendsFrom(ClassName.fromClass(clazz));
  }

  public HierarchicalClassId getHierarchicalId() {
    return new HierarchicalClassId(this.id, getDeepHash());
  }

  public BigInteger getDeepHash() {
    BigInteger hash = getHash();
    final Optional<ClassInfo> parent = getParent();
    if (parent.isPresent()) {
      hash = hash.add(parent.get().getHash());
    }
    final Optional<ClassInfo> outer = getOuterClass();
    if (outer.isPresent()) {
      hash = hash.add(outer.get().getHash());
    }
    return hash;
  }

  public BigInteger getHash() {
    return BigInteger.valueOf(this.id.getHash());
  }

  private Optional<ClassInfo> getParent() {
    if (this.superClass == null) {
      return Optional.empty();
    }
    return this.superClass.fetch();
  }

  private boolean descendsFrom(final ClassName clazz) {

    if (!this.getSuperClass().isPresent()) {
      return false;
    }

    if (this.getSuperClass().get().getName().equals(clazz)) {
      return true;
    }

    return getSuperClass().get().descendsFrom(clazz);
  }

  public static Predicate<ClassInfo> matchIfAbstract() {
    return a -> a.isAbstract();
  }

  @Override
  public String toString() {
    return this.id.getName().asJavaName();
  }

  public static Function<ClassInfo, ClassName> toClassName() {
    return a -> a.getName();
  }

  public static Function<ClassInfo, HierarchicalClassId> toFullClassId() {
    return a -> a.getHierarchicalId();
  }
}
