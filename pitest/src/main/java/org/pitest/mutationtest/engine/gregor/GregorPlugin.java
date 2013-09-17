package org.pitest.mutationtest.engine.gregor;

import org.pitest.plugin.ClientClasspathPlugin;

public class GregorPlugin implements ClientClasspathPlugin {

  public String description() {
    return "Default Gregor mutation engine";
  }

}
