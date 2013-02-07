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
    final ClassInfo ci = new ClassInfo(parent, new DefaultClassPointer(null),
        data);
    return ci;
  }

  public static ClassInfo make(final ClassName name) {
    return make(new ClassIdentifier(1, name));
  }

  public static ClassInfo make(final String name) {
    return make(new ClassIdentifier(1, new ClassName(name)));
  }
}
