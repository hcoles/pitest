package org.pitest.mutationtest.tooling;

import java.util.Optional;
import org.pitest.process.JavaAgent;

public class KnownLocationJavaAgentFinder implements JavaAgent {

  private final String location;

  public KnownLocationJavaAgentFinder(final String location) {
    this.location = location;
  }

  @Override
  public Optional<String> getJarLocation() {
    return Optional.ofNullable(this.location);
  }

  @Override
  public void close() {
  }

}
