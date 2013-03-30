package org.pitest.mutationtest;

import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.MutationIdentifier;

public class MutationDetailsMother {

  public static MutationDetails makeMutation() {
    return makeMutation(ClassName.fromString("foo"));
  }

  public static MutationDetails makeMutation(final ClassName clazz) {
    return new MutationDetails(new MutationIdentifier(clazz.asJavaName(), 1,
        "mutatorId"), "foo.java", "A mutation", "fooMethod", 0, 0);
  }

}
