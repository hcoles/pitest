package org.pitest.mutationtest.build;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.pitest.mutationtest.LocationMother.aLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.coverage.TestInfo;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.process.LaunchOptions;

@RunWith(MockitoJUnitRunner.class)
public class MutationSourceTest {

  private MutationSource       testee;

  private MutationConfig       config;

  private ClassByteArraySource source = ClassloaderByteArraySource.fromContext();

  @Mock
  private Mutater              mutater;

  @Mock
  private MutationEngine       engine;

  @Mock
  private TestPrioritiser      prioritiser;

  private final ClassName      foo = ClassName.fromClass(Foo.class);

  @Before
  public void setUp() {
    when(this.engine.createMutator(any(ClassByteArraySource.class))).thenReturn(this.mutater);
    this.config = new MutationConfig(this.engine, new LaunchOptions(null));
    this.testee = new MutationSource(this.config, this.prioritiser
        , this.source, CompoundMutationInterceptor.nullInterceptor());
  }

  @Test
  public void shouldReturnNoMuationsWhenNoneFound() {
    assertEquals(Collections.emptyList(), this.testee.createMutations(this.foo));
  }

  @Test
  public void shouldAssignTestsFromPrioritiserToMutant() {
    final List<TestInfo> expected = makeTestInfos(0);
    final List<MutationDetails> mutations = makeMutations("foo");

    when(this.prioritiser.assignTests(any(MutationDetails.class))).thenReturn(
        expected);
    when(this.mutater.findMutations(any(ClassName.class)))
    .thenReturn(mutations);
    final MutationDetails actual = this.testee.createMutations(this.foo)
        .iterator().next();
    assertEquals(expected, actual.getTestsInOrder());
  }

  private List<TestInfo> makeTestInfos(final Integer... times) {
    return new ArrayList<TestInfo>(FCollection.map(Arrays.asList(times),
        timeToTestInfo()));
  }

  private F<Integer, TestInfo> timeToTestInfo() {
    return new F<Integer, TestInfo>() {
      @Override
      public TestInfo apply(final Integer a) {
        return new TestInfo("foo", "bar", a, Option.<ClassName> none(), 0);
      }
    };
  }

  private List<MutationDetails> makeMutations(final String method) {
    final List<MutationDetails> mutations = Arrays.asList(makeMutation(method));
    return mutations;
  }

  private MutationDetails makeMutation(final String method) {
    final MutationIdentifier id = new MutationIdentifier(aLocation()
        .withClass(this.foo).withMethod(method).build(), 0, "mutator");
    return new MutationDetails(id, "file", "desc", 1, 2);
  }

}

class Foo {
  
}
