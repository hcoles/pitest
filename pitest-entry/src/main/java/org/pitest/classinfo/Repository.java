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

import org.pitest.functional.Option;

public class Repository implements ClassInfoSource {

  private final HashFunction              hashFunction;
  private final Map<ClassName, ClassInfo> knownClasses   = new HashMap<ClassName, ClassInfo>();
  private final Set<ClassName>            unknownClasses = new HashSet<ClassName>();
  private final ClassByteArraySource      source;

  public Repository(final ClassByteArraySource source) {
    this(source, new AddlerHash());
  }

  Repository(final ClassByteArraySource source, final HashFunction hashFunction) {
    this.source = source;
    this.hashFunction = hashFunction;
  }

  public boolean hasClass(final ClassName name) {
    return this.knownClasses.containsKey(name) || querySource(name).hasSome();
  }

  public Option<ClassInfo> fetchClass(final Class<?> clazz) { // NO_UCD (test
    // only)
    return fetchClass(clazz.getName());
  }

  private Option<ClassInfo> fetchClass(final String name) {
    return fetchClass(ClassName.fromString(name));
  }

  @Override
  public Option<ClassInfo> fetchClass(final ClassName name) {
    final ClassInfo info = this.knownClasses.get(name);
    if (info != null) {
      return Option.some(info);
    }

    final Option<ClassInfo> maybeInfo = nameToClassInfo(name);
    if (maybeInfo.hasSome()) {
      this.knownClasses.put(name, maybeInfo.value());
    }
    return maybeInfo;
  }

  private Option<ClassInfo> nameToClassInfo(final ClassName name) {
    final Option<byte[]> bytes = querySource(name);
    if (bytes.hasSome()) {
      final ClassInfoBuilder classData = ClassInfoVisitor.getClassInfo(name,
          bytes.value(), this.hashFunction.hash(bytes.value()));
      return contructClassInfo(classData);
    } else {
      return Option.none();
    }
  }

  public Option<byte[]> querySource(final ClassName name) {
    if (this.unknownClasses.contains(name)) {
      return Option.none();
    }
    final Option<byte[]> option = this.source.getBytes(name.asJavaName());
    if (option.hasSome()) {
      return option;
    }

    this.unknownClasses.add(name);
    return option;
  }

  private Option<ClassInfo> contructClassInfo(final ClassInfoBuilder classData) {
    return Option.some(new ClassInfo(resolveClass(classData.superClass),
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
