package org.pitest.aggregate;

import static org.assertj.core.api.Assertions.assertThat;

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

    assertThat(results).isNotNull();
    assertThat(results).hasSize(2);

    for (final MutationResult result : results) {
      if (result.getDetails().getFirstIndex() == 5) {
        assertThat(result.getDetails().getBlocks()).contains(38);
        assertThat(result.getDetails().getId().getClassName().asJavaName()).isEqualTo("com.mycompany.OrderedWeightedValueSampler");
        assertThat(result.getDetails().getLineNumber()).isEqualTo(202);
        assertThat(result.getStatus().isDetected()).isFalse();
        assertThat(result.getStatus()).isEqualTo(DetectionStatus.NO_COVERAGE);
      } else {
        assertThat(result.getDetails().getBlocks()).contains(27);
        assertThat(result.getDetails().getId().getClassName().asJavaName()).isEqualTo("com.mycompany.OrderedWeightedValueSampler");
        assertThat(result.getDetails().getLineNumber()).isEqualTo(77);
        assertThat(result.getStatus().isDetected()).isTrue();
        assertThat(result.getStatus()).isEqualTo(DetectionStatus.KILLED);
      }
    }
  }

}
