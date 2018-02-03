package org.pitest.classinfo;

import java.util.function.Function;

import org.pitest.functional.Option;

public class NameToClassInfo implements Function<ClassName, Option<ClassInfo>> {

  private final ClassInfoSource repository;

  public NameToClassInfo(final ClassInfoSource repository) {
    this.repository = repository;
  }

  @Override
  public Option<ClassInfo> apply(final ClassName a) {
    return this.repository.fetchClass(a);
  }

}