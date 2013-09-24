package org.pitest.mutationtest;

import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MethodName;
import org.pitest.mutationtest.engine.MutationIdentifier;

public class LocationMother {
  
  public static Location aLocation() {
    return Location.location(ClassName.fromString("clazz"), MethodName.fromString("method"), "()I");
  }
  
  public static MutationIdentifier aMutationId() {
    return new MutationIdentifier(aLocation(),1,"mutator");
  }
}
