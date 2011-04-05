package org.pitest.coverage.execute;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;
import org.pitest.Description;
import org.pitest.coverage.ClassStatistics;
import org.pitest.internal.IsolationUtils;

public class CoverageResultTest {

  @Test
  public void shouldSerializeQuickly() {
    final Description d = new Description("foo",
        Collections.<Class<?>> emptyList(), null);

    final Collection<ClassStatistics> cov = Arrays.asList(cov(), cov(), cov(),
        cov(), cov(), cov());
    final CoverageResult cr = new CoverageResult(d, 100, true, cov);

    final long t0 = System.currentTimeMillis();
    for (int i = 0; i != 1000; i++) {

      IsolationUtils.fromXml(IsolationUtils.toXml(cr));
    }
    System.out.println(System.currentTimeMillis() - t0);

  }

  private ClassStatistics cov() {
    return new ClassStatistics("foo", oneToTwenty());
  }

  private Collection<Integer> oneToTwenty() {
    return Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
        17, 18, 19, 20);
  }
}
