package org.pitest.mutationtest.build.intercept.javafeatures;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.NonVoidMethodCallMutator;
import org.pitest.mutationtest.engine.gregor.mutators.NullMutateEverything;

public class ForEachFilterTest {
  private static final String             PATH      = "foreach/{0}_{1}";

  ForEachLoopFilter testee = new ForEachLoopFilter();
  FilterTester verifier = new FilterTester(PATH, this.testee, NullMutateEverything.asList());

  @Test
  public void declaresTypeAsFilter() {
    assertThat(this.testee.type()).isEqualTo(InterceptorType.FILTER);
  }

  @Test
  public void filtersMutationsToForEachLoopJumps() {
    this.verifier = new FilterTester(PATH, this.testee, NegateConditionalsMutator.NEGATE_CONDITIONALS_MUTATOR);
    this.verifier.assertFiltersNMutationFromSample(1, "HasForEachLoop");
  }

  @Test
  public void filtersMutationsToHasNextAndNext() {
    this.verifier = new FilterTester(PATH, this.testee, NonVoidMethodCallMutator.NON_VOID_METHOD_CALL_MUTATOR);
    // can mutate calls to iterator, hasNext and next
    this.verifier.assertFiltersNMutationFromSample(3, "HasForEachLoop");
  }

  @Test
  public void filtersMutationsToForEachOverField() {
    this.verifier = new FilterTester(PATH, this.testee, NonVoidMethodCallMutator.NON_VOID_METHOD_CALL_MUTATOR);
    // can mutate calls to iterator, hasNext and next
    this.verifier.assertFiltersNMutationFromClass(3, HasForEachLoopOverField.class);
  }

  @Test
  public void filtersMutationsToForEachOverMethodReturn() {
    this.verifier = new FilterTester(PATH, this.testee, NonVoidMethodCallMutator.NON_VOID_METHOD_CALL_MUTATOR);
    // can mutate calls to iterator, hasNext and next
    this.verifier.assertFiltersNMutationFromClass(3, HasForEachLoopOverMethodReturn.class);
  }

  @Test
  public void filtersMutationsToForEachOverCollections() {
    this.verifier = new FilterTester(PATH, this.testee, NonVoidMethodCallMutator.NON_VOID_METHOD_CALL_MUTATOR);
    // can mutate calls to iterator, hasNext and next
    this.verifier.assertFiltersNMutationFromClass(3, HasForEachLoopOverCollection.class);
  }

  @Test
  public void filtersMutationsToForEachOverArrays() {
    // arrayLength, IConst, Jump, IINC
    this.verifier.assertFiltersNMutationFromSample(4, "HasForEachLoopOverArray");
  }


  @Test
  public void doesNotFilterMutationsToIndexedForLoopJumps() {
    this.verifier = new FilterTester("forloops/{0}_{1}", this.testee, NegateConditionalsMutator.NEGATE_CONDITIONALS_MUTATOR);
    this.verifier.assertFiltersNMutationFromSample(0, "HasAForLoop");
  }

  @Test
  public void doesNotFilterMutationsToHandRolledIteratorLoops() {
    // additional label nodes seem to be enough to prevent triggering
    this.verifier.assertFiltersNMutationFromSample(0, "HandRolledIteratorLoop");
  }

  public static class HasForEachLoop {
    void foo(List<Integer> is) {
      for (final int each : is) {
        System.out.println(each);
      }
    }
  }

  public static class HasForEachLoopOverField {
    List<Integer> is;
    void foo() {
      for (final int each : this.is) {
        System.out.println(each);
      }
    }
  }

  public static class HasForEachLoopOverMethodReturn {
    void foo() {
      for (final int each : Collections.singletonList(1)) {
        System.out.println(each);
      }
    }
  }

  public static class HasForEachLoopOverCollection {
    void foo(Collection<Integer> c) {
      for (final int each : c) {
        System.out.println(each);
      }
    }
  }

  public static class HasForEachLoopOverArray {
    void foo(int[] is) {
      for (final int each : is) {
        System.out.println(each);
      }
    }
  }


  static class HandRolledIteratorLoop {
    void foo(List<Integer> is) {
      final Iterator<Integer> it = is.iterator();
      while (it.hasNext()) {
        final Integer each = it.next();
        System.out.println(each);
      }
    }
  }

}
