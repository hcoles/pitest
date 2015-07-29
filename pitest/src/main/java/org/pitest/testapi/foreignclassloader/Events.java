package org.pitest.testapi.foreignclassloader;

import java.util.List;

import org.pitest.functional.SideEffect2;
import org.pitest.testapi.Description;
import org.pitest.testapi.ResultCollector;
import org.pitest.util.IsolationUtils;

public class Events {

  public static void applyEvents(final List<String> encodedEvents,
      final ResultCollector rc, final Description description) {
    for (final String each : encodedEvents) {
      @SuppressWarnings("unchecked")
      final SideEffect2<ResultCollector, org.pitest.testapi.Description> event = (SideEffect2<ResultCollector, org.pitest.testapi.Description>) IsolationUtils
      .fromXml(each);
      event.apply(rc, description);
    }

  }
}
