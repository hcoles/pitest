package org.pitest.classinfo;

import java.util.function.Function;

import java.util.Optional;

public class NameToClassInfo implements Function<ClassName, Optional<ClassInfo>> {

  private final ClassInfoSource repository;

  public NameToClassInfo(final ClassInfoSource repository) {
    this.repository = repository;
  }

  @Override
  public Optional<ClassInfo> apply(final ClassName a) {
    return this.repository.fetchClass(a);
  }

}