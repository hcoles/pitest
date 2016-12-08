package org.pitest.mutationtest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.pitest.mutationtest.LocationMother.aMutationId;
import static org.pitest.mutationtest.engine.MutationDetailsMother.aMutationDetail;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MethodName;
import org.pitest.mutationtest.engine.MutationDetails;

public class MutationMetaDataTest {

  @Test
  public void shouldPartitionResultsByMutatedClass() {
    MutationResult a = makeResult("Foo", "a");
    MutationResult b = makeResult("Bar", "a");
    MutationResult c = makeResult("Foo", "b");
    MutationResult d = makeResult("Foo", "c");

    MutationMetaData testee = new MutationMetaData(Arrays.asList(a, b, c, d));
    Collection<ClassMutationResults> actual = testee.toClassResults();

    assertThat(actual).hasSize(2);

    Iterator<ClassMutationResults> it = actual.iterator();
    ClassMutationResults first = it.next();
    ClassMutationResults second = it.next();

    assertThat(first.getMutatedClass()).isEqualTo(ClassName.fromString("Bar"));
    assertThat(first.getMutations()).hasSize(1);

    assertThat(second.getMutatedClass()).isEqualTo(ClassName.fromString("Foo"));
    assertThat(second.getMutations()).hasSize(3);

  }

  @Test
  public void shouldNotCreateEmptyClassResultsObjects() {
    MutationMetaData testee = new MutationMetaData(
        Collections.<MutationResult> emptyList());
    assertThat(testee.toClassResults()).isEmpty();
  }

  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(MutationMetaData.class).verify();
  }

  private MutationResult makeResult(String clazz, String method) {
    Location location = Location.location(ClassName.fromString(clazz),
        MethodName.fromString(method), "()V");
    MutationDetails md = aMutationDetail().withId(
        aMutationId().withLocation(location)).build();
    final MutationResult mr = new MutationResult(md,
        new MutationStatusTestPair(0, DetectionStatus.KILLED));
    return mr;
  }

}
