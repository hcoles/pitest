package org.pitest.mutationtest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.reeltwo.jumble.mutation.Mutater;

public class MutationConfigTest {

  private MutationConfig testee;

  @Test
  public void testNoMutationsSetByDefault() {
    this.testee = new MutationConfig();
    for (final Mutator each : Mutator.values()) {
      assertFalse(this.testee.has(each));
    }
  }

  @Test
  public void testMutationsPassedToConstructorAreStored() {
    this.testee = new MutationConfig(Mutator.CPOOL, Mutator.INCREMENTS);
    assertTrue(this.testee.has(Mutator.CPOOL));
    assertTrue(this.testee.has(Mutator.INCREMENTS));
  }

  @Test
  public void testNoMutationsConfiguredInReturnedMutatorIfNonePassedToTheConstructor()
      throws Exception {
    this.testee = new MutationConfig();
    final Mutater actual = this.testee.createMutator();
    class Canary {
      @SuppressWarnings("unused")
      public int f(final int i) {
        switch (i) {
        case 0:
          return 1;
        }
        return 0;
      }
    }

    assertEquals(0, actual.countMutationPoints(Canary.class.getName()));
  }

  @Test
  public void testCreateMutatorReturnsAMutatorWithNoMutationSet() {
    this.testee = new MutationConfig(Mutator.CPOOL, Mutator.SWITCHES);
    final Mutater actual = this.testee.createMutator();
    assertNull(actual.getModification());
  }

}
