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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java.util.Optional;

public class Repository implements ClassInfoSource {

  private final HashFunction              hashFunction;
  private final Map<ClassName, ClassInfo> knownClasses   = new HashMap<>();
  private final Set<ClassName>            unknownClasses = new HashSet<>();
  private final ClassByteArraySource      source;

  public Repository(final ClassByteArraySource source) {
    this(source, new AddlerHash());
  }

  Repository(final ClassByteArraySource source, final HashFunction hashFunction) {
    this.source = source;
    this.hashFunction = hashFunction;
  }

  public boolean hasClass(final ClassName name) {
    return this.knownClasses.containsKey(name) || querySource(name).isPresent();
  }

  public Optional<ClassInfo> fetchClass(final Class<?> clazz) { // NO_UCD (test
    // only)
    return fetchClass(clazz.getName());
  }

  private Optional<ClassInfo> fetchClass(final String name) {
    return fetchClass(ClassName.fromString(name));
  }

  @Override
  public Optional<ClassInfo> fetchClass(final ClassName name) {
    final ClassInfo info = this.knownClasses.get(name);
    if (info != null) {
      return Optional.ofNullable(info);
    }

    final Optional<ClassInfo> maybeInfo = nameToClassInfo(name);
    if (maybeInfo.isPresent()) {
      this.knownClasses.put(name, maybeInfo.get());
    }
    return maybeInfo;
  }

  private Optional<ClassInfo> nameToClassInfo(final ClassName name) {
    final Optional<byte[]> bytes = querySource(name);
    if (bytes.isPresent()) {
      final ClassInfoBuilder classData = ClassInfoVisitor.getClassInfo(name,
          bytes.get(), this.hashFunction.hash(bytes.get()));
      return contructClassInfo(classData);
    } else {
      return Optional.empty();
    }
  }

  public Optional<byte[]> querySource(final ClassName name) {
    if (this.unknownClasses.contains(name)) {
      return Optional.empty();
    }
    final Optional<byte[]> option = this.source.getBytes(name.asJavaName());
    if (option.isPresent()) {
      return option;
    }

    this.unknownClasses.add(name);
    return option;
  }

  private Optional<ClassInfo> contructClassInfo(final ClassInfoBuilder classData) {
    return Optional.ofNullable(new ClassInfo(resolveClass(classData.superClass),
        resolveClass(classData.outerClass), classData));
  }

  private ClassPointer resolveClass(final String clazz) {
    if (clazz == null) {
      return new DefaultClassPointer(null);
    } else {
      final ClassInfo alreadyResolved = this.knownClasses.get(ClassName
          .fromString(clazz));
      if (alreadyResolved != null) {
        return new DefaultClassPointer(alreadyResolved);
      } else {
        return new DeferredClassPointer(this, ClassName.fromString(clazz));
      }
    }
  }

}
