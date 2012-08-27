package org.pitest.mutationtest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.classinfo.ClassName;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.coverage.domain.TestInfo;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.internal.ClassByteArraySource;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.filter.MutationFilterFactory;
import org.pitest.mutationtest.filter.UnfilteredMutationFilter;
import org.pitest.mutationtest.instrument.ClassLine;


public class MutationSourceTest {

  private MutationSource testee;
  
  private MutationConfig config;
  
  @Mock
  private MutationFilterFactory filter;
  
  @Mock
  private CoverageDatabase coverage;
  
  @Mock
  private ClassByteArraySource source;
  
  @Mock
  private Mutater mutater;
  
  @Mock
  private MutationEngine engine;
  
  private ClassName foo = ClassName.fromString("foo");
  
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    when(engine.createMutator(source)).thenReturn(mutater);
    config =  new MutationConfig(engine, Collections.<String>emptyList());
    setupFilterFactoryToFilterNothing();
    testee = new MutationSource(config, filter, coverage, source);
  }
  
  private void setupFilterFactoryToFilterNothing() {
    when(filter.createFilter()).thenReturn(UnfilteredMutationFilter.INSTANCE);
  }
  
  @Test
  public void shouldReturnNoMuationsWhenNoneFound() {
    assertEquals(Collections.emptyList(),testee.createMutations(foo));
  }
  
  @Test
  public void shouldAssignTestsForRelevantLineToGeneratedMutations() {
    List<TestInfo> expected = makeTestInfos(0);
    List<MutationDetails> mutations = makeMutations("foo");
    when(coverage.getTestsForClassLine(any(ClassLine.class))).thenReturn(expected);

    when(mutater.findMutations(any(ClassName.class))).thenReturn(mutations);
    MutationDetails actual = testee.createMutations(foo).iterator().next();
    assertEquals(expected,actual.getTestsInOrder());
  }

  @Test
  public void shouldAssignAllTestsForClassWhenMutationInStaticInitialiser() {
    List<TestInfo> expected = makeTestInfos(0);
    List<MutationDetails> mutations = makeMutations("<clinit>");
    when(coverage.getTestsForClass(foo)).thenReturn(expected);
    when(mutater.findMutations(any(ClassName.class))).thenReturn(mutations);
    MutationDetails actual = testee.createMutations(foo).iterator().next();
    assertEquals(expected,actual.getTestsInOrder());
  }
  
  @Test
  public void shouldPrioritiseTestsByExecutionTime() {
    List<TestInfo> unorderedTests = makeTestInfos(100,1000,1);
    List<MutationDetails> mutations = makeMutations("foo");
    when(coverage.getTestsForClassLine(any(ClassLine.class))).thenReturn(unorderedTests);
    when(mutater.findMutations(any(ClassName.class))).thenReturn(mutations);
    MutationDetails actual = testee.createMutations(foo).iterator().next();
    assertEquals(makeTestInfos(1,100,1000),actual.getTestsInOrder());
  }
  
  private List<TestInfo> makeTestInfos(Integer ... times ) {
    return new ArrayList<TestInfo>(FCollection.map(Arrays.asList(times), timeToTestInfo()));
  }
  
  
  private F<Integer,TestInfo> timeToTestInfo() {
    return new F<Integer,TestInfo>() {
      public TestInfo apply(Integer a) {
        return new TestInfo("foo","bar",a, Option.<ClassName>none(),0);
      }
      
    };
  }

  private List<MutationDetails> makeMutations(String method) {
    List<MutationDetails> mutations = Arrays.asList(makeMutation(method));
    return mutations;
  }

  private MutationDetails makeMutation(String method) {
    final MutationIdentifier id = new MutationIdentifier("foo", 0, "mutator");
    return new MutationDetails(id, "file", "desc", method, 1,2);
  }
  
}
