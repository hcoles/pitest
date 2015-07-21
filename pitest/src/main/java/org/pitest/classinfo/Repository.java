/*
 * Copyright 2011 Henry Coles
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

import org.pitest.functional.Option;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Repository implements ClassInfoSource {

  private final Map<ClassName, ClassInfo> knownClasses   = new HashMap<ClassName, ClassInfo>();
  private final Set<ClassName>            unknownClasses = new HashSet<ClassName>();

  private final ClassByteArraySource source;
  private final HashFunction         hashFunction;

  public Repository(ClassByteArraySource source) {
    this(source, new AddlerHash());
  }

  Repository(ClassByteArraySource source, HashFunction hashFunction) {
    this.source = source;
    this.hashFunction = hashFunction;
  }

  public boolean hasClass(ClassName name) {
    return knownClasses.containsKey(name) || querySource(name).hasSome();
  }

  public Option<ClassInfo> fetchClass(Class<?> clazz) { // NO_UCD (test only)
    ClassName name = new ClassName(clazz.getName());
    return fetchClass(name);
  }

  public Option<ClassInfo> fetchClass(ClassName name) {
    ClassInfo info = knownClasses.get(name);
    if (info != null) {
      return Option.some(info);
    }

    Option<ClassInfo> maybeInfo = nameToClassInfo(name);
    if (maybeInfo.hasSome()) {
      knownClasses.put(name, maybeInfo.value());
    }
    return maybeInfo;
  }

  private Option<ClassInfo> nameToClassInfo(ClassName name) {
    Option<byte[]> bytes = querySource(name);
    if (bytes.hasSome()) {
      long hash = hashFunction.hash(bytes.value());
      ClassInfoBuilder classData = ClassInfoVisitor.getClassInfo(name, bytes.value(), hash);
      return contructClassInfo(classData);
    } else {
      return Option.none();
    }
  }

  public Option<byte[]> querySource(ClassName name) {
    if (unknownClasses.contains(name)) {
      return Option.none();
    }
    Option<byte[]> option = source.getBytes(name.asJavaName());
    if (option.hasSome()) {
      return option;
    }

    unknownClasses.add(name);
    return option;
  }

  private Option<ClassInfo> contructClassInfo(ClassInfoBuilder classData) {
    return Option.some(new ClassInfo(resolveClass(classData.superClass),
        resolveClass(classData.outerClass), classData));
  }

  private ClassPointer resolveClass(String clazz) {
    if (clazz == null) {
      return new DefaultClassPointer(null);
    } else {
      ClassInfo alreadyResolved = knownClasses.get(ClassName.fromString(clazz));
      if (alreadyResolved != null) {
        return new DefaultClassPointer(alreadyResolved);
      } else {
        return new DeferredClassPointer(this, ClassName.fromString(clazz));
      }
    }
  }

}
