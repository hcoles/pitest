package org.pitest.maven.report;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;

import org.junit.Test;
import org.pitest.coverage.BlockCoverage;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationResult;

public class PitAggregationMojoTest {

  @Test
  public void testCanConvertTextToCoverageResults() throws IOException {
    final PitAggregationMojo mojo = new PitAggregationMojo();

    final Collection<BlockCoverage> results = mojo.loadCoverageData(
        PitAggregationMojo.class.getResourceAsStream("/linecoverage.xml"));

    assertNotNull(results);
    assertEquals(5, results.size());
  }

  @Test
  public void testCanConvertTextToMutationResults() throws IOException {
    final PitAggregationMojo mojo = new PitAggregationMojo();

    final Collection<MutationResult> results = mojo.loadExecutionData(
        PitAggregationMojo.class.getResourceAsStream("/mutations.xml"));

    assertNotNull(results);
    assertEquals(2, results.size());

    for (final MutationResult result : results) {
      if (result.getDetails().getFirstIndex() == 5) {
        assertEquals(38, result.getDetails().getBlock());
        assertEquals("com.mycompany.OrderedWeightedValueSampler",
            result.getDetails().getId().getClassName().asJavaName());
        assertEquals(202, result.getDetails().getLineNumber());
        assertFalse(result.getStatus().isDetected());
        assertEquals(DetectionStatus.NO_COVERAGE, result.getStatus());
      } else {
        assertEquals(27, result.getDetails().getBlock());
        assertEquals("com.mycompany.OrderedWeightedValueSampler",
            result.getDetails().getId().getClassName().asJavaName());
        assertEquals(77, result.getDetails().getLineNumber());
        assertTrue(result.getStatus().isDetected());
        assertEquals(DetectionStatus.KILLED, result.getStatus());
      }
    }
  }
}
