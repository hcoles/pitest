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

import java.util.Optional;

interface ClassPointer {
  Optional<ClassInfo> fetch();
}

class DefaultClassPointer implements ClassPointer {

  private final ClassInfo clazz;

  DefaultClassPointer(final ClassInfo clazz) {
    this.clazz = clazz;
  }

  @Override
  public Optional<ClassInfo> fetch() {
    return Optional.ofNullable(this.clazz);
  }

}

class DeferredClassPointer implements ClassPointer {
  private final Repository repository;
  private final ClassName  name;

  DeferredClassPointer(final Repository repository, final ClassName name) {
    this.repository = repository;
    this.name = name;
  }

  @Override
  public Optional<ClassInfo> fetch() {
    return this.repository.fetchClass(this.name);
  }

}
