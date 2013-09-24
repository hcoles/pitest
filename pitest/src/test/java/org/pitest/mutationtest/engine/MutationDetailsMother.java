package org.pitest.mutationtest.engine;

import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MethodName;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;

public class MutationDetailsMother {

  public static MutationDetails makeMutation() {
    return makeMutation(ClassName.fromString("foo"));
  }

  public static MutationDetails makeMutation(final ClassName clazz) {
    return new MutationDetails(new MutationIdentifier(Location.location(clazz, new MethodName("aMethod"), "()V"), 1,
        "mutatorId"), "foo.java", "A mutation", 0,
        0);
  }

}
