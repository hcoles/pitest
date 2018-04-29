package org.pitest.aggregate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.pitest.coverage.BlockCoverage;

public class BlockCoverageDataLoaderTest {

  private BlockCoverageDataLoader underTest;

  @Before
  public void setup() throws Exception {
    final URL url = MutationResultDataLoaderTest.class.getResource("/full-data/linecoverage.xml");
    final File file = new File(url.toURI());

    this.underTest = new BlockCoverageDataLoader(Arrays.asList(file));
  }

  @Test
  public void testLoadData() throws Exception {
    final Collection<BlockCoverage> results = this.underTest.loadData();

    assertNotNull(results);
    assertEquals(5, results.size());
    for (final BlockCoverage block : results) {
      assertNotNull(block.getTests());
      assertEquals(1, block.getTests().size());

      assertEquals("com.example.DividerTest.testDivide(com.example.DividerTest)", block.getTests().iterator().next());
    }
  }

}
