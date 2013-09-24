package org.pitest.mutationtest;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.mockito.MockitoAnnotations;
import org.pitest.functional.predicate.Predicate;
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

  protected MutationResultListenerFactory listenerFactory() {
    return new MutationResultListenerFactory() {
      public MutationResultListener getListener(ListenerArguments args) {
        return ReportTestBase.this.metaDataExtractor;
      }

      public String name() {
        return null;
      }

      public String description() {
        return null;
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
