package org.pitest.aggregate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationResult;

public class MutationResultDataLoaderTest {

  private MutationResultDataLoader underTest;

  @Before
  public void setup() throws Exception {
    final URL url = MutationResultDataLoaderTest.class.getResource("/full-data/mutations.xml");
    final File file = new File(url.toURI());

    this.underTest = new MutationResultDataLoader(Arrays.asList(file));
  }

  @Test
  public void testLoadData() throws Exception {
    final Collection<MutationResult> results = this.underTest.loadData();

    assertNotNull(results);
    assertEquals(2, results.size());

    for (final MutationResult result : results) {
      if (result.getDetails().getFirstIndex() == 5) {
        assertEquals(38, result.getDetails().getBlock());
        assertEquals("com.mycompany.OrderedWeightedValueSampler", result.getDetails().getId().getClassName().asJavaName());
        assertEquals(202, result.getDetails().getLineNumber());
        assertFalse(result.getStatus().isDetected());
        assertEquals(DetectionStatus.NO_COVERAGE, result.getStatus());
      } else {
        assertEquals(27, result.getDetails().getBlock());
        assertEquals("com.mycompany.OrderedWeightedValueSampler", result.getDetails().getId().getClassName().asJavaName());
        assertEquals(77, result.getDetails().getLineNumber());
        assertTrue(result.getStatus().isDetected());
        assertEquals(DetectionStatus.KILLED, result.getStatus());
      }
    }
  }

}
