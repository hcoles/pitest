package org.pitest.classinfo;

import org.pitest.functional.Option;

class DeferredClassPointer implements ClassPointer {
  private final Repository repository;
  private final ClassName  name;

  DeferredClassPointer(Repository repository, ClassName name) {
    this.repository = repository;
    this.name = name;
  }

  public Option<ClassInfo> fetch() {
    return repository.fetchClass(name);
  }
}
