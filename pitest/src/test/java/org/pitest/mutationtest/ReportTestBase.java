package org.pitest.mutationtest;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.mockito.MockitoAnnotations;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.report.SourceLocator;
import org.pitest.mutationtest.results.DetectionStatus;
import org.pitest.util.Glob;

public abstract class ReportTestBase {

  protected MetaDataExtractor metaDataExtractor;
  protected ReportOptions     data;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.metaDataExtractor = new MetaDataExtractor();
    this.data = new ReportOptions();
    this.data.setSourceDirs(Collections.<File> emptyList());
   // this.data.setMutators(Mutator.DEFAULTS.asCollection());
  }

  protected ListenerFactory listenerFactory() {
    return new ListenerFactory() {

      public MutationResultListener getListener(
          final CoverageDatabase coverage, final long startTime,
          final SourceLocator locator) {
        return ReportTestBase.this.metaDataExtractor;
      }

    };
  }

  protected void verifyResults(final DetectionStatus... detectionStatus) {
    final List<DetectionStatus> expected = Arrays.asList(detectionStatus);
    final List<DetectionStatus> actual = this.metaDataExtractor
        .getDetectionStatus();

    Collections.sort(expected);
    Collections.sort(actual);

    assertEquals(expected, actual);
  }

  protected Collection<Predicate<String>> predicateFor(final String... glob) {
    return Glob.toGlobPredicates(Arrays.asList(glob));
  }

  protected Collection<Predicate<String>> predicateFor(final Class<?> clazz) {
    return predicateFor(clazz.getName());
  }

}
