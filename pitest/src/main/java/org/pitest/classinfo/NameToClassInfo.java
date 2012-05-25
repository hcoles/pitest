package org.pitest.classinfo;

import org.pitest.functional.F;
import org.pitest.functional.Option;

public class NameToClassInfo implements F<String, Option<ClassInfo>> {

  private final Repository repository;

  public NameToClassInfo(final Repository repository) {
    this.repository = repository;
  }

  public Option<ClassInfo> apply(final String a) {
    return this.repository.fetchClass(a);
  }

}