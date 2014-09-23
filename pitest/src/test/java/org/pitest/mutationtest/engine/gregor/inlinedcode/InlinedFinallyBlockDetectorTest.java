package org.pitest.mutationtest.engine.gregor.inlinedcode;

import static org.junit.Assert.assertEquals;
import static org.pitest.mutationtest.LocationMother.aLocation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;

public class InlinedFinallyBlockDetectorTest {

  private InlinedFinallyBlockDetector testee;

  @Before
  public void setUp() {
    this.testee = new InlinedFinallyBlockDetector();
  }

  @Test
  public void shouldNotCombineMutantsWhenOnSameLineAndDifferentBlocksButFromDifferentMutators() {
    final int line = 100;
    final int block = 1;
    final List<MutationDetails> mutations = Arrays.asList(
        makeMutant(line, block, "Foo", 0),
        makeMutant(line, block + 1, "NotFoo", 1));
    assertEquals(mutations, this.testee.process(mutations));
  }

  @Test
  public void shouldNotCombineMutantsWhenOnSameLineAndBlock() {
    final int line = 100;
    final int block = 1;
    final String mutator = "foo";
    final List<MutationDetails> mutations = Arrays.asList(
        makeMutant(line, block, mutator, 0),
        makeMutant(line, block, mutator, 1));
    assertEquals(mutations, this.testee.process(mutations));
  }

  @Test
  public void shouldCreateSingleMutantWhenSameMutationCreatedOnSameLineInDifferentBlocksAndOneIsInAHandlerBlock() {
    final int line = 100;
    final String mutator = "foo";
    final int block = 1000;
    final List<MutationDetails> mutations = Arrays.asList(
        makeMutantInHandlerBlock(line, block, mutator, 0),
        makeMutant(line, block + 1, mutator, 1));
    assertEquals(
        Arrays.asList(makeMutantInHandlerBlock(line, block, mutator,
            Arrays.asList(0, 1))), this.testee.process(mutations));
  }

  @Test
  public void shouldNotCombineMutationsWhenMoreThanOneInAHandlerBlock() {
    final int line = 100;
    final String mutator = "foo";
    final int block = 1000;
    final List<MutationDetails> mutations = Arrays.asList(
        makeMutantInHandlerBlock(line, block, mutator, 0),
        makeMutantInHandlerBlock(line, block, mutator, 2),
        makeMutant(line, block + 1, mutator, 1));
    final Collection<MutationDetails> actual = this.testee.process(mutations);
    assertEquals(mutations, actual);
  }

  private MutationDetails makeMutantInHandlerBlock(final int line,
      final int block, final String mutator, final int index) {
    return new MutationDetails(makeId(Collections.singleton(index), mutator),
        "file", "desc", line, block, true, false);
  }

  private MutationDetails makeMutantInHandlerBlock(final int line,
      final int block, final String mutator, final Collection<Integer> indexes) {
    return new MutationDetails(makeId(new HashSet<Integer>(indexes), mutator),
        "file", "desc", line, block, true, false);
  }

  private MutationDetails makeMutant(final int line, final int block,
      final String mutator, final Collection<Integer> indexes) {
    return new MutationDetails(makeId(new HashSet<Integer>(indexes), mutator),
        "file", "desc", line, block);
  }

  private MutationDetails makeMutant(final int line, final int block,
      final String mutator, final int index) {
    return makeMutant(line, block, mutator, Arrays.asList(index));
  }

  private MutationIdentifier makeId(final Set<Integer> indexes,
      final String mutator) {
    return new MutationIdentifier(aLocation().build(), indexes, mutator);
  }

}
