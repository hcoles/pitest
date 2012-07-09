package org.pitest.mutationtest.engine.gregor.inlinedcode;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;


public class InlinedFinallyBlockDetectorTest {
  
  private InlinedFinallyBlockDetector testee;
  
  @Before
  public void setUp() {
    testee = new InlinedFinallyBlockDetector();
  }
  
  @Test
  public void shouldNotCombineMutantsWhenOnSameLineAndDifferentBlocksButFromDifferentMutators() {
    int line = 100;
    int block = 1;
    List<MutationDetails> mutations = Arrays.asList(makeMutant(line, block, "Foo",0), makeMutant(line, block + 1,"NotFoo",1));
    assertEquals(mutations, testee.process(mutations));
  }
  
  @Test
  public void shouldNotCombineMutantsWhenOnSameLineAndBlock() {
    int line = 100;
    int block = 1;
    String mutator = "foo";
    List<MutationDetails> mutations = Arrays.asList(makeMutant(line, block, mutator,0), makeMutant(line, block, mutator,1));
    assertEquals(mutations, testee.process(mutations));
  }
  
  
  @Test
  public void shouldCreateSingleMutantWhenSameMutationCreatedOnSameLineInDifferentBlocks() {
    int line = 100;
    String mutator = "foo";
    int block = 1000;
    List<MutationDetails> mutations = Arrays.asList(makeMutant(line, block, mutator,0), makeMutant(line, block + 1,mutator,1));
    assertEquals(Arrays.asList(makeMutant(line,block,mutator, Arrays.asList(0,1))), testee.process(mutations));
  }
  
  @Test
  public void shouldNotCombineMutationsWhenSomeOccurInSameBlockButOthersDont() {
    int line = 100;
    String mutator = "foo";
    int block = 1000;
    List<MutationDetails> mutations = Arrays.asList(makeMutant(line, block, mutator,0)
                                                  , makeMutant(line, block, mutator,2)
                                                  , makeMutant(line, block + 1,mutator,1));
    Collection<MutationDetails> actual = testee.process(mutations);
    assertEquals(mutations,actual);
  }
  
  private MutationDetails makeMutant(int line, int block, String mutator, Collection<Integer> indexes) {
    return new MutationDetails(makeId(new HashSet<Integer>(indexes),mutator), "file", "desc", "method", line, block);
  }
  
  private MutationDetails makeMutant(int line, int block, String mutator, int index) {
    return makeMutant(line, block, mutator, Arrays.asList(index));
  }
  
  private MutationIdentifier makeId(Set<Integer> indexes, String mutator) {
    return new MutationIdentifier("foo", indexes, mutator);
  }

}
