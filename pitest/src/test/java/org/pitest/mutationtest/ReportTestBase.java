package org.pitest.mutationtest;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.pitest.extension.TestListener;
import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.TestMutationTesting.MetaDataExtractor;
import org.pitest.mutationtest.instrument.ResultsReader.DetectionStatus;
import org.pitest.util.Glob;

public abstract class ReportTestBase {

  private MetaDataExtractor metaDataExtractor;
  protected ReportOptions   data;

  @Before
  public void setUp() {
    this.metaDataExtractor = new MetaDataExtractor();
    this.data = new ReportOptions();
    this.data.setSourceDirs(Collections.<File> emptyList());
    this.data.setMutators(DefaultMutationConfigFactory.DEFAULT_MUTATORS);
    this.data.setClassesInScope(predicateFor("com.example.*"));
  }

  protected ListenerFactory listenerFactory() {
    return new ListenerFactory() {

      public TestListener getListener(final CoverageDatabase coverage,
          final ReportOptions data, final long startTime) {
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

  protected Collection<Predicate<String>> predicateFor(final String glob) {
    return Glob.toGlobPredicates(Arrays.asList(glob));
  }

  protected Collection<Predicate<String>> predicateFor(final Class<?> clazz) {
    return predicateFor(clazz.getName());
  }

}
