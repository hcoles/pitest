package org.pitest.classinfo;

public class ClassInfoMother {

  public static ClassInfo make(ClassIdentifier id) {
    ClassInfoBuilder data = new ClassInfoBuilder();
    data.id = id;
    ClassInfo ci = new ClassInfo(null,null,data);
    return ci;
  }

  public static ClassInfo make(String name) {
    return make(new ClassIdentifier(1, new ClassName(name)));
  }
}
