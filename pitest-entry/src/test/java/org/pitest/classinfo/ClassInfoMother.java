package org.pitest.classinfo;


public class ClassInfoMother {

  public static ClassInfo make(final ClassIdentifier id) {
    return make(id, new DefaultClassPointer(null));
  }

  public static ClassInfo make(final ClassIdentifier id, final ClassInfo parent) {
    return make(id, new DefaultClassPointer(parent));
  }

  public static ClassInfo make(final ClassIdentifier id,
      final ClassPointer parent) {
    final ClassInfoBuilder data = new ClassInfoBuilder();
    data.id = id;
    return new ClassInfo(parent, new DefaultClassPointer(null), data);
  }

  public static ClassInfo make(final ClassName name) {
    final DefaultClassPointer parent = new DefaultClassPointer(null);
    final ClassInfoBuilder data = new ClassInfoBuilder();
    data.id = new ClassIdentifier(1, name);
    return new ClassInfo(parent, new DefaultClassPointer(null), data);
  }

  public static ClassInfo make(final String name) {
    return make(new ClassIdentifier(1, ClassName.fromString(name)));
  }
}
