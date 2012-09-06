package org.pitest.classinfo;

public class ClassInfoMother {

  public static ClassInfo make(ClassIdentifier id) {
    return make(id,(ClassPointer)null);
  }

  public static ClassInfo make(ClassIdentifier id, ClassInfo parent) {
    return make(id,new DefaultClassPointer(parent));
  }
  
  public static ClassInfo make(ClassIdentifier id, ClassPointer parent) {
    ClassInfoBuilder data = new ClassInfoBuilder();
    data.id = id;
    ClassInfo ci = new ClassInfo(parent,null,data);
    return ci;
  }
  
  public static ClassInfo make(ClassName name) {
    return make(new ClassIdentifier(1, name));
  }
  
  public static ClassInfo make(String name) {
    return make(new ClassIdentifier(1, new ClassName(name)));
  }
}
