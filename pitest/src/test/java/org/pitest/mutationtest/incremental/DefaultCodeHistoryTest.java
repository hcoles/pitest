package org.pitest.mutationtest.incremental;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

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
import org.pitest.functional.Option;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.execute.MutationStatusTestPair;
import org.pitest.mutationtest.results.DetectionStatus;

public class DefaultCodeHistoryTest {
  
  private DefaultCodeHistory testee;
  
  @Mock
  private ClassInfoSource classInfoSource;

  private Map<ClassName, ClassIdentifier> historicClassPath = new  HashMap<ClassName, ClassIdentifier>();

  private Map<MutationIdentifier, MutationStatusTestPair> results = new HashMap<MutationIdentifier, MutationStatusTestPair> ();
  
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    testee = new DefaultCodeHistory(classInfoSource, results, historicClassPath);
  }

  @Test
  public void shouldReturnNoneWhenNoMatchingHistoricResultExists() {
    final MutationIdentifier id = new MutationIdentifier("foo", 0, "mutator");
    Option<MutationStatusTestPair> actual = testee.getPreviousResult(id);
    assertEquals(Option.none(), actual);
  }
  
  @Test
  public void shouldReturnHistoricResultWhenOneExists() {
    final MutationIdentifier id = new MutationIdentifier("foo", 0, "mutator");
    final MutationStatusTestPair expected = new MutationStatusTestPair(0, DetectionStatus.KILLED, "foo");
    results.put(id, expected);
    Option<MutationStatusTestPair> actual = testee.getPreviousResult(id);
    assertEquals(Option.some(expected), actual);
  }
  
  @Test
  public void shouldTreatNewClassAsChanged() {
    assertTrue(testee.hasClassChanged(ClassName.fromString("notInLastRun")));
  }

  @Test
  public void shouldTreatClassesWithDifferentHashesAsChanged() {
    long currentHash = 42;
    ClassName foo = ClassName.fromString("foo");
    ClassIdentifier currentId = new ClassIdentifier(currentHash, foo);
    setCurrentClassPath(currentId);
    historicClassPath.put(foo, new ClassIdentifier(currentHash + 1,foo));
    assertTrue(testee.hasClassChanged(ClassName.fromString("foo")));
  }

  
  @Test
  public void shouldTreatClassesWithSameHashAsUnChanged() {
    ClassName foo = ClassName.fromString("foo");
    ClassIdentifier currentId = new ClassIdentifier(0, foo);
    setCurrentClassPath(currentId);
    historicClassPath.put(foo, currentId);
    assertFalse(testee.hasClassChanged(ClassName.fromString("foo")));
  }

  private void setCurrentClassPath(ClassIdentifier currentId) {
    ClassInfo currentClass = ClassInfoMother.make(currentId);
    when(this.classInfoSource.fetchClass(ClassName.fromString("foo"))).thenReturn(Option.some(currentClass));
  }
  
}
