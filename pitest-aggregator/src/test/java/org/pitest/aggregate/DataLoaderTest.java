package org.pitest.aggregate;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.pitest.mutationtest.DetectionStatus.KILLED;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

import org.junit.Test;
import org.pitest.coverage.BlockCoverage;
import org.pitest.mutationtest.MutationResult;

public class DataLoaderTest {

  @Test
  public void testLoadMutationSnippet() throws Exception {
    String file = "/snippets/mutation.xml";
    MutationResultDataLoader dataLoader = new MutationResultDataLoader(asList(new File(file)));
    Set<MutationResult> results = dataLoader.loadData(DataLoaderTest.class.getResourceAsStream(file), new File(file));

    assertEquals(1, results.size());
    MutationResult result = results.iterator().next();
//    assertEquals("true",  result.);
    assertEquals(KILLED, result.getStatus());
    assertEquals(1, result.getNumberOfTestsRun());
    assertEquals("OrderedWeightedValueSampler.java", result.getDetails().getFilename());
    assertEquals("com.mycompany.OrderedWeightedValueSampler", result.getDetails().getClassName().asJavaName());
    assertEquals("<init>", result.getDetails().getId().getLocation().getMethodName());
    assertEquals("(JLjava/util/function/Consumer;Ljava/util/function/BiFunction;I)V", result.getDetails().getId().getLocation().getMethodDesc());
    assertEquals(77, result.getDetails().getLineNumber());
    assertEquals("org.pitest.mutationtest.engine.gregor.mutators.MathMutator", result.getDetails().getMutator());

    assertThat(result.getDetails().getId().getIndexes()).containsExactly(61, 62);

    assertEquals(
        "com.mycompany.SmallScaleOrderedWeightedValueSamplerTest.shouldSucceedWithVariousGapTimestamps(com.mycompany.SmallScaleOrderedWeightedValueSamplerTest)",
        result.getKillingTest().orElse(null));
    assertEquals("Replaced long multiplication with division", result.getDetails().getDescription());

    assertThat(result.getDetails().getBlocks()).containsExactly(27, 28);
  }

  @Test
  public void testLoadCoverageSnippet() throws Exception {
    String file = "/snippets/linecoverage.xml";
    BlockCoverageDataLoader dataLoader = new BlockCoverageDataLoader(asList(new File(file)));
    Set<BlockCoverage> results = dataLoader.loadData(DataLoaderTest.class.getResourceAsStream(file), new File(file));
    
    assertEquals(1, results.size());
    BlockCoverage result = results.iterator().next();
    assertEquals("com.example.DividerTest", result.getBlock().getLocation().getClassName().asJavaName());
    assertEquals("testDivide", result.getBlock().getLocation().getMethodName());
    assertEquals("()V", result.getBlock().getLocation().getMethodDesc());
    assertEquals(1, result.getBlock().getBlock());
    assertEquals(Arrays.asList("com.example.DividerTest.testDivide(com.example.DividerTest)"), result.getTests());
  }

}
