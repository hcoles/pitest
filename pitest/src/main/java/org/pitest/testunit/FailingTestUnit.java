package org.pitest.testunit;

import org.pitest.Description;
import org.pitest.PitError;
import org.pitest.extension.ResultCollector;

public class FailingTestUnit extends AbstractTestUnit {

  private final String message;

  public FailingTestUnit(final Description d, final String message) {
    super(d);
    this.message = message;
  }

  @Override
  public void execute(final ClassLoader loader, final ResultCollector rc) {
    rc.notifyStart(this.getDescription());
    rc.notifyEnd(this.getDescription(), new PitError(this.message));
  }

}
