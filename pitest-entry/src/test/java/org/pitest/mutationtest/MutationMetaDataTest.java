package org.pitest.mutationtest;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.pitest.mutationtest.LocationMother.aMutationId;
import static org.pitest.mutationtest.engine.MutationDetailsMother.aMutationDetail;

public class MutationMetaDataTest {

  @Test
  public void shouldPartitionResultsByMutatedClass() {
    final MutationResult a = makeResult("Foo", "a");
    final MutationResult b = makeResult("Bar", "a");
    final MutationResult c = makeResult("Foo", "b");
    final MutationResult d = makeResult("Foo", "c");

    final MutationMetaData testee = new MutationMetaData(Arrays.asList(a, b, c, d));
    final Collection<ClassMutationResults> actual = testee.toClassResults();

    assertThat(actual).hasSize(2);

    final Iterator<ClassMutationResults> it = actual.iterator();
    final ClassMutationResults first = it.next();
    final ClassMutationResults second = it.next();

    assertThat(first.getMutatedClass()).isEqualTo(ClassName.fromString("Bar"));
    assertThat(first.getMutations()).hasSize(1);

    assertThat(second.getMutatedClass()).isEqualTo(ClassName.fromString("Foo"));
    assertThat(second.getMutations()).hasSize(3);

  }

  @Test
  public void shouldNotCreateEmptyClassResultsObjects() {
    final MutationMetaData testee = new MutationMetaData(
        Collections.<MutationResult> emptyList());
    assertThat(testee.toClassResults()).isEmpty();
  }

  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(MutationMetaData.class).verify();
  }

  private MutationResult makeResult(String clazz, String method) {
    final Location location = Location.location(ClassName.fromString(clazz),
        method, "()V");
    final MutationDetails md = aMutationDetail().withId(
        aMutationId().withLocation(location)).build();
    return new MutationResult(md,
        MutationStatusTestPair.notAnalysed(0, DetectionStatus.KILLED));
  }

}
