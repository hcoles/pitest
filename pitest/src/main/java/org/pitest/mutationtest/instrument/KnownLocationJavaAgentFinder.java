package org.pitest.mutationtest.instrument;

import org.pitest.functional.Option;
import org.pitest.util.JavaAgent;

public class KnownLocationJavaAgentFinder implements JavaAgent {

  private final String location;

  public KnownLocationJavaAgentFinder(final String location) {
    this.location = location;
  }

  public Option<String> getJarLocation() {
    return Option.some(this.location);
  }

  public void close() {
  }

}
