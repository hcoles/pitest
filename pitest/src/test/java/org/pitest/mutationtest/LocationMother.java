package org.pitest.mutationtest;

import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.Location;

public class LocationMother {
  
  public static Location aLocation() {
    return Location.location(ClassName.fromString("clazz"), MethodName.fromString("method"), "()I");
  }
  
}
