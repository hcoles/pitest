package org.pitest.mutationtest.build.intercept.timeout;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.GregorMutater;
import org.pitest.mutationtest.engine.gregor.mutators.NonVoidMethodCallMutator;
import org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveIncrementsMutator;

public class InfiniteIteratorLoopFilterTest extends InfiniteLoopBaseTest {

  InfiniteIteratorLoopFilter testee = new InfiniteIteratorLoopFilter();

  @Override
  InfiniteIteratorLoopFilter testee() {
    return this.testee;
  }

  @Test
  public void shouldNotFilterMutationsInMethodsThatAppearToAlreadyHaveInfiniteLoops() {
    final GregorMutater mutator = createMutator(RemoveIncrementsMutator.REMOVE_INCREMENTS_MUTATOR);
    // our analysis incorrectly identifies some loops as infinite - must skip these
    final List<MutationDetails> mutations = mutator.findMutations(ClassName.fromClass(DontFilterMyAlreadyInfiniteLoop.class));
    assertThat(mutations).hasSize(1);

    this.testee.begin(forClass(DontFilterMyAlreadyInfiniteLoop.class));
    final Collection<MutationDetails> actual = this.testee.intercept(mutations, mutator);
    this.testee.end();

    assertThat(actual).hasSize(1);
  }

  @Test
  public void shouldFilterMutationsThatRemoveIteratorNextCalls() {
    final GregorMutater mutator = createMutator(NonVoidMethodCallMutator.NON_VOID_METHOD_CALL_MUTATOR);
    final List<MutationDetails> mutations = mutator.findMutations(ClassName.fromClass(MutateMyForEachLoop.class));
    assertThat(mutations).hasSize(3);

    this.testee.begin(forClass(MutateMyForEachLoop.class));
    final Collection<MutationDetails> actual = this.testee.intercept(mutations, mutator);
    this.testee.end();

    assertThat(actual).hasSize(2);
  }


  @Test
  public void shouldNotFindInfiniteLoopInForEach() {
    checkNotFiltered(HasIteratorLoops.class, "forEach");
  }

  @Test
  public void shouldNotFindInfiniteLoopInHandCodedInteratorLoop() {
    checkNotFiltered(HasIteratorLoops.class, "iteratorLoop");
  }

  @Test
  public void shouldFindInfiniteLoopInIteratorLoopWithoutNext() {
    checkFiltered(HasIteratorLoops.class, "infiniteNoNextCall");
  }

}

class HasIteratorLoops {
  public void forEach(List<String> ss) {
    for (final String each : ss) {
      System.out.println(each);
    }
  }

  public void iteratorLoop(List<String> ss) {
    for (final String s : ss) {
      System.out.println(s);
    }
  }

  public void infiniteNoNextCall(List<String> ss) {
    for(final Iterator<String> it = ss.iterator(); it.hasNext(); ) {
      System.out.println(it);
    }
  }

}

class MutateMyForEachLoop {
  public void forEach(List<String> ss) {
    for (final String each : ss) {
      System.out.println(each);
    }
  }
}


