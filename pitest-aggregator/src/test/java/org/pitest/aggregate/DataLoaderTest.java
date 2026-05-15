package org.pitest.aggregate;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
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

    assertThat(results).hasSize(1);
    MutationResult result = results.iterator().next();
//    assertEquals("true",  result.);
    assertThat(result.getStatus()).isEqualTo(KILLED);
    assertThat(result.getNumberOfTestsRun()).isEqualTo(1);
    assertThat(result.getDetails().getFilename()).isEqualTo("OrderedWeightedValueSampler.java");
    assertThat(result.getDetails().getClassName().asJavaName()).isEqualTo("com.mycompany.OrderedWeightedValueSampler");
    assertThat(result.getDetails().getId().getLocation().getMethodName()).isEqualTo("<init>");
    assertThat(result.getDetails().getId().getLocation().getMethodDesc()).isEqualTo("(JLjava/util/function/Consumer;Ljava/util/function/BiFunction;I)V");
    assertThat(result.getDetails().getLineNumber()).isEqualTo(77);
    assertThat(result.getDetails().getMutator()).isEqualTo("org.pitest.mutationtest.engine.gregor.mutators.MathMutator");

    assertThat(result.getDetails().getId().getIndexes()).containsExactly(61, 62);

    assertThat(result.getKillingTest().orElse(null)).isEqualTo(
        "com.mycompany.SmallScaleOrderedWeightedValueSamplerTest.shouldSucceedWithVariousGapTimestamps(com.mycompany.SmallScaleOrderedWeightedValueSamplerTest)");
    assertThat(result.getDetails().getDescription()).isEqualTo("Replaced long multiplication with division");

    assertThat(result.getDetails().getBlocks()).containsExactly(27, 28);
  }

  @Test
  public void testLoadCoverageSnippet() throws Exception {
    String file = "/snippets/linecoverage.xml";
    BlockCoverageDataLoader dataLoader = new BlockCoverageDataLoader(asList(new File(file)));
    Set<BlockCoverage> results = dataLoader.loadData(DataLoaderTest.class.getResourceAsStream(file), new File(file));
    
    assertThat(results).hasSize(1);
    BlockCoverage result = results.iterator().next();
    assertThat(result.getBlock().getLocation().getClassName().asJavaName()).isEqualTo("com.example.DividerTest");
    assertThat(result.getBlock().getLocation().getMethodName()).isEqualTo("testDivide");
    assertThat(result.getBlock().getLocation().getMethodDesc()).isEqualTo("()V");
    assertThat(result.getBlock().getBlock()).isEqualTo(1);
    assertThat(result.getTests()).containsExactly("com.example.DividerTest.testDivide(com.example.DividerTest)");
  }

}
