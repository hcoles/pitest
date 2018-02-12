package org.pitest.mutationtest.incremental;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.pitest.mutationtest.LocationMother.aMutationId;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.classinfo.ClassIdentifier;
import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassInfoMother;
import org.pitest.classinfo.ClassInfoSource;
import org.pitest.classinfo.ClassName;
import org.pitest.classinfo.HierarchicalClassId;
import java.util.Optional;
import org.pitest.mutationtest.ClassHistory;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.MutationIdentifier;

public class DefaultCodeHistoryTest {

  private DefaultCodeHistory                                    testee;

  @Mock
  private ClassInfoSource                                       classInfoSource;

  private final Map<ClassName, ClassHistory>                    historicClassPath = new HashMap<>();

  private final Map<MutationIdentifier, MutationStatusTestPair> results           = new HashMap<>();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.testee = new DefaultCodeHistory(this.classInfoSource, this.results,
        this.historicClassPath);
  }

  @Test
  public void shouldReturnNoneWhenNoMatchingHistoricResultExists() {
    final MutationIdentifier id = aMutationId().build();
    final Optional<MutationStatusTestPair> actual = this.testee
        .getPreviousResult(id);
    assertEquals(Optional.empty(), actual);
  }

  @Test
  public void shouldReturnHistoricResultWhenOneExists() {
    final MutationIdentifier id = aMutationId().build();
    final MutationStatusTestPair expected = new MutationStatusTestPair(0,
        DetectionStatus.KILLED, "foo");
    this.results.put(id, expected);
    final Optional<MutationStatusTestPair> actual = this.testee
        .getPreviousResult(id);
    assertEquals(Optional.ofNullable(expected), actual);
  }

  @Test
  public void shouldTreatNewClassAsChanged() {
    assertTrue(this.testee
        .hasClassChanged(ClassName.fromString("notInLastRun")));
  }

  @Test
  public void shouldTreatClassesWithDifferentHashesAsChanged() {
    final long currentHash = 42;
    final ClassName foo = ClassName.fromString("foo");
    final ClassIdentifier currentId = new ClassIdentifier(currentHash, foo);
    setCurrentClassPath(ClassInfoMother.make(currentId));
    this.historicClassPath.put(foo, makeHistory(new HierarchicalClassId(
        currentHash + 1, foo, "0")));
    assertTrue(this.testee.hasClassChanged(ClassName.fromString("foo")));
  }

  @Test
  public void shouldTreatClassesWithModifiedParentAsChanged() {
    final long currentHash = 42;
    final ClassName foo = ClassName.fromString("foo");

    final ClassInfo parent = ClassInfoMother.make("parent");
    final ClassIdentifier currentId = new ClassIdentifier(currentHash, foo);
    final ClassInfo currentFoo = ClassInfoMother.make(currentId, parent);

    final ClassInfo modifiedParent = ClassInfoMother.make(new ClassIdentifier(
        parent.getHash().longValue() + 1, ClassName.fromString("parent")));
    final ClassInfo modifiedFoo = ClassInfoMother.make(currentId,
        modifiedParent);

    setCurrentClassPath(currentFoo);

    this.historicClassPath.put(foo, makeHistory(modifiedFoo));

    assertTrue(this.testee.hasClassChanged(foo));
  }

  @Test
  public void shouldTreatClassesWithSameHashAsUnChanged() {
    final ClassName foo = ClassName.fromString("foo");
    final HierarchicalClassId currentId = new HierarchicalClassId(0, foo, "0");
    setCurrentClassPath(currentId);
    this.historicClassPath.put(foo, makeHistory(currentId));
    assertFalse(this.testee.hasClassChanged(ClassName.fromString("foo")));
  }

  private void setCurrentClassPath(final HierarchicalClassId currentId) {
    final ClassInfo currentClass = ClassInfoMother.make(currentId.getId());
    when(this.classInfoSource.fetchClass(ClassName.fromString("foo")))
    .thenReturn(Optional.ofNullable(currentClass));
  }

  private void setCurrentClassPath(final ClassInfo info) {
    when(this.classInfoSource.fetchClass(ClassName.fromString("foo")))
    .thenReturn(Optional.ofNullable(info));
  }

  private ClassHistory makeHistory(final HierarchicalClassId id) {
    return new ClassHistory(id, "");
  }

  private ClassHistory makeHistory(final ClassInfo ci) {
    return makeHistory(ci.getHierarchicalId());
  }

}
