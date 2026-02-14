package org.pitest.aggregate;

import static org.assertj.core.api.Assertions.assertThat;

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

    assertThat(results).isNotNull();
    assertThat(results).hasSize(5);
    for (final BlockCoverage block : results) {
      assertThat(block.getTests()).isNotNull();
      assertThat(block.getTests()).hasSize(1);

      assertThat(block.getTests().iterator().next()).isEqualTo("com.example.DividerTest.testDivide(com.example.DividerTest)");
    }
  }

}
