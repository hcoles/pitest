package org.pitest.mutationtest;

import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MethodName;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.quickbuilder.Builder;
import org.pitest.quickbuilder.Generator;
import org.pitest.quickbuilder.SequenceBuilder;
import org.pitest.quickbuilder.builders.QB;

public class LocationMother {

  public interface MutationIdentifierBuilder extends
  SequenceBuilder<MutationIdentifier> {

    MutationIdentifierBuilder withLocation(Location location);

    MutationIdentifierBuilder withLocation(Builder<Location> location);

    MutationIdentifierBuilder withIndex(int index);

    MutationIdentifierBuilder withMutator(String mutator);

    Location _Location();

    int _Index();

    String _Mutator();

  }

  public interface LocationBuilder extends SequenceBuilder<Location> {
    LocationBuilder withClass(ClassName clazz);

    LocationBuilder withMethod(String method);

    LocationBuilder withMethodDescription(String methodDesc);

    ClassName _Class();

    String _Method();

    String _MethodDescription();

  }

  public static LocationBuilder aLocation(String clazz) {
    return aLocation().withClass(ClassName.fromString(clazz));
  }

  public static LocationBuilder aLocation() {
    return QB.builder(LocationBuilder.class, locationSeed())
        .withClass(ClassName.fromString("clazz")).withMethod("method")
        .withMethodDescription("()I");
  }

  private static Generator<LocationBuilder, Location> locationSeed() {
    return b -> Location.location(b._Class(),
        MethodName.fromString(b._Method()), b._MethodDescription());
  }

  public static MutationIdentifierBuilder aMutationId() {
    return QB.builder(MutationIdentifierBuilder.class, idSeed())
        .withLocation(aLocation()).withIndex(1).withMutator("mutator");
  }

  private static Generator<MutationIdentifierBuilder, MutationIdentifier> idSeed() {
    return b -> new MutationIdentifier(b._Location(), b._Index(), b._Mutator());
  }
}
