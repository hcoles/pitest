package org.pitest.mutationtest.instrument;

import org.pitest.functional.Option;
import org.pitest.util.JavaAgent;

public class KnownLocationJavaAgentJarFinder implements JavaAgent {

  private final String location;

  public KnownLocationJavaAgentJarFinder(final String location) {
    this.location = location;
  }

  public Option<String> getJarLocation() {
    return Option.some(this.location);
  }

}
