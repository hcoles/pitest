package org.pitest.classinfo;

import org.pitest.functional.F;
import org.pitest.functional.Option;

public class NameToClassInfo implements F<ClassName, Option<ClassInfo>> {

  private final ClassInfoSource repository;

  public NameToClassInfo(final ClassInfoSource repository) {
    this.repository = repository;
  }

  public Option<ClassInfo> apply(final ClassName a) {
    System.out.println(a);
    final Option<ClassInfo> o = this.repository.fetchClass(a);
    System.out.println(o);
    return o;
  }

}