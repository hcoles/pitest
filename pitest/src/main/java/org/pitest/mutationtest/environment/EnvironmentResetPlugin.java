package org.pitest.mutationtest.environment;

import org.pitest.plugin.ClientClasspathPlugin;

public interface EnvironmentResetPlugin extends ClientClasspathPlugin {

    ResetEnvironment make();

}
