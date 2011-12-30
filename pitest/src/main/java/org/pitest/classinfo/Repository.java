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
import java.util.Map;

import org.pitest.functional.Option;
import org.pitest.internal.ClassByteArraySource;

public class Repository {

  private final Map<ClassName, ClassInfo> knownClasses = new HashMap<ClassName, ClassInfo>();
  private final ClassByteArraySource      source;

  public Repository(final ClassByteArraySource source) {
    this.source = source;
  }

  public boolean hasClass(final ClassName name) {
    return this.knownClasses.containsKey(name)
        || this.source.apply(name.asJavaName()).hasSome();
  }

  public Option<ClassInfo> fetchClass(final Class<?> clazz) {
    return fetchClass(clazz.getName());
  }

  public Option<ClassInfo> fetchClass(final String name) {
    return fetchClass(new ClassName(name));
  }

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
    final Option<byte[]> bytes = this.source.apply(name.asJavaName());
    if (bytes.hasSome()) {
      final ClassInfoBuilder classData = ClassInfoVisitor.getClassInfo(name,
          bytes.value());
      return contructClassInfo(classData);
    } else {
      return Option.none();
    }
  }

  private Option<ClassInfo> contructClassInfo(final ClassInfoBuilder classData) {
    return Option.some(new ClassInfo(resolveClass(classData.superClass),
        resolveClass(classData.outerClass), classData));
  }

  private ClassPointer resolveClass(final String clazz) {
    if (clazz == null) {
      return new DefaultClassPointer(null);
    } else {
      final ClassInfo alreadyResolved = this.knownClasses.get(clazz);
      if (alreadyResolved != null) {
        return new DefaultClassPointer(alreadyResolved);
      } else {
        return new DeferredClassPointer(this, clazz);
      }
    }
  }

}
