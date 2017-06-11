package org.pitest.mutationtest.engine;

import static org.pitest.mutationtest.LocationMother.aMutationId;

import java.util.Collections;
import java.util.List;

import org.pitest.classinfo.ClassName;
import org.pitest.coverage.TestInfo;
import org.pitest.quickbuilder.Builder;
import org.pitest.quickbuilder.Generator;
import org.pitest.quickbuilder.SequenceBuilder;
import org.pitest.quickbuilder.builders.QB;

public class MutationDetailsMother {

  public interface MutationDetailsBuilder extends
      SequenceBuilder<MutationDetails> {
    MutationDetailsBuilder withId(Builder<MutationIdentifier> id);

    MutationDetailsBuilder withId(MutationIdentifier id);

    MutationDetailsBuilder withFilename(String filename);

    MutationDetailsBuilder withBlock(int block);

    MutationDetailsBuilder withLineNumber(int lineNumber);

    MutationDetailsBuilder withDescription(String desc);

    MutationDetailsBuilder withTestsInOrder(List<TestInfo> tests);

    MutationDetailsBuilder withIsInFinallyBlock(boolean b);

    MutationDetailsBuilder withPoison(PoisonStatus b);

    MutationIdentifier _Id();

    String _Filename();

    int _Block();

    int _LineNumber();

    String _Description();

    boolean _IsInFinallyBlock();

    PoisonStatus _Poison();

    List<TestInfo> _TestsInOrder();
  }

  public static MutationDetailsBuilder aMutationDetail() {
    return QB.builder(MutationDetailsBuilder.class, seed()).withBlock(0)
        .withDescription("A mutation").withFilename("foo.java")
        .withId(aMutationId()).withLineNumber(42).withIsInFinallyBlock(false)
        .withPoison(PoisonStatus.NORMAL).withTestsInOrder(Collections.<TestInfo> emptyList());
  }

  private static Generator<MutationDetailsBuilder, MutationDetails> seed() {
    return new Generator<MutationDetailsBuilder, MutationDetails>() {

      @Override
      public MutationDetails generate(MutationDetailsBuilder b) {
        MutationDetails md = new MutationDetails(b._Id(), b._Filename(),
            b._Description(), b._LineNumber(), b._Block(),
            b._IsInFinallyBlock(), b._Poison());
        md.addTestsInOrder(b._TestsInOrder());
        return md;
      }

    };
  }

  public static MutationDetails makeMutation() {
    return makeMutation(ClassName.fromString("foo"));
  }

  public static MutationDetails makeMutation(final ClassName clazz) {
    return new MutationDetails(new MutationIdentifier(Location.location(clazz,
        new MethodName("aMethod"), "()V"), 1, "mutatorId"), "foo.java",
        "A mutation", 0, 0);
  }

}
